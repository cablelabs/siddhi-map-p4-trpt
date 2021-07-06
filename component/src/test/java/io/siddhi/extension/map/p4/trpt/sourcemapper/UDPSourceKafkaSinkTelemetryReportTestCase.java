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
import io.siddhi.extension.map.p4.TestTelemetryReports;
import io.siddhi.extension.map.p4.trpt.TelemetryReport;
import io.siddhi.extension.map.p4.trpt.TelemetryReportHeader;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


/**
 * Tests for the UDP Source extension with the p4-trpt mapper to ensure that the events get sent out to Kafka.
 * This test case sends mock Telemetry report UDP packets to this UDP source Siddhi extension and ensures each of the
 * resulting JSON documents contains the expected values.
 */
public class UDPSourceKafkaSinkTelemetryReportTestCase {
    // If you will know about this related testcase,
    //refer https://github.com/siddhi-io/siddhi-io-file/blob/master/component/src/test

    private static final Logger log = Logger.getLogger(UDPSourceKafkaSinkTelemetryReportTestCase.class);
    private SiddhiAppRuntime siddhiAppRuntime;
    private List<Event[]> packetEvents;
    private List<Event[]> dropEvents;
    private String testTopicPkt;
    private String testTopicDrop;
    private KafkaRunnable trptPktConsumer;
    private KafkaRunnable trptDropConsumer;
    private static final String kafkaServer = "localhost:9092";

    @BeforeMethod
    public void setUp() {
        log.info("In setUp()");
        packetEvents = new ArrayList<>();
        dropEvents = new ArrayList<>();
        testTopicPkt = UUID.randomUUID().toString();
        testTopicDrop = UUID.randomUUID().toString();

        final String inStreamDefinition = String.format(
                "@app:name('Kafka-Sink-TRPT')\n" +
                "@source(type='udp', listen.port='5556', @map(type='p4-trpt',\n" +
                        "\t@attributes(in_type='telemRptHdr.inType', full_json='jsonString')))\n" +
                "define stream typeStream (in_type int, full_json object);\n\n" +

                "@sink(type='kafka', topic='%s', bootstrap.servers='%s', is.binary.message = 'false',\n" +
                    "\t@map(type='text'))\n" +
                "define stream trptPacket (full_json OBJECT);\n\n" +

                "@sink(type='kafka', topic='%s', bootstrap.servers='%s', is.binary.message = 'false',\n" +
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
                testTopicPkt, kafkaServer, testTopicDrop, kafkaServer);

        log.info("Siddhi script\n" + inStreamDefinition);
        final SiddhiManager siddhiManager = new SiddhiManager();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        siddhiAppRuntime.addCallback("TrptPacket", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                packetEvents.add(inEvents);
            }
        });
        siddhiAppRuntime.addCallback("TrptDrop", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                dropEvents.add(inEvents);
            }
        });
        siddhiAppRuntime.start();
        trptPktConsumer = KafkaRunnable.runConsumer(kafkaServer, testTopicPkt);
        trptDropConsumer = KafkaRunnable.runConsumer(kafkaServer, testTopicDrop);
    }

    @AfterMethod
    public void tearDown() {
        log.info("In tearDown()");
        trptPktConsumer.stop();
        trptDropConsumer.stop();

        try {
            siddhiAppRuntime.shutdown();
        } finally {
            // Delete topic
            final Properties conf = new Properties();
            conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
            conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
            final AdminClient client = AdminClient.create(conf);
            log.info("Deleting testTopic - " + testTopicPkt);
            client.deleteTopics(Collections.singletonList(testTopicPkt));
            log.info("Deleting testTopic - " + testTopicDrop);
            client.deleteTopics(Collections.singletonList(testTopicDrop));
            client.close();
        }

        log.info("Siddhi App Runtime down");
    }

    /**
     * Tests to ensure that UDP IPv4 two hop Telemetry Report packets can be converted to JSON and sent out via Kafka.
     */
    @Test
    public void testTelemetryReportUdp4InterleaveWithDrop() {
        runTest(TestTelemetryReports.UDP4_2HOPS, TestTelemetryReports.DROP_RPT, 4);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON and sent out via Kafka.
     */
    @Test
    public void testTelemetryReportTcp4InterleaveWithDrop() {
        runTest(TestTelemetryReports.TCP4_2HOPS, TestTelemetryReports.DROP_RPT, 4);
    }

    /**
     * Tests to ensure that UDP IPv6 two hop Telemetry Report packets can be converted to JSON and sent out via Kafka.
     */
    @Test
    public void testTelemetryReportUdp6InterleaveWithDrop() {
        runTest(TestTelemetryReports.UDP6_2HOPS, TestTelemetryReports.DROP_RPT, 6);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON and sent out via Kafka.
     */
    @Test
    public void testTelemetryReportTcp6InterleaveWithDrop() {
        runTest(TestTelemetryReports.TCP6_2HOPS, TestTelemetryReports.DROP_RPT, 6);
    }

    private void runTest(final byte[] trptPktBytes, final byte[] trptDropBytes, final int ipVer) {
        final int numTestEvents = 50;
        try {
            sendTestEvents(trptPktBytes, trptDropBytes, numTestEvents, ipVer);

            // Wait a sec for the processing to complete
            Thread.sleep(3000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        validateTelemetryReports();

        // TODO - Determine why all are not always received by the Kafka Consumer, KafkaRunnable
        Assert.assertTrue(trptPktConsumer.events.size() <= numTestEvents
                && trptPktConsumer.events.size() > numTestEvents * .50,
                "Expected = " + numTestEvents + " Size = " + trptPktConsumer.events.size());
        Assert.assertTrue(trptDropConsumer.events.size() <= numTestEvents
                && trptDropConsumer.events.size() > numTestEvents * .50,
                "Expected = " + numTestEvents + "Size = " + trptDropConsumer.events.size());
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
        for (final String eventStr : trptPktConsumer.events) {
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(eventStr.replaceAll("full_json:", ""));
            final JsonObject trptJsonObj = jsonElement.getAsJsonObject();
            Assert.assertNotNull(trptJsonObj);
            final JsonObject trptHdrJson = trptJsonObj.get("telemRptHdr").getAsJsonObject();
            Assert.assertNotNull(trptHdrJson);
            Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
            Assert.assertTrue(2 != trptHdrJson.get(TelemetryReportHeader.TRPT_IN_TYPE_KEY).getAsInt());
            Assert.assertEquals(234, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
            Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());
        }
        for (final String eventStr : trptDropConsumer.events) {
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(eventStr.replaceAll("full_json:", ""));
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
