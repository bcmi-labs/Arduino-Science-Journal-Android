package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.utils.StringUtils;

import org.json.JSONObject;

import java.util.Map;

public abstract class Auth0SignUpCall extends Auth0Call<Auth0SignUpCall.Response> {

    private final String mUsername;
    private final String mEmail;
    private final String mPassword;
    private final String mBirthday;

    public Auth0SignUpCall(final @NonNull Context context,
                           final @NonNull String username,
                           final @NonNull String email,
                           final @NonNull String password,
                           final @NonNull String birthday) {
        super(context);
        mUsername = username;
        mEmail = email;
        mPassword = password;
        mBirthday = birthday;
    }

    @Override
    protected String buildEndpoint() {
        return "/dbconnections/signup";
    }

    @Override
    protected void buildRequest(Map<String, String> params) {
        params.put("username", mUsername);
        params.put("email", mEmail);
        params.put("password", mPassword);
        if (!StringUtils.isEmpty(mBirthday)) {
            params.put("user_metadata[birthday]", mBirthday);
        }
        completeBuildRequest(params);
    }

    protected abstract void completeBuildRequest(Map<String, String> params);

    @Override
    protected void onCompleted(int code, JSONObject json) {
        if (code == 200) {
            final Response response = new Response();
            response.success = true;
            notifyResponse(response);
        } else if (code == 400) {
            final Response response = new Response();
            response.success = false;
            final String error = json.optString("code", "");
            switch (error) {
                case "user_exists":
                    response.code = Response.Code.USER_EXISTS;
                    break;
                case "invalid_password":
                    response.code = Response.Code.INVALID_PASSWORD;
                    break;
                case "consent":
                    response.code = Response.Code.CONSENT;
                    break;
                case "unconfirmed_teen":
                    response.success = true;
                    break;
                default:
                    response.code = Response.Code.UNKNOWN;
                    break;
            }
            notifyResponse(response);
        } else {
            notifyFailure(new Exception("Unexpected status code " + code + " with body: " + json));
        }
    }

    public static class Response {

        public boolean success;

        public Code code;

        public enum Code {

            USER_EXISTS, INVALID_PASSWORD, CONSENT, UNKNOWN

        }

    }

}
