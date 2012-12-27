package com.uservoice;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class SSOTest extends com.uservoice.Test {

    @SuppressWarnings("serial")
    @Test
    public void shouldGenerateSSOToken() throws APIError {
        String token = SSO.getInstance().generateToken(config("subdomain_name"), config("sso_key"),
                new HashMap<String, Object>() {
            {
                put("email", "man.with.only.answers@example.com");
            }
        });
        System.out.println(config("protocol", "https") + "://" + config("subdomain_name") + "."
                + config("uservoice_domain", "uservoice.com")
                + "/?sso=" + token);
        assertTrue(token.length() > 9);
    }
}
