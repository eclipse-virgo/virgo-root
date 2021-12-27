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

package org.eclipse.virgo.repository.internal.remote;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;


final class ArtifactDescriptorStoreWatcher {

    private final StoreUpdaterThread storeUpdaterThread;

    ArtifactDescriptorStoreWatcher(MutableArtifactDescriptorDepository mutableDepository, RemoteRepositoryConfiguration configuration) {
        this.storeUpdaterThread = new StoreUpdaterThread(mutableDepository, configuration);
    }

    void start() {
        this.storeUpdaterThread.start();
    }

    void stop() {
        this.storeUpdaterThread.stopUpdates();
    }

    static final class StoreUpdaterThread extends Thread {

        private static final Logger LOGGER = LoggerFactory.getLogger(StoreUpdaterThread.class);

        private static final String RESPONSE_HEADER_ETAG = "Etag";

        private static final String RESPONSE_HEADER_CONTENT_ENCODING = "Content-Encoding";

        private static final String CONTENT_ENCODING_GZIP = "gzip";

        private static final String REQUEST_HEADER_IF_NONE_MATCH = "If-None-Match";

        private final CloseableHttpClient httpClient = HttpClients.createDefault();

        private final long msUpdateInterval;

        private final String repositoryUri;

        private final MutableArtifactDescriptorDepository mutableDepository;

        private final DescriptorStoreFactory descriptorStoreFactory;

        private final String repositoryName;

        private volatile boolean update = true;

        private int countContiguousHttpClientFailures = 0;

        private StoreUpdaterThread(MutableArtifactDescriptorDepository mutableDepository, RemoteRepositoryConfiguration configuration) {
            super(configuration.getName());
            this.msUpdateInterval = configuration.getIndexUpdateInterval() * 1000L;
            this.repositoryUri = configuration.getRepositoryUri().toString();
            this.mutableDepository = mutableDepository;
            this.repositoryName = configuration.getName();
            this.descriptorStoreFactory = new DescriptorStoreFactory(this.repositoryName, configuration.getIndexLocation().getParentFile());
            setDaemon(true);
        }

        @Override
        public void run() {

            DescriptorStore descriptorStore = descriptorStoreFactory.recoverDescriptorStore();

            while (this.update) {
                HttpGet getIndex = new HttpGet(this.repositoryUri);

                if (descriptorStore != null) {
                    getIndex.addHeader(REQUEST_HEADER_IF_NONE_MATCH, descriptorStore.getEtag());
                }

                int responseCode;
                try {
                    CloseableHttpResponse response = httpClient.execute(getIndex);
                    responseCode = response.getStatusLine().getStatusCode();
                    if (this.countContiguousHttpClientFailures > 0) {
                        LOGGER.info(String.format("Remote repository '%s' re-accessed after failure.", this.repositoryName));
                        this.countContiguousHttpClientFailures = 0;
                    }
                    if (responseCode == HttpStatus.SC_OK || responseCode == HttpStatus.SC_NOT_MODIFIED) {
                        if (responseCode == HttpStatus.SC_OK) {
                            descriptorStore = readNewDescriptorStore(response);
                        }
                        this.mutableDepository.setDescriptorStore(descriptorStore);
                    } else {
                        handleUnexpectedResponse(responseCode);
                    }
                } catch (IOException ioe) {
                    if (0 == this.countContiguousHttpClientFailures) {
                        LOGGER.warn(String.format("Remote repository '%s' inaccessible.", this.repositoryName), ioe);
                    } else if (5 > this.countContiguousHttpClientFailures) {
                        this.countContiguousHttpClientFailures = 0;
                        LOGGER.warn(String.format("Remote repository '%s' inaccessible.", this.repositoryName));
                    }
                    ++this.countContiguousHttpClientFailures;
                }

                try {
                    Thread.sleep(this.msUpdateInterval);
                } catch (InterruptedException ie) {
                    LOGGER.info("Interrupted. Stopping updates");
                    this.update = false;
                }
            }
        }

        private void stopUpdates() {
            this.update = false;
            this.interrupt();
        }

        private void handleUnexpectedResponse(int responseCode) {
            LOGGER.error(String.format("Unexpected HTTP response code: %s from remote repository '%s'.", String.valueOf(responseCode),
                this.repositoryName));
        }

        private String getETag(CloseableHttpResponse response) {
            Header responseHeader = response.getFirstHeader(RESPONSE_HEADER_ETAG);
            return responseHeader == null ? null : responseHeader.getValue();
        }

        private DescriptorStore readNewDescriptorStore(CloseableHttpResponse response) {
            try {
                String etag = getETag(response);
                
                InputStream storeStream = response.getEntity().getContent();
                
                Header contentEncodingResponseHeader = response.getFirstHeader(RESPONSE_HEADER_CONTENT_ENCODING);
                if (contentEncodingResponseHeader != null && CONTENT_ENCODING_GZIP.equals(contentEncodingResponseHeader.getValue())) {
                    storeStream = new GZIPInputStream(storeStream);
                }

                return descriptorStoreFactory.createDescriptorStore(storeStream, etag);
            } catch (IOException ioe) {
                LOGGER.error(String.format("Copying index failed for remote repository '%s'.", this.repositoryName), ioe);
                return null;
            }
        }

    }
}
