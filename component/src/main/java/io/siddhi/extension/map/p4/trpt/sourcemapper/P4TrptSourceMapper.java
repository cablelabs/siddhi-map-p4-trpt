package io.siddhi.extension.map.p4.trpt.sourcemapper;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.source.AttributeMapping;
import io.siddhi.core.stream.input.source.InputEventHandler;
import io.siddhi.core.stream.input.source.SourceMapper;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.map.p4.trpt.TelemetryReport;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Annotation of Siddhi Extension.
 * <pre><code>
 * eg:-
 * {@literal @}Extension(
 * name = "The name of the extension",
 * namespace = "The namespace of the extension",
 * description = "The description of the extension (optional).",
 * //Source Mapper configurations
 * parameters = {
 * {@literal @}Parameter(name = "The name of the first parameter",
 *                               description= "The description of the first parameter",
 *                               type =  "Supported parameter types.
 *                                        eg:{DataType.STRING, DataType.INT, DataType.LONG etc}",
 *                               dynamic= "false
 *                                         (if parameter doesn't depend on each event then dynamic parameter is false.
 *                                         In Source, only use static parameter)",
 *                               optional= "true/false, defaultValue= if it is optional then assign a default value
 *                                          according to the type."),
 * {@literal @}Parameter(name = "The name of the second parameter",
 *                               description= "The description of the second parameter",
 *                               type =   "Supported parameter types.
 *                                         eg:{DataType.STRING, DataType.INT, DataType.LONG etc}",
 *                               dynamic= "false
 *                                         (if parameter doesn't depend on each event then dynamic parameter is false.
 *                                         In Source, only use static parameter)",
 *                               optional= "true/false, defaultValue= if it is optional then assign a default value
 *                                         according to the type."),
 * },
 * //If Source Mapper system configurations will need then
 * systemParameters = {
 * {@literal @}SystemParameter(name = "The name of the first  system parameter",
 *                                      description="The description of the first system parameter." ,
 *                                      defaultValue = "the default value of the system parameter.",
 *                                      possibleParameter="the possible value of the system parameter.",
 *                               ),
 * },
 * examples = {
 * {@literal @}Example(syntax = "sample query with Source Mapper annotation that explain how extension use in Siddhi."
 *                              description =" The description of the given example's query."
 *                      ),
 * }
 * )
 * </code></pre>
 */

@Extension(
        name = "p4-trpt",
        namespace = "sourceMapper",
        description = "Maps a P4 Telemetry Report byte array into JSON",
        examples = {
                @Example(
                        syntax = " ",
                        description = " "
                )
        }
)
// for more information refer https://siddhi.io/en/v5.0/docs/query-guide/#source-mapper
public class P4TrptSourceMapper extends SourceMapper {

    private static final Logger log = Logger.getLogger(P4TrptSourceMapper.class);

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
    protected void mapAndProcess(Object eventObject, InputEventHandler inputEventHandler) {
        log.info("Event object class - " + eventObject.getClass().getName());
        log.info("Event values - " + eventObject.toString());

        final TelemetryReport telemetryReport;
        if (eventObject instanceof ByteBuffer) {
            telemetryReport = new TelemetryReport(((ByteBuffer) eventObject).array());
        } else if (eventObject instanceof byte[]) {
            telemetryReport = new TelemetryReport((byte[]) eventObject);
        } else {
            throw new RuntimeException("Invalid object, cannot continue to process");
        }
        final Event event = new Event();
        final Object[] eventObjs = new Object[1];
        eventObjs[0] = telemetryReport;
        event.setData(eventObjs);
        try {
            inputEventHandler.sendEvent(event);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected error processing event", e);
        }
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
