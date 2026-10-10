package com.ktf.icp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ktf.icp.repository.AppConfigRepository;
import com.ktf.icp.sync.DropboxSyncService;

import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {
    public static final String REDIRECT_URI = "icp://dropbox-auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        EditText appKeyInput = findViewById(R.id.inputAppKey);
        EditText appSecretInput = findViewById(R.id.inputAppSecret);
        Button setupButton = findViewById(R.id.btnSetupDropbox);
        Button uploadButton = findViewById(R.id.btnUpload);
        Button downloadButton = findViewById(R.id.btnDownload);

        AppConfigRepository configRepository = new AppConfigRepository(this);
        DropboxSyncService dropboxSyncService = new DropboxSyncService(this, configRepository);

        String savedKey = configRepository.get(AppConfigRepository.KEY_APP_KEY);
        String savedSecret = configRepository.get(AppConfigRepository.KEY_APP_SECRET);
        if (savedKey != null) appKeyInput.setText(savedKey);
        if (savedSecret != null) appSecretInput.setText(savedSecret);

        setupButton.setOnClickListener(v -> {
            String appKey = appKeyInput.getText().toString().trim();
            String appSecret = appSecretInput.getText().toString().trim();
            if (appKey.isEmpty() || appSecret.isEmpty()) {
                Toast.makeText(this, "Bitte App Key und App Secret eingeben.", Toast.LENGTH_LONG).show();
                return;
            }

            String state = UUID.randomUUID().toString();
            configRepository.set(AppConfigRepository.KEY_APP_KEY, appKey);
            configRepository.set(AppConfigRepository.KEY_APP_SECRET, appSecret);
            configRepository.set(AppConfigRepository.KEY_PENDING_STATE, state);
            String url = dropboxSyncService.createAuthorizationUrl(appKey, REDIRECT_URI, state);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        uploadButton.setOnClickListener(v -> dropboxSyncService.uploadSave()
                .thenRun(() -> runOnUiThread(() -> Toast.makeText(this, "Upload erfolgreich.", Toast.LENGTH_SHORT).show()))
                .exceptionally(ex -> {
                    runOnUiThread(() -> Toast.makeText(this, "Upload fehlgeschlagen. Prüfe Internet und Dropbox-Setup.", Toast.LENGTH_LONG).show());
                    return null;
                }));

        downloadButton.setOnClickListener(v -> dropboxSyncService.downloadSave()
                .thenRun(() -> runOnUiThread(() -> Toast.makeText(this, "Download erfolgreich.", Toast.LENGTH_SHORT).show()))
                .exceptionally(ex -> {
                    runOnUiThread(() -> Toast.makeText(this, "Download fehlgeschlagen. Prüfe Internet und Dropbox-Setup.", Toast.LENGTH_LONG).show());
                    return null;
                }));
    }
}
