/*
 * Copyright (c) 2015 Pradeeban Kathiravelu and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.apache.qpid.amqp_1_0.jms.impl.*;

import javax.jms.*;

public class Publisher {

    private static boolean isKarafBased = false;

    public static void main(String[] args) throws Exception {
        publish(args);
    }

    public static void publish(String[] args) throws JMSException, InterruptedException {
        String destination = arg(args, 0, "topic://event");
        publish(destination);
    }

    public static void publish(){
        String destination = "topic://event";
        try {
            publish(destination);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void publish(String destination) throws JMSException, InterruptedException {
        String user = isKarafBased ? env("ACTIVEMQ_USER", "karaf") : env("ACTIVEMQ_USER", "admin");
        String password = isKarafBased ? env("ACTIVEMQ_PASSWORD", "karaf") : env("ACTIVEMQ_PASSWORD", "password");

        String host = env("ACTIVEMQ_HOST", "localhost");
        int port = Integer.parseInt(env("ACTIVEMQ_PORT", "5672"));

        int messages = 10000;
        int size = 256;

        String DATA = "abcdefghijklmnopqrstuvwxyz";
        String body = "";
        for (int i = 0; i < size; i++) {
            body += DATA.charAt(i % DATA.length());
        }

        ConnectionFactoryImpl factory = new ConnectionFactoryImpl(host, port, user, password);
        Destination dest = null;
        if (destination.startsWith("topic://")) {
            dest = new TopicImpl(destination);
        } else {
            dest = new QueueImpl(destination);
        }

        Connection connection = factory.createConnection(user, password);
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        for (int i = 1; i <= messages; i++) {
            TextMessage msg = session.createTextMessage("#:" + i);
            msg.setIntProperty("id", i);
            producer.send(msg);
            if ((i % 1000) == 0) {
                System.out.println(String.format("Sent %d messages", i));
            }
        }
    }

    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if (rc == null)
            return defaultValue;
        return rc;
    }

    private static String arg(String[] args, int index, String defaultValue) {
        if (index < args.length)
            return args[index];
        else
            return defaultValue;
    }

}
