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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.extension.map.p4.TestTelemetryReports;
import io.siddhi.extension.map.p4.trpt.TelemetryReport;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Tests for the UDP Source extension with the p4-trpt mapper to ensure that the events get sent out to Kafka with
 * "json" mapping and received by the kafka source with "json" mapping.
 */
public class SimpleDDoSAlertTestCase {
    // If you will know about this related testcase,
    //refer https://github.com/siddhi-io/siddhi-io-file/blob/master/component/src/test

    private static final Logger log = Logger.getLogger(UDPSourceToKafkaSourceTelemetryReportTestCase.class);

    private static final String kafkaServer = "localhost:9092";

    private SiddhiAppRuntime srcUdpSiddhiAppRuntime;
    private SiddhiAppRuntime kafkaIngressSiddhiAppRuntime;
    private String testTopic;
    private HttpServer httpServer;
    private TestHttpHandler handler;

    @BeforeMethod
    public void setUp() throws Exception {
        log.info("In setUp()");
        testTopic = UUID.randomUUID().toString();
        httpServer = HttpServer.create(new InetSocketAddress(5005), 0);
        handler = new TestHttpHandler();
        httpServer.createContext("/attack", handler);
        httpServer.setExecutor(Executors.newFixedThreadPool(1));
        httpServer.start();

        final SiddhiManager siddhiManager = new SiddhiManager();
        createSrcUdpAppRuntime(siddhiManager);
        createKafkaIngressAppRuntime(siddhiManager);
    }

    @AfterMethod
    public void tearDown() {
        log.info("In tearDown()");
        try {
            srcUdpSiddhiAppRuntime.shutdown();
            kafkaIngressSiddhiAppRuntime.shutdown();
        } finally {
            // Delete topic
            final Properties conf = new Properties();
            conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
            conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
            final AdminClient client = AdminClient.create(conf);
            log.info("Deleting testTopic - " + testTopic);
            client.deleteTopics(Collections.singletonList(testTopic));
            client.close();
            httpServer.stop(0);
        }

        log.info("Siddhi App Runtime down");
    }

