package com.uservoice;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

public class CollectionTest extends com.uservoice.Test {

    protected Collection getEmptyCollection(String path) {
        Client client = mock(Client.class);
        when(client.get(anyString())).thenReturn(
                new JSONObject().element("response_data", new JSONObject().element("total_records", 0)).element(
                        "users", new JSONArray()));
        return new Collection(client, path);
    }

    protected Collection getSingleElementCollection(String path) {
        Client client = mock(Client.class);
        when(client.get(anyString())).thenReturn(
                new JSONObject().element("response_data", new JSONObject().element("total_records", 1)).element(
                        "users",
                        new JSONArray().element(new JSONObject().element("email", "man.with.only.answers@example.com")
                                .element("id", 1))));
        return new Collection(client, path);
    }

    @Test
    public void shouldGetNoUsers() throws APIError {
        Collection users = getEmptyCollection("/api/v1/users");
        assertEquals(0, users.size());
    }

    @Test
    public void shouldLoopZeroTimes() {
        for (JSONObject user : getEmptyCollection("/api/v1/users")) {
            throw new RuntimeException("Should not iterate any elements but got " + user);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowOnAccess() {
        getEmptyCollection("/api/v1/users").get(0);
    }

    @Test
    public void shouldLoopOneTime() {
        int times = 0;
        for (JSONObject user : getSingleElementCollection("/api/v1/users")) {
            assertEquals(1, user.getInt("id"));
            times++;
        }
        if (times != 1) {
            throw new RuntimeException("Should iterate one element but itereated through " + times);
        }
    }

    @Test
    public void shouldGetLastElement() {
        Collection collection = getSingleElementCollection("/api/v1/users");
        assertEquals("man.with.only.answers@example.com", collection.get(collection.size() - 1).getString("email"));
    }

}
