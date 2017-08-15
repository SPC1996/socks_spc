package com.keessi.socks.config;


import java.io.*;
import java.util.*;

public final class Config {
    private class OrderProperties extends Properties {
        private static final long serialVersionUID = 1L;
        private final Set<Object> keys = new LinkedHashSet<>();

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(keys);
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        @Override
        public Set<Object> keySet() {
            return keys;
        }

        @Override
        public Set<String> stringPropertyNames() {
            Set<String> set = new LinkedHashSet<>();
            for (Object key : keys) {
                set.add((String) key);
            }
            return set;
        }
    }

    private static final String REMOTE_HOST = "127.0.0.1";
    private static final String REMOTE_PORT = "8888";
    private static final String LOCAL_PORT = "8080";
    private static final String LISTEN_PORT = "8888";
    private static final String ENCRYPT_KEY = "H5";
    private static final String USER = "spc";

    private final Properties properties = new OrderProperties();
    private static final Config ins = new Config();

    private Config() {
        properties.put("remoteHost", REMOTE_HOST);
        properties.put("remotePort", REMOTE_PORT);
        properties.put("encryptKey", ENCRYPT_KEY);
        properties.put("localPort", LOCAL_PORT);
        properties.put("listenPort", LISTEN_PORT);
        properties.put("user", USER);

        String configFilePath = getClass().getResource("/").getFile() + "local.properties";
        File configFile = new File(configFilePath);
        try {
            properties.load(new FileInputStream(configFilePath));
        } catch (IOException e) {
            System.out.println("Load config failed!");
            e.printStackTrace();
        }
        try {
            properties.store(new FileOutputStream(configFile), "Config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config ins() {
        return ins;
    }

    public final String remoteHost() {
        return properties.getProperty("remoteHost", REMOTE_HOST);
    }

    public final int remotePort() {
        return Integer.parseInt(properties.getProperty("remotePort", REMOTE_PORT));
    }

    public final int listenPort() {
        return Integer.parseInt(properties.getProperty("listenPort", LISTEN_PORT));
    }

    public final int encryptKey() {
        return Integer.parseInt(properties.getProperty("encryptKey", ENCRYPT_KEY), 16);
    }

    public final int localPort() {
        return Integer.parseInt(properties.getProperty("localPort", LOCAL_PORT));
    }

    public final String user() {
        return properties.getProperty("user", USER);
    }
}
