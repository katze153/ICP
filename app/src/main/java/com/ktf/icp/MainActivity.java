package com.ktf.icp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button calculate = findViewById(R.id.btnCalculate);
        Button orders = findViewById(R.id.btnOrders);
        Button settings = findViewById(R.id.btnSettings);

        calculate.setOnClickListener(v -> {
            Intent intent = new Intent(this, InterestInputActivity.class);
            intent.putExtra(InterestInputActivity.EXTRA_MODE, InterestInputActivity.MODE_CALCULATE);
            startActivity(intent);
        });

        orders.setOnClickListener(v -> startActivity(new Intent(this, OrdersMenuActivity.class)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
}
