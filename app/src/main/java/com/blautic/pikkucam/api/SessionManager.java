package com.blautic.pikkucam.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SessionManager {
    private final SharedPreferences prefs;
    private final String USER_TOKEN = "user_token";
    private final String USER_NAME = "user_name";

    public SessionManager(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveAuthToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        String token  = prefs.getString(USER_TOKEN, "");
        if (token.isEmpty()) {
            return null;
        } else {
            return token;
        }
    }

    public void saveUserName(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_NAME, token);
        editor.apply();
    }

    public String getUserName() {
        String token  = prefs.getString(USER_NAME, "");
        if (token.isEmpty()) {
            return null;
        } else {
            return token;
        }
    }


}
