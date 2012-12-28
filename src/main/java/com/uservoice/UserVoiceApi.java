package com.uservoice;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class UserVoiceApi extends DefaultApi10a {
    private String serverLocation;

    public UserVoiceApi(String serverLocation) {
        this.serverLocation = serverLocation;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return serverLocation + "/oauth/request_token";
    }

    @Override
    public String getAuthorizationUrl(Token token) {
        return serverLocation + "/oauth/authorize?oauth_token=" + token.getToken();
    }

    @Override
    public String getAccessTokenEndpoint() {
        return serverLocation + "/oauth/access_token";
    }
}
