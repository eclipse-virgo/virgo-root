/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.test.tools;

import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class UrlWaitLatch {

    public static final long HALF_SECOND = 500;

    public static final long ONE_MINUTE = 60 * 1000;

    public static int waitFor(String url) {
        return waitFor(url, HALF_SECOND, ONE_MINUTE);
    }

    public static int waitFor(String url, String username, String password) {
        return waitFor(url, username, password, HALF_SECOND, ONE_MINUTE);
    }

    public static int waitFor(String url, long interval, long duration) {
        return waitFor(url, null, null, interval, duration);
    }

    public static int waitFor(String url, String username, String password, long interval, long duration) {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (username != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }

        final HttpGet get = new HttpGet(url);
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                if (get != null) {
                    System.err.println("Operation timed out. Aborting request.");
                    get.abort();
                }
            }
        };
        new Timer(true).schedule(task, Duration.ofSeconds(60).toMillis());

        try (CloseableHttpClient client = httpClientBuilder.build()) {
            int statusCode = -1;
            Instant start = now();
            while (start.plus(Duration.ofSeconds(30)).isAfter(now())) {
                try {
                    statusCode = client.execute(get).getStatusLine().getStatusCode();
                } catch (HttpHostConnectException e) {
                    System.out.println("Connection refused. The servlet container seems not be ready yet.");
                }
                System.out.println("Current status Code: " + statusCode);
                if (statusCode == SC_OK || statusCode == SC_UNAUTHORIZED) {
                    task.cancel();
                    return statusCode;
                }
                System.out.println("Sleeping for " + interval + " millis.");
                MILLISECONDS.sleep(interval);
            }

            fail(String.format("After %d ms, status code was %d", duration, statusCode));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to connect to '" + url + "'.");
        } finally {
            get.releaseConnection();
        }
        throw new RuntimeException("Failed to connect to '" + url + "'.");
    }

    public static void main(String[] args) throws Exception {
        checkHttpPage("Checking splash screen...", "http://localhost:8080/");
        checkHttpPage("Checking admin screen...", "http://localhost:8080/admin");
        checkHttpPage("Checking admin login with (admin/admin)...", "http://localhost:8080/admin/content/overview", "admin", "admin");
        checkHttpPage("Checking admin login with (foo/bar)...", "http://localhost:8080/admin/content/overview", "foo", "bar");
    }

    private static void checkHttpPage(String message, String url) {
        System.out.print(message);
        int returnCode = UrlWaitLatch.waitFor(url);
        System.out.println(returnCode);
    }

    private static void checkHttpPage(String message, String url, String username, String password) {
        System.out.print(message);
        int returnCode = UrlWaitLatch.waitFor(url, username, password);
        System.out.println(returnCode);
    }
}
