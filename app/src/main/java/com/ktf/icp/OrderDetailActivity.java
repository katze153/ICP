package com.ktf.icp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ktf.icp.model.CalculationResult;
import com.ktf.icp.model.OrderData;
import com.ktf.icp.repository.AppConfigRepository;
import com.ktf.icp.repository.OrderRepository;
import com.ktf.icp.sync.DropboxSyncService;
import com.ktf.icp.util.DateUtils;

import java.time.LocalDate;

public class OrderDetailActivity extends AppCompatActivity {
    private OrderRepository repository;
    private DropboxSyncService dropboxSyncService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        repository = new OrderRepository(this);
        dropboxSyncService = new DropboxSyncService(this, new AppConfigRepository(this));

        String name = getIntent().getStringExtra("orderName");
        if (name == null) {
            finish();
            return;
        }

        OrderData orderData = repository.get(name);
        if (orderData == null) {
            Toast.makeText(this, "Auftrag nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        CalculationResult result = repository.calculateCurrentResult(orderData, LocalDate.now(), new InterestCalculator());
        String methodText = result.getMethode() == InterestCalculator.METHOD_DAILY ? "täglich" : "jährlich";

        TextView details = findViewById(R.id.orderDetails);
        details.setText("Auftrag: " + orderData.getName() + "
"
                + "Geldbetrag: " + String.format("%.2f €", result.getMoney()) + "
"
                + "Gespeichertes Datum: " + orderData.getDate() + "
"
                + "Aktuelles Datum: " + DateUtils.formatDate(LocalDate.now()) + "
"
                + "Tage: " + result.getDays() + "
"
                + "Zinssatz: " + String.format("%.2f %%", result.getZins()) + "
"
                + "Methode: " + methodText + "
"
                + "Zinsen: " + String.format("%.2f €", result.getInterest()) + "
"
                + "Endbetrag: " + String.format("%.2f €", result.getFinalAmount()));

        Button deleteButton = findViewById(R.id.btnDeleteOrder);
        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Auftrag löschen")
                .setMessage("Soll der Auftrag wirklich gelöscht werden?")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (dialog, which) -> {
                    if (repository.delete(orderData.getName())) {
                        if (dropboxSyncService.isConfigured()) {
                            dropboxSyncService.uploadSave()
                                    .thenRun(() -> runOnUiThread(() -> Toast.makeText(this, "Auftrag gelöscht und synchronisiert.", Toast.LENGTH_SHORT).show()))
                                    .exceptionally(ex -> {
                                        runOnUiThread(() -> Toast.makeText(this, "Auftrag gelöscht, Sync fehlgeschlagen.", Toast.LENGTH_LONG).show());
                                        return null;
                                    });
                        }
                        Toast.makeText(this, "Auftrag gelöscht.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Löschen fehlgeschlagen.", Toast.LENGTH_LONG).show();
                    }
                }).show());
    }
}
