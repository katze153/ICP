package com.ktf.icp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ktf.icp.repository.AppConfigRepository;
import com.ktf.icp.sync.DropboxSyncService;

public class OAuthCallbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppConfigRepository configRepository = new AppConfigRepository(this);
        DropboxSyncService dropboxSyncService = new DropboxSyncService(this, configRepository);

        if (getIntent() == null || getIntent().getData() == null) {
            finish();
            return;
        }

        String code = getIntent().getData().getQueryParameter("code");
        String state = getIntent().getData().getQueryParameter("state");
        String error = getIntent().getData().getQueryParameter("error");
        String expectedState = configRepository.get(AppConfigRepository.KEY_PENDING_STATE);

        if (error != null) {
            Toast.makeText(this, "Dropbox OAuth abgebrochen: " + error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (code == null || state == null || expectedState == null || !state.equals(expectedState)) {
            Toast.makeText(this, "Ungültige OAuth-Antwort.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String appKey = configRepository.get(AppConfigRepository.KEY_APP_KEY);
        String appSecret = configRepository.get(AppConfigRepository.KEY_APP_SECRET);

        dropboxSyncService.setupWithAuthorizationCode(appKey, appSecret, code, SettingsActivity.REDIRECT_URI)
                .thenRun(() -> runOnUiThread(() -> Toast.makeText(this, "Dropbox eingerichtet.", Toast.LENGTH_LONG).show()))
                .exceptionally(ex -> {
                    runOnUiThread(() -> Toast.makeText(this, "Dropbox-Einrichtung fehlgeschlagen.", Toast.LENGTH_LONG).show());
                    return null;
                })
                .whenComplete((r, ex) -> runOnUiThread(this::finish));
    }
}
