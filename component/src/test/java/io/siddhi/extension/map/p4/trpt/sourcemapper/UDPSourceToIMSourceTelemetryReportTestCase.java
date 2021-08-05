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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.transport.InMemoryBroker;
import io.siddhi.extension.map.p4.TestTelemetryReports;
import io.siddhi.extension.map.p4.trpt.TelemetryReportHeader;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;


/**
 * Tests for the UDP Source extension with the p4-trpt mapper to ensure that the events get sent out to inMemory with
 * "json" mapping and received by the inMemory source with "p4-trpt" mapping.
 */
public class UDPSourceToIMSourceTelemetryReportTestCase {
    // If you will know about this related testcase,
    //refer https://github.com/siddhi-io/siddhi-io-file/blob/master/component/src/test

    private static final Logger log = Logger.getLogger(UDPSourceToIMSourceTelemetryReportTestCase.class);
    private SiddhiAppRuntime srcUdpSiddhiAppRuntime;
    private SiddhiAppRuntime ingressSiddhiAppRuntime;
    private List<Object> parsedUdpEvents;
    private List<Object> ingressEvents;
    private String testTopic;
    private static final int numTestEvents = 500;
    private static final int waitMs = 100;
    private InMemoryBroker.Subscriber brokerSubscriber;

    @BeforeMethod
    public void setUp() {
        log.info("In setUp()");
        parsedUdpEvents = new ArrayList<>();
        ingressEvents = new ArrayList<>();
        testTopic = UUID.randomUUID().toString();

        brokerSubscriber = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object o) {
                log.info("packet subscriber onMessage() - " + o);
                if (o instanceof String) {
                    ingressEvents.add((String) o);
                }
            }

