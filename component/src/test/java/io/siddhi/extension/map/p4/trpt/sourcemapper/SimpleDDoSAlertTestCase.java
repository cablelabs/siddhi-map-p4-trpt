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

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.map.p4.TestTelemetryReports;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Tests for the UDP Source extension with the p4-trpt mapper to ensure that the events get sent out to Kafka with
 * "json" mapping and received by the kafka source with "json" mapping.
 */
public class SimpleDDoSAlertTestCase {
    // If you will know about this related testcase,
    //refer https://github.com/siddhi-io/siddhi-io-file/blob/master/component/src/test

    private static final Logger log = Logger.getLogger(UDPSourceToKafkaSourceTelemetryReportTestCase.class);

    private static final String kafkaServer = "wso2-vm:9092";
    private static final int numTestEvents = 200;
    private static final int waitMs = 500;

    private SiddhiAppRuntime srcUdpSiddhiAppRuntime;
    private SiddhiAppRuntime kafkaIngressSiddhiAppRuntime;
    private List<Event[]> alertEvents;
    private String testTopic;
    private HttpServer httpServer;
    private MyHttpHandler handler;

    @BeforeMethod
    public void setUp() throws Exception {
        log.info("In setUp()");
        alertEvents = new ArrayList<>();
        testTopic = UUID.randomUUID().toString();
        httpServer = HttpServer.create(new InetSocketAddress(5005), 0);
        handler = new MyHttpHandler();
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
        runTest(TestTelemetryReports.UDP4_2HOPS);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportTcp4() {
        runTest(TestTelemetryReports.TCP4_2HOPS);
    }

    /**
     * Tests to ensure that UDP IPv6 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportUdp6() {
        runTest(TestTelemetryReports.UDP6_2HOPS);
    }

    /**
     * Tests to ensure that TCP IPv4 two hop Telemetry Report packets can be converted to JSON, sent out and received
     * back via Kafka.
     */
    @Test
    public void testTelemetryReportTcp6() {
        runTest(TestTelemetryReports.TCP6_2HOPS);
    }

    /**
     * Executes the test against the byte array.
     * @param bytes - the packet UDP payload (the TelemetryReport bytes)
     */
    private void runTest(final byte[] bytes) {
        try {
            sendTestEvents(bytes, numTestEvents, 1);
            Thread.sleep(1500);
            sendTestEvents(bytes, numTestEvents, 1);

            // Wait a short bit for the processing to complete
            Thread.sleep(waitMs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // TODO - make more dynamic
//        Assert.assertEquals(alertEvents.size(), 1);
        Assert.assertEquals(alertEvents.size(), 2);
        validateAlertEvents();

        // TODO - HTTP requests with keyvalue mapping (maybe JSON encoded body too) not working with HttpSink
        //  (and hacked version HttpSinkFix)
        Assert.assertEquals(handler.responses.size(), 2);
        validateHttpResponses();
    }

    void validateAlertEvents() {
        for (final Event[] events : alertEvents) {
            Assert.assertEquals(events.length, 1);
            Assert.assertEquals((String) events[0].getData(0), "00:00:00:00:01:01");
            if (events[0].getData(1) == "4") {
                Assert.assertEquals(events[0].getData(2), "192.168.1.10");
                Assert.assertEquals(events[0].getData(3), "5792");
            }
        }
    }

    void validateHttpResponses() {
        for (final String response : handler.responses) {
            Assert.assertTrue(response.contains("00:00:00:00:01:01"));
            Assert.assertTrue(response.contains("5792"));
        }
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
                "threading.option='single.thread',\n" +
                "@map(type='p4-trpt-json',\n" +
                    "@attributes(origMac='intHdr.mdStackHdr.origMac', ipVer='ipHdr.version',\n" +
                    "dstAddr='ipHdr.dstAddr', dstPort='dstPort')))\n" +
            "define stream trptJsonStream (origMac string, ipVer int, dstAddr string, dstPort long);\n" +
            "@sink(type='http-fix', publisher.url='http://localhost:5005/attack', method='POST',\n" +
                "headers='trp:headers', @map(type='keyvalue'))\n" +
            "define stream attackStream (origMac string, ipVer int, dstAddr string, dstPort long, count long);\n" +
            "@info(name = 'trptJsonQuery')\n" +
                "from trptJsonStream#window.time(1 sec)\n" +
                "select origMac, ipVer, dstAddr, dstPort, count(ipVer) as count\n" +
                "group by origMac, dstAddr, dstPort\n" +
                "having count == 100\n" +
                "insert into attackStream;\n",
            testTopic, kafkaServer);
        log.info("Kafka-Source-JSON Siddhi script \n" + siddhiScriptStr);
        kafkaIngressSiddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiScriptStr);
        kafkaIngressSiddhiAppRuntime.addCallback("trptJsonQuery", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                log.info("Query response JSON");
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                alertEvents.add(inEvents);
            }
        });
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
                "@source(type='udp', listen.port='5556', @map(type='p4-trpt'))\n" +
                "@sink(type='kafka', topic='%s', bootstrap.servers='%s', @map(type='json'))\n" +
                "define stream trptUdpPktStream (telemRpt object);",
                testTopic, kafkaServer);
        log.info("UDP-Source-TRPT Siddhi script \n" + udpSourceDefinition);
        srcUdpSiddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(udpSourceDefinition);
        srcUdpSiddhiAppRuntime.start();
    }

    private void sendTestEvents(final byte[] eventBytes, final int numTestEvents, final long delayMs) throws Exception {
        for (int ctr = 0; ctr < numTestEvents; ctr++) {
            final InetAddress address = InetAddress.getByName("localhost");
            final DatagramPacket packet = new DatagramPacket(eventBytes, 0, eventBytes.length,
                    address, 5556);
            final DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
            Thread.sleep(delayMs);
        }
    }

    private static class MyHttpHandler implements HttpHandler {

        public final List<String> responses = new ArrayList<>();

        @Override
        public void handle(HttpExchange httpExchange) {
            // TODO - need to figure out why HttpSink is not forming the POST call properly
            log.info("httpExchange request method - " + httpExchange.getRequestMethod());
            log.info("httpExchange request URI - " + httpExchange.getRequestURI());
            final HttpContext context = httpExchange.getHttpContext();
            log.info("httpExchange request context attributes - " + context.getAttributes());
            log.info("httpExchange request context path - " + context.getPath());
            responses.add(httpExchange.getRequestURI().toString());
        }
    }

}