    /**
     * Tests to ensure that UDP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportUdp4() {
        final List<TrptTestUdpSendDims> dimensions = new ArrayList<>();
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:01:01", "10.10.1.10", 1234,
                375, 2, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:02:02", "11.11.1.12", 4567,
                99, 10, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:03:03", "12.12.1.13", 5678,
                275, 3, 0));
        runTest(TestTelemetryReports.UDP4_2HOPS, dimensions, 2);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportTcp4() {
        final List<TrptTestUdpSendDims> dimensions = new ArrayList<>();
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:01:01", "10.10.1.10", 1234,
                375, 2, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:02:02", "11.11.1.12", 4567,
                99, 10, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:03:03", "12.12.1.13", 5678,
                275, 3, 0));
        runTest(TestTelemetryReports.TCP4_2HOPS, dimensions, 2);
    }

    /**
     * Tests to ensure that UDP IPv6 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportUdp6() {
        final List<TrptTestUdpSendDims> dimensions = new ArrayList<>();
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:01:01", "::1", 1234,
                375, 2, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:02:02", "::2", 4567,
                99, 10, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:03:03", "::3", 5678,
                275, 3, 0));
        runTest(TestTelemetryReports.UDP6_2HOPS, dimensions, 2);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportTcp6() {
        final List<TrptTestUdpSendDims> dimensions = new ArrayList<>();
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:01:01", "::1", 1234,
                375, 2, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:02:02", "::2", 4567,
                99, 10, 0));
        dimensions.add(new TrptTestUdpSendDims("00:00:00:00:03:03", "::3", 5678,
                275, 3, 0));
        runTest(TestTelemetryReports.TCP6_2HOPS, dimensions, 2);
    }

    /**
     * Executes the test against the byte array.
     * @param bytes - the packet UDP payload (the TelemetryReport bytes)
     */
    private void runTest(final byte[] bytes, final List<TrptTestUdpSendDims> dimensions, final int iterations) {
        TelemetryReport trpt = new TelemetryReport(bytes);
        final Set<UdpPacketSender> senders = new HashSet<>();


        for (final TrptTestUdpSendDims dimension : dimensions) {
            // Update Telemetry Report values
            trpt.ipHdr.setDstAddr(dimension.dstAddr);
            trpt.protoHdr.setDstPort(dimension.dstPort);
            trpt.intHdr.mdStackHdr.setOrigMac(dimension.origMac);

            // Instantiate runnable that is responsible for sending the packets
            senders.add(new UdpPacketSender(trpt.getBytes(), dimension.numEvents, dimension.delayMillis,
                    dimension.delayNanos));
        }

        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newScheduledThreadPool(dimensions.size());
        for (int i = 0; i < iterations; i++) {
            final List<Future> futures1 = new ArrayList<>();
            for (final UdpPacketSender sender : senders) {
                futures1.add(executor.submit(sender));
            }
            // Wait for senders to complete the initial run
            while (futures1.size() > 0) {
                futures1.removeIf(Future::isDone);
            }
            if (i < iterations - 1) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e);
        }
        // TODO - Determine the expected count based on the dimension count and frequency
        // TODO - Determine why sometimes get one less than actually expected
        Assert.assertTrue(validateResponsesWithDimension(dimensions.get(0)) >= 4,
                "dimensions.get(0) - " + dimensions.get(0)); // should always be 6
        Assert.assertEquals(validateResponsesWithDimension(dimensions.get(1)), 0);
        Assert.assertTrue(validateResponsesWithDimension(dimensions.get(2)) >= 3);
    }

    private int validateResponsesWithDimension(final TrptTestUdpSendDims dimension) {
        final JsonParser parser = new JsonParser();
        int recordsFound = 0;
        for (final String response : handler.responses) {
            final JsonObject bodyJson = (JsonObject) parser.parse(response);
            final JsonObject eventJson = bodyJson.getAsJsonObject("event");
            if (dimension.origMac.equals(eventJson.get("origMac").getAsString().replaceAll("\"", ""))) {
                try {
                    final InetAddress eventDstIp = InetAddress.getByName(
                            eventJson.get("dstAddr").getAsString().replaceAll("\"", ""));
                    final InetAddress dimDstIp = InetAddress.getByName(dimension.dstAddr);
                    Assert.assertTrue(eventDstIp.equals(dimDstIp));
                } catch (UnknownHostException e) {
                    Assert.fail("Unexpected excepton", e);
                }

                Assert.assertEquals(eventJson.get("dstPort").getAsLong(), dimension.dstPort);
                Assert.assertEquals(eventJson.get("count").getAsLong() % 100, 0);
                recordsFound++;
            }
        }
        return recordsFound;
    }

    /**
     * Method responsible for starting the kafka source Siddhi script with "json" mapper.
     * TODO - Properly define trptJsonStream and write one DDoS query
     * TODO - Add HTTP sink for making web service call to mock SDN controller
     * @param siddhiManager - the Siddhi manager to leverage
     */
    private void createKafkaIngressAppRuntime(final SiddhiManager siddhiManager) {
        final String siddhiScriptStr = String.format(
            "@app:name('Kafka-Source-JSON')\n" +
            "@source(type='kafka', topic.list='%s', bootstrap.servers='%s', group.id='test',\n" +
                "\tthreading.option='single.thread',\n" +
                "\t@map(type='p4-trpt',\n" +
                    "\t\t@attributes(origMac='intHdr.mdStackHdr.origMac', ipVer='ipHdr.version',\n" +
                        "\t\t\tdstAddr='ipHdr.dstAddr', dstPort='protoHdr.dstPort')))\n" +
            "define stream trptJsonStream (origMac string, ipVer int, dstAddr string, dstPort long);\n" +
            "@sink(type='http', publisher.url='http://localhost:5005/attack', method='POST',\n" +
                "\theaders='trp:headers', @map(type='json'))\n" +
            "define stream attackStream (origMac string, ipVer int, dstAddr string, dstPort long, count long);\n" +
            "@info(name = 'trptJsonQuery')\n" +
                "\tfrom trptJsonStream#window.time(1 sec)\n" +
                "\tselect origMac, ipVer, dstAddr, dstPort, count(ipVer) as count\n" +
                "\tgroup by origMac, dstAddr, dstPort\n" +
                "\thaving count == 100 or count == 200 or count == 300 or count == 400\n" +
                "\tinsert into attackStream;\n",
            testTopic, kafkaServer);
        log.info("Kafka-Source-JSON Siddhi script \n" + siddhiScriptStr);
        kafkaIngressSiddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiScriptStr);
        kafkaIngressSiddhiAppRuntime.start();
    }

    /**
     * Method responsible for starting the UDP source siddhi script with the "p4-trpt" mapper and kafka sink with the
     * "json" mapper.
     * @param siddhiManager - the Siddhi manager to leverage
     */
    private void createSrcUdpAppRuntime(final SiddhiManager siddhiManager) {
        final String udpSourceDefinition = String.format(
            "@app:name('UDP-Source-TRPT')\n" +
                "@source(type='udp', listen.port='5556', @map(type='p4-trpt',\n" +
                    "\t@attributes(in_type='telemRptHdr.inType', full_json='jsonString')))\n" +
                "define stream typeStream (in_type int, full_json object);\n\n" +

                "@sink(type='kafka', topic='%1$s', bootstrap.servers='%2$s', is.binary.message = 'false',\n" +
                    "\t@map(type='text'))\n" +
                "define stream trptPacket (full_json OBJECT);\n\n" +

                "@info(name = 'TrptPacket')\n" +
                    "\tfrom typeStream[in_type != 2]\n" +
                    "\tselect full_json\n" +
                    "\tinsert into trptPacket;",
                testTopic, kafkaServer);
        log.info("UDP-Source-TRPT Siddhi script \n" + udpSourceDefinition);
        srcUdpSiddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(udpSourceDefinition);
        srcUdpSiddhiAppRuntime.start();
    }

    private static class TestHttpHandler implements HttpHandler {

        public final List<String> responses = new ArrayList<>();

        @Override
        public void handle(HttpExchange httpExchange) {
            try {
                final String text = IOUtils.toString(httpExchange.getRequestBody(), StandardCharsets.UTF_8);
                responses.add(text);
            } catch (IOException e) {
                log.error("Unexpected error extracting HTTP request body");
            }
        }
    }
}

