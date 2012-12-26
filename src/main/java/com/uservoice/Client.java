package com.uservoice;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class Client {

	private String serverLocation;
	private OAuthService service;
	private Token requestToken;
    private Token token;


    public Client(String subdomainName, String apiKey) {
        this(subdomainName, apiKey, null);
    }

    public Client(String subdomainName, String apiKey, String apiSecret) {
        this(subdomainName, apiKey, apiSecret, null);
    }

    public Client(String subdomainName, String apiKey, String apiSecret, String callback) {
        this(subdomainName, apiKey, apiSecret, callback, "", "");
    }

    public Client(String subdomainName, String apiKey, String apiSecret, String callback, String token, String secret) {
        this(subdomainName, apiKey, apiSecret, callback, token, secret, "uservoice.com");
    }

    public Client(String subdomainName, String apiKey, String apiSecret, String callback, String token, String secret, String uservoiceDomain) {
        this(subdomainName, apiKey, apiSecret, callback, token, secret, uservoiceDomain, "https");
    }

    public Client(String subdomainName, String apiKey, String apiSecret, String callback, String token, String secret, String uservoiceDomain, String protocol) {
        this(getValueOrDefault(protocol, "https") + "://" + subdomainName + "."
                + getValueOrDefault(uservoiceDomain, "uservoice.com"), new ServiceBuilder()
                .provider(
                        new UserVoiceApi(getValueOrDefault(protocol, "https") + "://" + subdomainName + "."
                                + getValueOrDefault(uservoiceDomain, "uservoice.com"))).apiKey(apiKey)
                .apiSecret(apiSecret).build(), new Token(getValueOrDefault(token, ""), getValueOrDefault(secret, "")));
    }

    private static String getValueOrDefault(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Client(String serverLocation, OAuthService service, Token token) {
        this.serverLocation = serverLocation;
        this.service = service;
        this.token = token;
    }

    public Client loginWithVerifier(String verifier) {
        Verifier v = new Verifier(verifier);
        Token accessToken = service.getAccessToken(requestToken, v);
        return new Client(serverLocation, service, accessToken);
    }
	public String authorizeUrl() {
		requestToken = service.getRequestToken();
		return service.getAuthorizationUrl(requestToken);
	}

    public Client loginWithAccessToken(String token, String secret) {
        return new Client(serverLocation, service, new Token(token, secret));
	}

    public Client loginAsOwner() throws APIError {
        requestToken = service.getRequestToken();
        JSONObject token = post("/api/v1/users/login_as_owner",
                new JSONObject().element("request_token", requestToken.getToken()));
        if (token != null && !token.getJSONObject("token").isNullObject()) {
            return loginWithAccessToken(token.getJSONObject("token").getString("oauth_token"),
                    token.getJSONObject("token").getString("oauth_token_secret"));
        } else {
            throw new Unauthorized("Could not get Request Token");
        }
    }

    @SuppressWarnings({ "serial" })
    public Client loginAs(final String email) throws APIError {
        requestToken = service.getRequestToken();
        JSONObject token = post("/api/v1/users/login_as", new HashMap<String, Object>() {
            {
                put("request_token", requestToken.getToken());
                put("user", new HashMap<String, String>() {
                    {
                        put("email", email);
                    }
                });
            }
        });
        if (token != null && !token.getJSONObject("token").isNullObject()) {
            return loginWithAccessToken(token.getJSONObject("token").getString("oauth_token"),
                    token.getJSONObject("token").getString("oauth_token_secret"));
        } else {
            throw new Unauthorized("Could not get Request Token");
        }
    }

    public JSONObject request(Verb method, String path, Map<String, Object> params) throws APIError {
        OAuthRequest request = new OAuthRequest(method, serverLocation + path);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        if (params != null) {
            request.addPayload(JSONObject.fromObject(params).toString());
        }
        service.signRequest(token, request);
        Response response = request.send();
        JSONObject result = JSONObject.fromObject(response.getBody());
        if (result != null && !result.getJSONObject("errors").isNullObject()) {
            String errorType = result.getJSONObject("errors").getString("type");
            if ("unauthorized".equals(errorType)) {
                throw new Unauthorized(result.getJSONObject("errors").getString("message"));
            } else if ("record_not_found".equals(errorType)) {
                throw new NotFound(result.getJSONObject("errors").getString("message"));
            } else if ("application_error".equals(errorType)) {
                throw new ApplicationError(result.getJSONObject("errors").getString("message"));
            } else {
                throw new APIError(result.getJSONObject("errors").getString("message"));
            }
        }

        return result;
	}

    public JSONObject get(String path) throws APIError {
        return request(Verb.GET, path, null);
    }

    public JSONObject delete(String path) throws APIError {
        return request(Verb.DELETE, path, null);
    }

    public JSONObject post(String path, Map params) throws APIError {
        return request(Verb.POST, path, params);
    }

    public JSONObject put(String path, Map params) throws APIError {
        return request(Verb.PUT, path, params);
    }

}