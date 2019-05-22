/* Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. 
*                                                              
* Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl. 
*/
package com.oracle.weblogic.imagetool.impl.meta;

import com.oracle.weblogic.imagetool.api.meta.CacheStore;
import com.oracle.weblogic.imagetool.util.Constants;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PreferenceCacheStore implements CacheStore {

    CACHE_STORE;

    private final String DEFAULT_CACHE_DIR = Paths.get(System.getProperty("user.home"), "cache")
            .toAbsolutePath().toString();
    private final Preferences preferences = Preferences.userRoot().node(Constants.WEBLOGIC_IMAGETOOL);

    PreferenceCacheStore() {
        try {
            if (preferences.get(Constants.CACHE_DIR_KEY, null) == null) {
                preferences.put(Constants.CACHE_DIR_KEY, DEFAULT_CACHE_DIR);
                persistToDisk();
            }
            File cacheDir = new File(preferences.get(Constants.CACHE_DIR_KEY, DEFAULT_CACHE_DIR));
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public String getCacheDir() {
        return preferences.get(Constants.CACHE_DIR_KEY, DEFAULT_CACHE_DIR);
    }

    @Override
    public String getValueFromCache(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        return preferences.get(key.toLowerCase(), null);
    }

    @Override
    public boolean hasMatchingKeyValue(String key, String value) {
        if (key == null || value == null) {
            return false;
        }
        return value.equals(preferences.get(key.toLowerCase(), null));
    }

    @Override
    public boolean addToCache(String key, String value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "Cache item value cannot be null");
        preferences.put(key.toLowerCase(), value);
        return persistToDisk();
    }

    @Override
    public String deleteFromCache(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        if (Constants.CACHE_DIR_KEY.equals(key.toLowerCase())) {
            return null;
        }
        String oldValue = preferences.get(key.toLowerCase(), null);
        if (oldValue != null) {
            preferences.remove(key.toLowerCase());
            persistToDisk();
        }
        return oldValue;
    }

    @Override
    public Map<String, String> getCacheItems() {
        try {
            Stream<String> stream = Stream.of(preferences.keys());
            return stream.collect(Collectors.toMap(e -> e, e -> preferences.get(e, null)));
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private boolean persistToDisk() {
        boolean retVal = true;
        synchronized (preferences) {
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
                retVal = false;
            }
        }
        return retVal;
    }

    public boolean setCacheDir(String cacheDirPath) {
        if (cacheDirPath != null && !cacheDirPath.equals(getCacheDir())) {
            preferences.put(Constants.CACHE_DIR_KEY, cacheDirPath);
            return persistToDisk();
        }
        return false;
    }

}