/**
 * Class for sending out packets in a ThreadPoolExecutor.
 */
class UdpPacketSender implements Runnable {
    private final byte[] udpPayloadBytes;
    private final int numEvents;
    private final long delayMs;
    private final int delayNanos;

    public UdpPacketSender(final byte[] bytes, final int num, final long dMili, final int dNanos) {
        udpPayloadBytes = bytes.clone();
        numEvents = num;
        delayMs = dMili;
        delayNanos = dNanos;
    }

    @Override
    public void run() {
        for (int ctr = 0; ctr < numEvents; ctr++) {
            final InetAddress address;
            try {
                address = InetAddress.getByName("localhost");
                final DatagramPacket packet = new DatagramPacket(udpPayloadBytes, 0, udpPayloadBytes.length,
                        address, 5556);
                final DatagramSocket datagramSocket = new DatagramSocket();
                datagramSocket.send(packet);
                Thread.sleep(delayMs, delayNanos);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

/**
 * Designed to determine how the Telemetry report bytes will be formed and sent.
 */
class TrptTestUdpSendDims {
    public final String origMac;
    public final String dstAddr;
    public final long dstPort;
    public final int numEvents;
    public final long delayMillis;
    public final int delayNanos;

    TrptTestUdpSendDims(final String origMac, final String dstAddr, final long dstPort, final int numEvents,
                        final long delayMillis, final int delayNanos) {
        this.origMac = origMac;
        this.dstAddr = dstAddr;
        this.dstPort = dstPort;
        this.numEvents = numEvents;
        this.delayMillis = delayMillis;
        this.delayNanos = delayNanos;
    }
}
