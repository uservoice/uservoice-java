package com.uservoice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.junit.Ignore;
import org.junit.Test;

public class ReadmeTest extends com.uservoice.Test {
    private final String USERVOICE_SUBDOMAIN;
    private final String API_KEY;
    private final String API_SECRET;
    private final String SSO_KEY;

    public ReadmeTest () {
        USERVOICE_SUBDOMAIN = config("subdomain_name");
        API_KEY = config("api_key");
        API_SECRET = config("api_secret");
        SSO_KEY = config("sso_key");
    }

    @Test
    @Ignore
    public void shouldGenerateSSOToken() throws APIError, IOException {
        String ssoToken = com.uservoice.SSO.generateToken(USERVOICE_SUBDOMAIN, SSO_KEY, new HashMap<String, Object>() {
            {
                put("display_name", "John Doe");
                put("email", "john.doe@example.com");
            }
        }, 5 * 60); // the token will be valid for 5 minutes (5*60 seconds) by
                    // default

        System.out.println("https://" + USERVOICE_SUBDOMAIN + ".uservoice.com/?sso=" + ssoToken);
    }

    @Test
    @Ignore
    public void shouldMakeAPICalls() throws APIError {
        try {
            com.uservoice.Client client = new com.uservoice.Client(USERVOICE_SUBDOMAIN, API_KEY, API_SECRET, null,
                    null, null, config("uservoice_domain"));

            // Get users of a subdomain (requires trusted client, but no user)
            com.uservoice.Collection users = client.loginAsOwner().getCollection("/api/v1/users", 300);

            System.out.println("Subdomain \"" + USERVOICE_SUBDOMAIN + "\" has " + users.size() + " users.");

            for (JSONObject user : users) {
                System.out.println("User: \"" + user.getString("name") + "\", Profile URL: " + user.getString("url"));
            }

            // Now, let's login as mailaddress@example.com, a regular user
            com.uservoice.Client regularAccessToken = client.loginAs("mailaddress@example.com");

            // Example request #1: Get current user.
            JSONObject user = regularAccessToken.get("/api/v1/users/current").getJSONObject("user");

            System.out.println("User: \"" + user.getString("name") + "\", Profile URL: " + user.getString("url"));

            // Login as account owner
            com.uservoice.Client ownerAccessToken = client.loginAsOwner();

            // Example request #2: Create a new private forum limited to only
            // example.com email domain.
            JSONObject forum = ownerAccessToken.post("/api/v1/forums", new HashMap<String, Object>() {
                {
                    put("forum", new HashMap<String, Object>() {
                        {
                            put("name", "Java Client Private Feedback");
                            put("private", true);
                            put("allow_by_email_domain", true);
                            put("allowed_email_domains", new ArrayList<Map<String, String>>() {
                                {
                                    add(new HashMap<String, String>() {
                                        {
                                            put("domain", "example.com");
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }).getJSONObject("forum");

            System.out.println("Forum \"" + forum.getString("name") + "\" created! URL: " + forum.getString("url"));
        } catch (com.uservoice.Unauthorized e) {
            /*
             * Thrown usually due to faulty tokens, untrusted client or if
             * attempting operations without Admin Privileges
             */
            System.out.println(e);
        } catch (com.uservoice.NotFound e) {
            // Thrown when attempting an operation to a resource that does not
            // exist
            System.out.println(e);
        }

    }

    @Test
    @Ignore
    public void shouldVerifyUser() throws APIError, IOException {
        final String callbackURL = "http://localhost:3000/"; // your site

        com.uservoice.Client client = new com.uservoice.Client(USERVOICE_SUBDOMAIN, API_KEY, API_SECRET, callbackURL,
                null, null, config("uservoice_domain"));

        // At this point you want to print/redirect to client.AuthorizeURL in
        // your application.
        // Here we just output them as this is a command-line example.
        System.out.println("1. Go to " + client.authorizeUrl() + " and click \"Allow access\".");
        System.out.println("2. Then type the oauth_verifier which was passed to the callback URL:");

        // In a web app we would get the oauth_verifier via a redirection to
        // CALLBACK_URL.
        // In this command-line example we just read it from stdin:
        String verifier = new BufferedReader(new InputStreamReader(System.in)).readLine();
        com.uservoice.Client accessToken = client.loginWithVerifier(verifier);

        // All done. Now we can read the current user's email address:
        JSONObject user = accessToken.get("/api/v1/users/current").getJSONObject("user");

        System.out.println("User logged in, Name: " + user.getString("name") + ", email: " + user.getString("email"));

    }

}
