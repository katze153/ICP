package com.ktf.icp.sync;

import android.content.Context;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.oauth.DbxRefreshResult;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.ktf.icp.repository.AppConfigRepository;
import com.ktf.icp.repository.OrderRepository;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DropboxSyncService {
    public static final String DROPBOX_SAVE_PATH = "/Zinsen/save.toml";
    private static final String TOKEN_ENDPOINT = "https://api.dropboxapi.com/oauth2/token";

    private final Context context;
    private final AppConfigRepository configRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DropboxSyncService(Context context, AppConfigRepository configRepository) {
        this.context = context.getApplicationContext();
        this.configRepository = configRepository;
    }

    public String createAuthorizationUrl(String appKey, String redirectUri, String state) {
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        return "https://www.dropbox.com/oauth2/authorize?client_id=" + appKey
                + "&response_type=code&token_access_type=offline&redirect_uri=" + encodedRedirect
                + "&state=" + encodedState;
    }

    public CompletableFuture<Void> setupWithAuthorizationCode(String appKey, String appSecret, String code, String redirectUri) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                        + "&grant_type=authorization_code"
                        + "&client_id=" + URLEncoder.encode(appKey, StandardCharsets.UTF_8)
                        + "&client_secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
                        + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

                HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(TOKEN_ENDPOINT).toURL().openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

                int status = connection.getResponseCode();
                String response;
                try (Scanner scanner = new Scanner(status >= 400 ? connection.getErrorStream() : connection.getInputStream(), StandardCharsets.UTF_8)) {
                    scanner.useDelimiter("\A");
                    response = scanner.hasNext() ? scanner.next() : "";
                }

                if (status >= 400) {
                    throw new IllegalStateException("Dropbox OAuth fehlgeschlagen: " + status);
                }

                JSONObject json = new JSONObject(response);
                String accessToken = json.getString("access_token");
                String refreshToken = json.optString("refresh_token", "");
                long expiresIn = json.optLong("expires_in", 0L);
                long expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);

                configRepository.set(AppConfigRepository.KEY_APP_KEY, appKey);
                configRepository.set(AppConfigRepository.KEY_APP_SECRET, appSecret);
                configRepository.set(AppConfigRepository.KEY_ACCESS_TOKEN, accessToken);
                if (!refreshToken.isBlank()) {
                    configRepository.set(AppConfigRepository.KEY_REFRESH_TOKEN, refreshToken);
                }
                configRepository.set(AppConfigRepository.KEY_EXPIRES_AT, Long.toString(expiresAt));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Void> uploadSave() {
        return CompletableFuture.runAsync(() -> {
            try {
                DbxClientV2 client = getClient();
                File saveFile = OrderRepository.getSaveFile(context);
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                try (InputStream in = new FileInputStream(saveFile)) {
                    client.files().uploadBuilder(DROPBOX_SAVE_PATH)
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(in);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Void> downloadSave() {
        return CompletableFuture.runAsync(() -> {
            try {
                DbxClientV2 client = getClient();
                File saveFile = OrderRepository.getSaveFile(context);
                try (OutputStream out = new FileOutputStream(saveFile)) {
                    client.files().download(DROPBOX_SAVE_PATH).download(out);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public boolean isConfigured() {
        return configRepository.hasDropboxSetup();
    }

    private DbxClientV2 getClient() throws Exception {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("ICP-Android").build();
        String appKey = configRepository.get(AppConfigRepository.KEY_APP_KEY);
        String appSecret = configRepository.get(AppConfigRepository.KEY_APP_SECRET);
        String accessToken = configRepository.get(AppConfigRepository.KEY_ACCESS_TOKEN);
        String refreshToken = configRepository.get(AppConfigRepository.KEY_REFRESH_TOKEN);
        String expires = configRepository.get(AppConfigRepository.KEY_EXPIRES_AT);

        long expiresAt = 0L;
        if (expires != null && !expires.isBlank()) {
            expiresAt = Long.parseLong(expires);
        }

        DbxCredential credential = new DbxCredential(accessToken, expiresAt, refreshToken, appKey, appSecret);
        if (refreshToken != null && !refreshToken.isBlank() && (expiresAt == 0L || System.currentTimeMillis() > expiresAt - 60_000L)) {
            DbxRefreshResult refreshResult = credential.refresh(requestConfig);
            credential = new DbxCredential(refreshResult.getAccessToken(), refreshResult.getExpiresAt(), refreshToken, appKey, appSecret);
            configRepository.set(AppConfigRepository.KEY_ACCESS_TOKEN, refreshResult.getAccessToken());
            configRepository.set(AppConfigRepository.KEY_EXPIRES_AT, Long.toString(refreshResult.getExpiresAt()));
        }

        return new DbxClientV2(requestConfig, credential);
    }
}