            @Override
            public String getTopic() {
                return testTopic;
            }
        };

        InMemoryBroker.subscribe(brokerSubscriber);

        final SiddhiManager siddhiManager = new SiddhiManager();
        createSrcUdpAppRuntime(siddhiManager);
        createIngressAppRuntime(siddhiManager);
    }

    @AfterMethod
    public void tearDown() {
        log.info("In tearDown()");
        try {
            srcUdpSiddhiAppRuntime.shutdown();
            ingressSiddhiAppRuntime.shutdown();
            InMemoryBroker.unsubscribe(brokerSubscriber);
        } finally {
        }

        log.info("Siddhi App Runtime down");
    }

    /**
     * Tests to ensure that UDP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out via IPv4
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportUdp4Via4() {
        runTest(TestTelemetryReports.UDP4_2HOPS, 4);
    }

    /**
     * Tests to ensure that UDP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out via IPv6
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportUdp4Via6() {
        runTest(TestTelemetryReports.UDP4_2HOPS, 6);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out via IPv4
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportTcp4Via4() {
        runTest(TestTelemetryReports.TCP4_2HOPS, 4);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out via IPv6
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportTcp4Via6() {
        runTest(TestTelemetryReports.TCP4_2HOPS, 6);
    }

    /**
     * Tests to ensure that UDP IPv6 two hop Telemetry Report packets can be converted to JSON, sent out via IPv4
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportUdp6Via4() {
        runTest(TestTelemetryReports.UDP6_2HOPS, 4);
    }

    /**
     * Tests to ensure that UDP IPv6 two hop Telemetry Report packets can be converted to JSON, sent out via IPv6
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportUdp6Via6() {
        runTest(TestTelemetryReports.UDP6_2HOPS, 6);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out via IPv4
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportTcp6Via4() {
        runTest(TestTelemetryReports.TCP6_2HOPS, 4);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out via IPv4
     * and received back via the inMemory sink.
     */
    @Test
    public void testTelemetryReportTcp6Via6() {
        runTest(TestTelemetryReports.TCP6_2HOPS, 6);
    }

    /**
     * Executes the test against the byte array.
     * @param bytes - the packet UDP payload (the TelemetryReport bytes)
     */
    private void runTest(final byte[] bytes, final int ipVer) {
        try {
            sendTestEvents(bytes, numTestEvents, ipVer);

            // Wait a short bit for the processing to complete
            Thread.sleep(waitMs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(numTestEvents, ingressEvents.size());
        Assert.assertEquals(numTestEvents, parsedUdpEvents.size());

        validateTelemetryReports();
    }

    /**
     * Method responsible for starting the inMemory sink source Siddhi script with "json" mapper.
     * TODO - Properly define trptJsonStream and write one DDoS query
     * TODO - Add HTTP sink for making web service call to mock SDN controller
     * @param siddhiManager - the Siddhi manager to leverage
     */
    private void createIngressAppRuntime(final SiddhiManager siddhiManager) {
        final String sourceDefinition = String.format(
                "@App:name('IMSourceTRPT')\n" +
                    "@source(type='inMemory', topic.list='%s', group.id='test',\n" +
                    "threading.option='single.thread',\n" +
                        "@map(type='p4-trpt',\n" +
                            "@attributes(domainId='telemRptHdr.domainId', ipVer='ipHdr.version',\n" +
                                "dstAddr='ipHdr.dstAddr', dstPort='protoHdr.dstPort')))\n" +
                    "@sink(type='file', file.uri='/tmp/alerts.out', @map(type='json'))\n" +
                    "define stream trptJsonStream (domainId long, ipVer int, dstAddr string, dstPort long);",
                testTopic);
        final String sourceQuery =
                "@info(name = 'sourceQuery')\n" +
                    "from trptJsonStream\n" +
                    "select *\n" +
                    "insert into parsedTrpt;";
        log.info("IM-Source-TRPT Siddhi script \n" + sourceDefinition + sourceQuery);
        ingressSiddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sourceDefinition + sourceQuery);
        ingressSiddhiAppRuntime.addCallback("sourceQuery", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                log.info("Receiving TRPT JSON");
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                ingressEvents.add(inEvents);
            }
        });
        ingressSiddhiAppRuntime.start();
    }

    /**
     * Method responsible for starting the UDP source siddhi script with the "p4-trpt" mapper and the inMemory sink with
     * the "json" mapper.
     * @param siddhiManager - the Siddhi manager to leverage
     */
    private void createSrcUdpAppRuntime(final SiddhiManager siddhiManager) {
        final String inStreamDefinition = String.format(
                "@App:name('IMSinkTRPT')\n" +
                    "@source(type='udp', listen.port='5556', @map(type='p4-trpt',\n" +
                        "\t@attributes(in_type='telemRptHdr.inType', full_json='jsonString')))\n" +
                    " define stream typeStream (in_type int, full_json object);\n\n" +

                    "@sink(type='inMemory', topic='%s',\n" +
                        "\t@map(type='text'))\n" +
                    "define stream trptPacket (full_json OBJECT);\n\n" +

                    "@info(name = 'TrptPacket')\n" +
                        "\tfrom typeStream[in_type != 2]\n" +
                        "\tselect full_json\n" +
                        "\tinsert into trptPacket;",
                testTopic);

        log.info("UDP-Source-TRPT Siddhi script \n" + inStreamDefinition);
        srcUdpSiddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        srcUdpSiddhiAppRuntime.addCallback("TrptPacket", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                log.info("Receiving TRPT JSON");
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                parsedUdpEvents.add(inEvents);
            }
        });
        srcUdpSiddhiAppRuntime.start();
    }

    private void sendTestEvents(final byte[] eventBytes, final int numTestEvents, final int ipVer) throws Exception {
        for (int ctr = 0; ctr < numTestEvents; ctr++) {
            final NetworkInterface netIface = NetworkInterface.getByName("lo");
            final Enumeration inetAddresses = netIface.getInetAddresses();
            InetAddress address = null;

            while (inetAddresses.hasMoreElements()) {
                final InetAddress addr = (InetAddress) inetAddresses.nextElement();
                if (ipVer == 4 && addr instanceof Inet4Address) {
                    address = addr;
                    break;
                } else if (ipVer == 6 && addr instanceof Inet6Address) {
                    address = addr;
                    break;
                }
            }
            final DatagramPacket packet = new DatagramPacket(eventBytes, 0, eventBytes.length,
                    address, 5556);
            final DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
            Thread.sleep(1, 1000);
        }
    }

    private void validateTelemetryReports() {
        for (final Object events : parsedUdpEvents) {
            for (final Event event : (Event[]) events) {
                validateTrptJsonEvent(event);
            }
        }
        for (final Object eventObj : ingressEvents) {
            Assert.assertTrue(eventObj instanceof String);
            final String eventJsonStr = (String) eventObj;
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(
                    eventJsonStr.replace("full_json:", ""));
            final JsonObject trptJsonObj = jsonElement.getAsJsonObject();
            validateTrptJson(trptJsonObj);
        }
    }

    /**
     * Validates the events generated by the UDP-Source-TRPT Siddhi script.
     * @param event - the Telemetry Report JSON Event object to validate
     */
    private void validateTrptJsonEvent(final Event event) {
        final JsonObject trptJsonObj = (JsonObject) event.getData()[0];
        validateTrptJson(trptJsonObj);
    }

    private void validateTrptJson(final JsonObject trptJson) {
        Assert.assertNotNull(trptJson);
        Assert.assertNotNull(trptJson.getAsJsonObject("telemRptHdr"));
        final JsonObject trptHdrJson = trptJson.getAsJsonObject("telemRptHdr");
        Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
        Assert.assertEquals(234, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
        Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());
    }


}
