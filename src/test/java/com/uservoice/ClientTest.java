package com.uservoice;

import static org.junit.Assert.*;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.junit.Test;

public class ClientTest extends com.uservoice.Test {

    protected Client getTrustedClient() {
        return new Client(config("subdomain_name"), config("api_key"), config("api_secret"), null, null, null,
                config("uservoice_domain"), config("uservoice_protocol"));
    }

    protected Client getUnauthorizedClient() {
        return new Client(config("subdomain_name"), config("api_key"), null, null, null, null,
                config("uservoice_domain"), config("uservoice_protocol"));
    }

    @Test
    public void shouldGet10Users() throws APIError {
        Client client = getTrustedClient();
        JSONObject result = client.get("/api/v1/users");
        assertEquals(10, result.getJSONArray("users").size());
    }

    @Test
    public void shouldGetSuggestionsAsUnauthorized() throws APIError {
        Client client = getUnauthorizedClient();
        JSONObject result = client.get("/api/v1/suggestions");
        assertEquals(10, result.getJSONArray("suggestions").size());
    }

    @Test
    public void shouldLoginAsOwner() throws APIError {
        Client client = getTrustedClient();
        JSONObject result = client.loginAsOwner().get("/api/v1/users/current");
        assertEquals(true, result.getJSONObject("user").getJSONObject("roles").getBoolean("owner"));
    }

    @Test
    public void shouldLoginAsRegularUser() throws APIError, IOException {
        Client client = getTrustedClient();
        JSONObject result = client.loginAs("man.with.only.answers@example.com").get("/api/v1/users/current");
        assertEquals(false, result.getJSONObject("user").getJSONObject("roles").getBoolean("owner"));
    }

    @Test
    public void shouldGet102Users() {
        assertEquals(102, getTrustedClient().getCollection("/api/v1/users", 102).size());
    }
}
