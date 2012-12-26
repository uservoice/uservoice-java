UserVoice Java library for API connections
========================================

This library allows you to easily:
* Do 3-legged and 2-legged UserVoice API calls safely without having to worry about the cryptographic details.

Installation
============

Compile from source by running in the application root
```sh
mvn package
```

Now you should be good to go!

Examples
========

Prerequisites:

```java
// Suppose your UserVoice site is at http://uservoice-subdomain.uservoice.com/
final String USERVOICE_SUBDOMAIN = "uservoice-subdomain";
final String SSO_KEY = "982c88f2df72572859e8e23423eg87ed"; // Admin Console -> Settings -> General -> User Authentication

// Define an API client at: Admin Console -> Settings -> Channels -> API
final String API_KEY = "oQt2BaunWNuainc8BvZpAm";
final String API_SECRET = "3yQMSoXBpAwuK3nYHR0wpY6opE341inL9a2HynGF2";

```

SSO-token generation using uservoice library
--------------------------------------------

SSO-token can be used to create sessions for SSO users. They are capable of synchronizing the user information from one system to another.
Generating the SSO token from SSO key and given uservoice subdomain can be done by calling com.uservoice.SSO.GenerateToken method like this:

```java
    String ssoToken = com.uservoice.SSO.generateToken(USERVOICE_SUBDOMAIN, SSO_KEY, new HashMap<String, String>() {{
        put("display_name, "John Doe");
        put("email", "john.doe@example.com");
    }}, 5*60); // the token will be valid for 5 minutes (5*60 seconds) by default

    System.out.writeln("https://" + USERVOICE_SUBDOMAIN + ".uservoice.com/?sso=" + ssoToken);
```

Making API calls
----------------

You need to create an instance of UserVoice\\Client. Get API_KEY and API_SECRET for an API client which you can create
from Admin Console. Go to Settings -> Channels -> API.

```java

try {
    com.uservoice.Client client = new com.uservoice.Client(USERVOICE_SUBDOMAIN, API_KEY, API_SECRET);

    // Get users of a subdomain (requires trusted client, but no user)
    com.uservoice.Collection users = client.loginAsOwner().getCollection("/api/v1/users");

    System.out.println("Subdomain \"" + USERVOICE_SUBDOMAIN + "\" has " + users.size() + " users.");

    foreach (JSONObject user in users) {
        System.out.println("User: \"" + user.getString("name") + "\", Profile URL: " + user.getString("url"));
    }

    // Now, let's login as mailaddress@example.com, a regular user
    com.uservoice.Client regularAccessToken = client.loginAs("mailaddress@example.com");

    // Example request #1: Get current user.
    JSONObject regularUser = regularAccessToken.get("/api/v1/users/current").getJSONObject("user");

    System.out.println("User: \"" + regularUser.getString("name") + "\", Profile URL: " + regularUser.getString("url"));

    // Login as account owner
    com.uservoice.Client ownerAccessToken = client.loginAsOwner();

    // Example request #2: Create a new private forum limited to only example.com email domain.
    JSONObject forum = ownerAccessToken.Post("/api/v1/forums", new HashMap<String, String>() {{
        forum = new HashMap<String, String>() {{
            put("name", "Java Client Private Feedback");
            put("private", true);
            put("allow_by_email_domain", true);
            put("allowed_email_domains" = new ArrayList< Map<String,String> {{
                add(new HashMap<String, String>() {{
		    put("domain", "example.com");
		}});
            }})
        }}
    }}).getJSONObject("forum");

    System.out.println("Forum \"" + forum.getString("name") + "\" created! URL: " + forum.getString("url"));
} catch (com.uservoice.Unauthorized e) {
    /* Thrown usually due to faulty tokens, untrusted client or if attempting
     * operations without Admin Privileges
     */
    System.out.println(e);
} catch (com.uservoice.NotFound e) {
    // Thrown when attempting an operation to a resource that does not exist
    System.out.println(e);
}

```

Verifying a UserVoice user
--------------------------

If you want to make calls on behalf of a user, but want to make sure he or she
actually owns certain email address in UserVoice, you need to use 3-Legged API
calls. Just pass your user an authorize link to click, so that user may grant
your site permission to access his or her data in com.uservoice.

```java

final String callbackURL = "http://localhost:3000/"; // your site

com.uservoice.Client client = new com.uservoice.Client(USERVOICE_SUBDOMAIN, API_KEY, API_SECRET, callbackURL);

// At this point you want to print/redirect to client.AuthorizeURL in your application.
// Here we just output them as this is a command-line example.
System.out.println("1. Go to " + client.authorizeUrl() + " and click \"Allow access\".");
System.out.println("2. Then type the oauth_verifier which was passed to the callback URL:");

// In a web app we would get the oauth_verifier via a redirection to CALLBACK_URL.
// In this command-line example we just read it from stdin:
com.uservoice.Client accessToken = client.loginWithVerifier(Console.ReadLine());

// All done. Now we can read the current user's email address:
JSONObject user = accessToken.get("/api/v1/users/current").getJSONObject("user");


System.out.println("User logged in, Name: " + user.getString("name") + ", email: " + user.getString("email"));

```