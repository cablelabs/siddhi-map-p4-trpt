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
import io.siddhi.extension.map.p4.trpt.TelemetryReport;
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
import java.util.Enumeration;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Tests for the UDP Source extension with the p4-trpt mapper to ensure that the events get sent to the inMemory sink.
 * This test case sends mock Telemetry report UDP packets to this UDP source Siddhi extension and ensures each of the
 * resulting JSON documents contains the expected values.
 */
public class UDPSourceIMSinkTelemetryReportTestCase {
    // If you will know about this related testcase,
    //refer https://github.com/siddhi-io/siddhi-io-file/blob/master/component/src/test

    private static final Logger log = Logger.getLogger(UDPSourceIMSinkTelemetryReportTestCase.class);
    private SiddhiAppRuntime siddhiAppRuntime;
    private Queue<Object> packetEventsSent;
    private Queue<String> packetJsonEventsRcvd;
    private Queue<Object> dropEventsSent;
    private Queue<Object> dropJsonEventsRcvd;
    private String testTopicPkt;
    private String testTopicDrop;
    private InMemoryBroker.Subscriber packetSubscriber;
    private InMemoryBroker.Subscriber dropSubscriber;

    @BeforeMethod
    public void setUp() {
        log.info("In setUp()");
        packetEventsSent = new ConcurrentLinkedQueue<>();
        packetJsonEventsRcvd = new ConcurrentLinkedQueue<>();
        dropEventsSent = new ConcurrentLinkedQueue<>();
        dropJsonEventsRcvd = new ConcurrentLinkedQueue<>();
        testTopicPkt = UUID.randomUUID().toString();
        testTopicDrop = UUID.randomUUID().toString();
        packetSubscriber = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object o) {
                log.info("packet subscriber onMessage() - " + o);
                if (o instanceof String) {
                    packetJsonEventsRcvd.add((String) o);
                } else {
                    packetEventsSent.add(o);
                }
            }

