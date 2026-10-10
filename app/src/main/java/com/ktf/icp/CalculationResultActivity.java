package com.ktf.icp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CalculationResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        double money = getIntent().getDoubleExtra("money", 0);
        int days = getIntent().getIntExtra("days", 0);
        double zins = getIntent().getDoubleExtra("zins", 0);
        int methode = getIntent().getIntExtra("methode", InterestCalculator.METHOD_DAILY);
        double finalAmount = getIntent().getDoubleExtra("finalAmount", 0);
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");

        String methodText = methode == InterestCalculator.METHOD_DAILY ? "täglich" : "jährlich";
        String result = "Geldbetrag: " + String.format("%.2f €", money) + "
"
                + (startDate != null ? "Anfangsdatum: " + startDate + "
" : "")
                + (endDate != null ? "Enddatum: " + endDate + "
" : "")
                + "Tage: " + days + "
"
                + "Zinssatz: " + String.format("%.2f %%", zins) + "
"
                + "Methode: " + methodText + "
"
                + "Zinsen: " + String.format("%.2f €", finalAmount - money) + "
"
                + "Endbetrag: " + String.format("%.2f €", finalAmount);

        TextView output = findViewById(R.id.resultText);
        output.setText(result);
    }
}
