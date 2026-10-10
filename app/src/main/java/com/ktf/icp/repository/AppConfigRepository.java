package com.ktf.icp.repository;

import android.content.Context;

import com.ktf.config.Config;

public class AppConfigRepository {
    private static final String SET_FILE = "set.toml";
    public static final String KEY_APP_KEY = "APP_KEY";
    public static final String KEY_APP_SECRET = "APP_SECRET";
    public static final String KEY_ACCESS_TOKEN = "accessToken";
    public static final String KEY_REFRESH_TOKEN = "refreshToken";
    public static final String KEY_EXPIRES_AT = "expiresAt";
    public static final String KEY_PENDING_STATE = "oauthState";

    private final Config config;

    public AppConfigRepository(Context context) {
        this.config = new Config(context.getFilesDir().toPath().resolve(SET_FILE).toString());
    }

    public String get(String key) {
        return config.getConfig(key);
    }

    public boolean set(String key, String value) {
        return config.setConfig(key, value);
    }

    public boolean hasDropboxSetup() {
        String refresh = get(KEY_REFRESH_TOKEN);
        return refresh != null && !refresh.isBlank();
    }
}
