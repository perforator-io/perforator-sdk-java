/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.codeless.actions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public interface CrawlerQueue {

    public static final int MAX_QUEUE_SIZE = 4096;

    public static CrawlerQueue newInstance(Collection<String> domains, boolean randomized, int maxVisitsOverall, int maxVisitsPerUrl) {
        return newInstance(domains, randomized, MAX_QUEUE_SIZE, maxVisitsOverall, maxVisitsPerUrl);
    }

    public static CrawlerQueue newInstance(Collection<String> domains, boolean randomized, int maxQueueSize, int maxVisitsOverall, int maxVisitsPerUrl) {
        if (maxQueueSize <= 0) {
            throw new IllegalArgumentException("maxQueueSize should be greater than 0");
        }

        if (maxVisitsOverall <= 0) {
            throw new IllegalArgumentException("maxVisitsOverall should be greater than 0");
        }

        if (maxVisitsPerUrl <= 0) {
            throw new IllegalArgumentException("maxVisitsPerUrl should be greater than 0");
        }

        if (domains == null || domains.isEmpty()) {
            throw new IllegalArgumentException("domains are required");
        }

        if (randomized) {
            return new RandomizedCrawlerQueue(domains, maxQueueSize, maxVisitsOverall, maxVisitsPerUrl);
        } else {
            return new NaturalOrderCrawlerQueue(domains, maxQueueSize, maxVisitsOverall, maxVisitsPerUrl);
        }
    }

    default void pushAll(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }

        for (String url : urls) {
            push(url);
        }
    }

    int size();

    void push(String url);

    String poll();

    void destroy();

    static abstract class AbstractCrawlerQueue<T extends Collection<String>> implements CrawlerQueue {

        protected final T urls;
        protected final Set<String> domains;
        protected final Map<String, Integer> urlsCounter;
        protected final int maxQueueSize;
        protected final int maxVisitsOverall;
        protected final int maxVisitsPerUrl;
        protected int overallCounter = 0;

        public AbstractCrawlerQueue(Supplier<T> storageProvider, Collection<String> domains, int maxQueueSize, int maxVisitsOverall, int maxVisitsPerUrl) {
            this.urls = storageProvider.get();
            this.domains = new HashSet<>();
            this.urlsCounter = new HashMap<>();
            this.maxQueueSize = maxQueueSize;
            this.maxVisitsOverall = maxVisitsOverall;
            this.maxVisitsPerUrl = maxVisitsPerUrl;

            for (String domain : domains) {
                if (domain == null || domain.isEmpty()) {
                    continue;
                }

                this.domains.add(domain.trim().toLowerCase().intern());
            }
        }

        protected abstract String doPoll();

        protected void compact() {
            urls.removeIf(
                    url -> urlsCounter.getOrDefault(url, 0) >= maxVisitsPerUrl
            );
        }

        @Override
        public void destroy() {
            this.urls.clear();
            this.domains.clear();
            this.urlsCounter.clear();
        }

        @Override
        public int size() {
            return urls.size();
        }

        @Override
        public void push(String url) {
            if (overallCounter >= maxVisitsOverall) {
                return;
            }

            if (urls.size() >= maxQueueSize) {
                return;
            }

            if (url == null) {
                return;
            }

            url = url.trim();
            if (url.length() == 0) {
                return;
            }

            int urlCounter = urlsCounter.getOrDefault(url, 0);
            if (urlCounter >= maxVisitsPerUrl) {
                return;
            }

            URL parsedURL = parseURL(url);
            if (parsedURL == null) {
                return;
            }

            String protocol = parsedURL.getProtocol();
            if (protocol == null || protocol.isBlank()) {
                return;
            }

            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                return;
            }

            String host = parsedURL.getHost();
            if (host == null || host.isBlank()) {
                return;
            }

            host = host.trim().toLowerCase().intern();
            if (!domains.contains(host)) {
                return;
            }

            urls.add(url);
        }

        @Override
        public String poll() {
            if (overallCounter >= maxVisitsOverall) {
                urls.clear();
                return null;
            }

            int urlCounter = 0;
            String result = null;

            while ((result = doPoll()) != null) {
                urlCounter = urlsCounter.getOrDefault(result, 0);

                if (urlCounter < maxVisitsPerUrl) {
                    break;
                } else {
                    compact();
                }
            }

            if (result == null) {
                return null;
            }

            urlsCounter.put(result, urlCounter + 1);
            overallCounter += 1;

            return result;
        }

        private static URL parseURL(String url) {
            try {
                return new URI(url).toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                return null;
            }
        }

    }

    static class NaturalOrderCrawlerQueue extends AbstractCrawlerQueue<LinkedList<String>> {

        private NaturalOrderCrawlerQueue(Collection<String> domains, int maxQueueSize, int maxVisitsOverall, int maxVisitsPerUrl) {
            super(LinkedList::new, domains, maxQueueSize, maxVisitsOverall, maxVisitsPerUrl);
        }

        @Override
        protected String doPoll() {
            return urls.poll();
        }

    }

    static class RandomizedCrawlerQueue extends AbstractCrawlerQueue<ArrayList<String>> {

        private final SplittableRandom random = new SplittableRandom();

        private RandomizedCrawlerQueue(Collection<String> domains, int maxQueueSize, int maxVisitsOverall, int maxVisitsPerUrl) {
            super(ArrayList::new, domains, maxQueueSize, maxVisitsOverall, maxVisitsPerUrl);
        }

        @Override
        protected String doPoll() {
            int size = urls.size();

            if (size == 0) {
                return null;
            }

            int index = random.nextInt(size);
            String result = urls.get(index);
            urls.set(index, urls.get(size - 1));
            urls.remove(size - 1);

            return result;
        }

    }

}
