package com.ktf.icp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OrdersMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_menu);

        Button create = findViewById(R.id.btnCreateOrder);
        Button read = findViewById(R.id.btnReadOrders);

        create.setOnClickListener(v -> {
            Intent intent = new Intent(this, InterestInputActivity.class);
            intent.putExtra(InterestInputActivity.EXTRA_MODE, InterestInputActivity.MODE_CREATE_ORDER);
            startActivity(intent);
        });
        read.setOnClickListener(v -> startActivity(new Intent(this, OrdersListActivity.class)));
    }
}