            @Override
            public String getTopic() {
                return testTopicPkt;
            }
        };

        dropSubscriber = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object o) {
                log.info("drop subscriber onMessage() - " + o);
                if (o instanceof String) {
                    dropJsonEventsRcvd.add((String) o);
                } else {
                    dropEventsSent.add(o);
                }
            }

            @Override
            public String getTopic() {
                return testTopicDrop;
            }
        };

        InMemoryBroker.subscribe(packetSubscriber);
        InMemoryBroker.subscribe(dropSubscriber);

        final String inStreamDefinition = String.format(
                "@App:name('IMSinkTRPT')\n" +
                "@source(type='udp', listen.port='5556', @map(type='p4-trpt',\n" +
                        "\t@attributes(in_type='telemRptHdr.inType', full_json='jsonString')))\n" +
                "define stream typeStream (in_type int, full_json object);\n\n" +

                "@sink(type='inMemory', topic='%s',\n" +
                    "\t@map(type='text'))\n" +
                "define stream trptPacket (full_json OBJECT);\n\n" +

                "@sink(type='inMemory', topic='%s',\n" +
                    "\t@map(type='text'))\n" +
                "define stream dropPacket (full_json OBJECT);\n\n" +

                "@info(name = 'TrptPacket')\n" +
                    "\tfrom typeStream[in_type != 2]\n" +
                    "\tselect full_json\n" +
                    "\tinsert into trptPacket;\n\n" +

                "@info(name = 'TrptDrop')\n" +
                    "\tfrom typeStream[in_type == 2]\n" +
                    "\tselect full_json\n" +
                    "\tinsert into dropPacket;",
                testTopicPkt, testTopicDrop);

        log.info("Siddhi script\n" + inStreamDefinition);
        final SiddhiManager siddhiManager = new SiddhiManager();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        siddhiAppRuntime.addCallback("TrptPacket", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                packetEventsSent.add(inEvents);
            }
        });
        siddhiAppRuntime.addCallback("TrptDrop", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                dropEventsSent.add(inEvents);
            }
        });
        siddhiAppRuntime.start();
    }

    @AfterMethod
    public void tearDown() {
        log.info("In tearDown()");

        try {
            siddhiAppRuntime.shutdown();
            InMemoryBroker.unsubscribe(packetSubscriber);
            InMemoryBroker.unsubscribe(dropSubscriber);
        } finally {
        }

        log.info("Siddhi App Runtime down");
    }

    /**
     * Tests to ensure that UDP IPv4 two hop Telemetry Report packets can be converted to JSON and sent out via
     * the inMemory sink.
     */
    @Test
    public void testTelemetryReportUdp4InterleaveWithDrop() {
        runTest(TestTelemetryReports.UDP4_2HOPS, TestTelemetryReports.DROP_RPT, 4);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON and sent out via
     * the inMemory sink.
     */
    @Test
    public void testTelemetryReportTcp4InterleaveWithDrop() {
        runTest(TestTelemetryReports.TCP4_2HOPS, TestTelemetryReports.DROP_RPT, 4);
    }

    /**
     * Tests to ensure that UDP IPv6 two hop Telemetry Report packets can be converted to JSON and sent out via
     * the inMemory sink.
     */
    @Test
    public void testTelemetryReportUdp6InterleaveWithDrop() {
        runTest(TestTelemetryReports.UDP6_2HOPS, TestTelemetryReports.DROP_RPT, 6);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON and sent out via
     * the inMemory sink.
     */
    @Test
    public void testTelemetryReportTcp6InterleaveWithDrop() {
        runTest(TestTelemetryReports.TCP6_2HOPS, TestTelemetryReports.DROP_RPT, 6);
    }

    private void runTest(final byte[] trptPktBytes, final byte[] trptDropBytes, final int ipVer) {
        final int numTestEvents = 50;
        try {
            sendTestEvents(trptPktBytes, trptDropBytes, numTestEvents, ipVer);
            Thread.sleep(100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        validateTelemetryReports();

        Assert.assertEquals(packetEventsSent.size(), numTestEvents);
        Assert.assertEquals(packetJsonEventsRcvd.size(), numTestEvents);
        Assert.assertEquals(dropEventsSent.size(), numTestEvents);
    }

    private void sendTestEvents(final byte[] trptPktBytes, final byte[] trptDropBytes, final int numTestEvents,
                                final int ipVer) throws Exception {
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

            final DatagramPacket trptPkt = new DatagramPacket(trptPktBytes, 0, trptPktBytes.length,
                    address, 5556);

            final DatagramPacket trptDrop;
            if (trptDropBytes != null) {
                trptDrop = new DatagramPacket(trptDropBytes, 0, trptDropBytes.length,
                        address, 5556);
            } else {
                trptDrop = null;
            }

            // TODO - need to get this???
            // configReader.readConfig(KEEP_ALIVE, "" + Constant.DEFAULT_KEEP_ALIVE)

            final DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(trptPkt);

            if (trptDrop != null) {
                datagramSocket.send(trptDrop);
            }

            Thread.sleep(1, 10000);
        }
    }

    private void validateTelemetryReports() {
        for (final Object eventObj : packetEventsSent) {
            final JsonObject trptJsonObj;
            Assert.assertTrue(eventObj instanceof Event[]);
            final Event[] event = (Event[]) eventObj;
            trptJsonObj = (JsonObject) event[0].getData(0);
            Assert.assertNotNull(trptJsonObj);
            final JsonObject trptHdrJson = trptJsonObj.get("telemRptHdr").getAsJsonObject();
            Assert.assertNotNull(trptHdrJson);
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
            Assert.assertTrue(2 != trptHdrJson.get(TelemetryReportHeader.TRPT_IN_TYPE_KEY).getAsInt());
            Assert.assertEquals(234, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
            Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());
        }

        for (final Object eventObj : packetJsonEventsRcvd) {
            Assert.assertTrue(eventObj instanceof String);
            final String eventJsonStr = (String) eventObj;
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(
                    eventJsonStr.replace("full_json:", ""));
            final JsonObject trptJsonObj = jsonElement.getAsJsonObject();
            Assert.assertNotNull(trptJsonObj);
            final JsonObject trptHdrJson = trptJsonObj.get("telemRptHdr").getAsJsonObject();
            Assert.assertNotNull(trptHdrJson);
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
            Assert.assertTrue(2 != trptHdrJson.get(TelemetryReportHeader.TRPT_IN_TYPE_KEY).getAsInt());
            Assert.assertEquals(234, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
            Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());
        }

        for (final Object eventObj : dropEventsSent) {
            final JsonObject trptJsonObj;
            Assert.assertTrue(eventObj instanceof Event[]);
            final Event[] event = (Event[]) eventObj;
            trptJsonObj = (JsonObject) event[0].getData(0);
            Assert.assertNotNull(trptJsonObj);
            final JsonObject trptHdrJson = trptJsonObj.get("telemRptHdr").getAsJsonObject();
            Assert.assertNotNull(trptHdrJson);
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_IN_TYPE_KEY).getAsInt());
            Assert.assertEquals(123, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
            Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());

            final JsonObject dropHdrJson = trptJsonObj.get("dropHdr").getAsJsonObject();
            Assert.assertEquals("6b00dbfc6026a3521bbe0f5d00170000",
                    dropHdrJson.get(TelemetryReport.DROP_KEY).getAsString());
        }

        for (final Object eventObj : dropJsonEventsRcvd) {
            Assert.assertTrue(eventObj instanceof String);
            final String eventJsonStr = (String) eventObj;
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(
                    eventJsonStr.replace("full_json:", ""));
            final JsonObject trptJsonObj = jsonElement.getAsJsonObject();
            Assert.assertNotNull(trptJsonObj);
            final JsonObject trptHdrJson = trptJsonObj.get("telemRptHdr").getAsJsonObject();
            Assert.assertNotNull(trptHdrJson);
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_IN_TYPE_KEY).getAsInt());
            Assert.assertEquals(123, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
            Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());

            final JsonObject dropHdrJson = trptJsonObj.get("dropHdr").getAsJsonObject();
            Assert.assertEquals("6b00dbfc6026a3521bbe0f5d00170000",
                    dropHdrJson.get(TelemetryReport.DROP_KEY).getAsString());
        }
    }
}
