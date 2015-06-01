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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

        private static final String RESPONSE_HEADER_ETAG = "Etag";
        
        private static final String REPONSE_HEADER_CONTENT_ENCODING = "Content-Encoding";
        
        private static final String CONTENT_ENCODING_GZIP = "gzip";

        private static final String REQUEST_HEADER_IF_NONE_MATCH = "If-None-Match";

        private static final Logger LOGGER = LoggerFactory.getLogger(StoreUpdaterThread.class);

        private final HttpClient httpClient = new HttpClient();

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
                GetMethod getIndex = new GetMethod(this.repositoryUri);

                if (descriptorStore != null) {
                    getIndex.addRequestHeader(REQUEST_HEADER_IF_NONE_MATCH, descriptorStore.getEtag());
                }

                int responseCode;
                try {
                    responseCode = this.httpClient.executeMethod(getIndex);
                    if (this.countContiguousHttpClientFailures > 0) {
                        LOGGER.info(String.format("Remote repository '%s' re-accessed after failure.", this.repositoryName));
                        this.countContiguousHttpClientFailures = 0;
                    }
                    if (responseCode == HttpStatus.SC_OK || responseCode == HttpStatus.SC_NOT_MODIFIED) {
                        if (responseCode == HttpStatus.SC_OK) {
                            descriptorStore = readNewDescriptorStore(getIndex);
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

        private String getETag(GetMethod getIndex) {
            Header responseHeader = getIndex.getResponseHeader(RESPONSE_HEADER_ETAG);
            return responseHeader == null ? null : responseHeader.getValue();
        }

        private DescriptorStore readNewDescriptorStore(GetMethod getDescriptorStore) {
            try {
                String etag = getETag(getDescriptorStore);
                
                InputStream storeStream = getDescriptorStore.getResponseBodyAsStream();
                
                Header contentEncodingResponseHeader = getDescriptorStore.getResponseHeader(REPONSE_HEADER_CONTENT_ENCODING);
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
