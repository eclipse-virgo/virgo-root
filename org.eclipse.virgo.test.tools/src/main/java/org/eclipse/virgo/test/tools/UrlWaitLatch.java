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

import static org.junit.Assert.fail;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

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
        CloseableHttpClient client = HttpClients.createDefault();
        return wait(url, client, null, interval, duration);
    }

    public static int waitFor(String url, String username, String password, long interval, long duration) {
        CloseableHttpClient client = HttpClients.createDefault();
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope("localhost", AuthScope.ANY_PORT), 
            new UsernamePasswordCredentials("admin", "admin"));
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(25).build();
		context.setRequestConfig(requestConfig);
        return wait(url, client, context, interval, duration);
    }

    private static int wait(String url, HttpClient client, HttpClientContext context, long interval, long duration) {
        final HttpGet get = new HttpGet(url);

        int hardTimeoutInSeconds = 75;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (get != null) {
                	System.err.println("Operation timed out. Aborting request.");
                    get.abort();
                }
            }
        };
        new Timer(true).schedule(task, hardTimeoutInSeconds * 1000);
        try {
            long startTime = System.currentTimeMillis();
            int statusCode = 999;
            while (System.currentTimeMillis() - startTime < duration) {
            	if (context != null) {
            		statusCode = client.execute(get).getStatusLine().getStatusCode();
				} else {
					statusCode = client.execute(get, context).getStatusLine().getStatusCode();
				}
                if (statusCode != 200 || statusCode != 404) {
                	task.cancel();
                    return statusCode;
                }
                System.out.println("Current status Code: " + statusCode);
                Thread.sleep(interval);
            }

            fail(String.format("After %d ms, status code was %d", duration, statusCode));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            get.releaseConnection();
        }
        throw new RuntimeException("Failed to connect to '" + url + "'.");
    }

    public static void main(String[] args) {
		checkHttpPage("Checking splash screen...", "http://localhost:8080/");
		checkHttpPage("Checking admin screen...", "http://localhost:8080/admin");
		checkHttpPage("Checking admin login...", "http://localhost:8080/admin/content/overview", "admin", "admin");
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
