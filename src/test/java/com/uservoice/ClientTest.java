package com.uservoice;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class ClientTest {
    private Map<String, String> configuration = null;

    @SuppressWarnings("unchecked")
    private String config(String name) {
        if (configuration == null) {
            try {
                configuration = new Yaml().loadAs(new FileReader("config.yml"), Map.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return configuration.get(name);
    }

    private Client getConfiguredClient() {
        return new Client(config("subdomain_name"), config("api_key"), config("api_secret"), null, null, null,
                config("uservoice_domain"), config("uservoice_protocol"));
    }

    @Test
    public void shouldGet10Users() throws APIError {
        Client client = getConfiguredClient();
        JSONObject result = client.get("/api/v1/users");
        assertEquals(10, result.getJSONArray("users").size());
    }

    @Test
    public void shouldLoginAsOwner() throws APIError {
        Client client = getConfiguredClient();
        JSONObject result = client.loginAsOwner().get("/api/v1/users/current");
        assertEquals(true, result.getJSONObject("user").getJSONObject("roles").getBoolean("owner"));
    }

    @Test
    public void shouldLoginAsRegularUser() throws APIError, IOException {
        Client client = getConfiguredClient();
        JSONObject result = client.loginAs("man.with.only.answers@example.com").get("/api/v1/users/current");
        assertEquals(false, result.getJSONObject("user").getJSONObject("roles").getBoolean("owner"));
    }

}
