package com.uservoice;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public abstract class Test {
    private Map<String, String> configuration = null;

    protected String config(String name) {
        return config(name, null);
    }

    @SuppressWarnings("unchecked")
    protected String config(String name, String defaultValue) {
        if (configuration == null) {
            try {
                configuration = new Yaml().loadAs(new FileReader("config.yml"), Map.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        if (configuration.get(name) == null) {
            return defaultValue;
        }
        return configuration.get(name);
    }
}
