/*
 * Copyright (c) 2021 Cable Television Laboratories, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.siddhi.extension.map.p4.trpt.sourcemapper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.source.AttributeMapping;
import io.siddhi.core.stream.input.source.InputEventHandler;
import io.siddhi.core.stream.input.source.SourceMapper;
import io.siddhi.core.util.AttributeConverter;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.map.p4.trpt.TelemetryReport;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Siddhi P4 Telemetry Report source mapper extension.
 * for more information refer https://siddhi.io/en/v5.0/docs/query-guide/#source-mapper.
 */
@Extension(
        name = "p4-trpt",
        namespace = "sourceMapper",
        description = "Maps a P4 Telemetry Report byte array into JSON",
        examples = {
                @Example(
                        syntax = "@map(type='p4-trpt')",
                        description = "Best when used with udp plugin when listening to the Telemetry Report port"
                )
        }
)
public class P4TrptSourceMapper extends SourceMapper {

    private static final Logger log = Logger.getLogger(P4TrptSourceMapper.class);
    private List<AttributeMapping> attributeMappingList;
    private final AttributeConverter attributeConverter = new AttributeConverter();
    private final JsonParser parser = new JsonParser();

    /**
     * The initialization method for {@link SourceMapper}, which will be called before other methods and validate
     * the all configuration and getting the initial values.
     *
     * @param streamDefinition     Associated output stream definition
     * @param optionHolder         Option holder containing static configuration related to the {@link SourceMapper}
     * @param attributeMappingList Custom attribute mapping for source-mapping
     * @param configReader         to read the {@link SourceMapper} related system configuration.
     * @param siddhiAppContext     the context of the {@link io.siddhi.query.api.SiddhiApp} used to get siddhi
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                     List<AttributeMapping> attributeMappingList, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        this.attributeMappingList = attributeMappingList;
    }

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
     */
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class, byte[].class, ByteBuffer.class};
    }

    /**
     * Method to map the incoming event and as pass that via inputEventHandler to process further.
     *
     * @param eventObject           Incoming event Object based on the supported event class imported by the extensions.
     * @param inputEventHandler     Handler to pass the converted Siddhi Event for processing
     */
    @Override
    protected void mapAndProcess(Object eventObject, InputEventHandler inputEventHandler)
            throws InterruptedException {
        log.debug("Event object class - " + eventObject.getClass().getName());
        log.debug("Event values - " + eventObject);

        final JsonObject trptJson;

        long timestamp = System.currentTimeMillis();
        if (eventObject instanceof ByteBuffer) {
            final TelemetryReport telemetryReport = new TelemetryReport(((ByteBuffer) eventObject).array());
            timestamp = ((ByteBuffer) eventObject).getLong();
             trptJson = telemetryReport.toJson();
        } else if (eventObject instanceof byte[]) {
            final TelemetryReport telemetryReport = new TelemetryReport((byte[]) eventObject);
            trptJson = telemetryReport.toJson();
        } else if (eventObject instanceof String) {
            String eventString = (String) eventObject;
            eventString = eventString.substring(eventString.indexOf(':') + 1);
            trptJson = (JsonObject) parser.parse(eventString);
        } else {
            throw new RuntimeException("Invalid object, cannot continue to process");
        }
        int ctr = 0;


        final Object[] eventAttr = new Object[attributeMappingList.size()];
        for (final AttributeMapping mapping : attributeMappingList) {
            eventAttr[ctr++] = extractField(trptJson, mapping);
            log.debug("Extracted field " + eventAttr[ctr - 1] + " with mapping " + mapping.getMapping());
        }
        final Event event = new Event(attributeMappingList.size());
        event.setData(eventAttr);
        event.setTimestamp(timestamp);
        try {
            inputEventHandler.sendEvent(event);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected error processing event", e);
        } catch (Throwable e2) {
            throw new RuntimeException("Unexpected throwable processing event", e2);
        }
    }

    private Object extractField(final JsonObject jsonObject, final AttributeMapping attrMapping)
            throws InterruptedException {
        if (jsonObject == null) {
            throw new InterruptedException("JSON element is null");
        }
        log.debug("Extracting jsonObject - " + jsonObject);
        log.debug("Attribute mapping - " + attrMapping.getMapping());
        if (attrMapping.getMapping().equals("jsonString")) {
            return jsonObject;
        }
        final String[] tokens = attrMapping.getMapping().split("\\.");
        JsonObject thisElem = jsonObject;
        for (int i = 0; i < tokens.length; i++) {
            if (i < tokens.length - 1) {
                thisElem = thisElem.getAsJsonObject(tokens[i]);
                if (thisElem == null) {
                    throw new InterruptedException(
                            "JSON element not found - " + tokens[i] + " for mapping - " + attrMapping.getMapping());
                }
            } else {
                final String contents;
                try {
                    contents = thisElem.get(tokens[i]).toString().replaceAll("\"", "");
                } catch (NullPointerException npe) {
                    throw new InterruptedException(npe.getMessage());
                }
                final Object outObj = attributeConverter.getPropertyValue(contents, attrMapping.getType());
                if (outObj == null) {
                    throw new InterruptedException(
                            "JSON element not found - " + tokens[i] + " for mapping - " + attrMapping.getMapping());
                }
                return outObj;
            }
        }
        throw new InterruptedException("Could locate field denoted by - " + attrMapping.getMapping());
    }

    /**
     * Method used by {@link SourceMapper} to determine on how to handle transport properties with null values. If
     * this returns 'false' then {@link SourceMapper} will drop any event/s with null transport
     * property values. If this returns
     * 'true' then {@link SourceMapper} will send events even though they contains null transport properties.
     * This method will be called after init().
     *
     * @return whether {@link SourceMapper} should allow or drop events when transport properties are null.
     */
    @Override
    protected boolean allowNullInTransportProperties() {
        return false;
    }
}
