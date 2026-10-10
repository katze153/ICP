package com.ktf.icp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ktf.icp.repository.OrderRepository;

import java.util.List;

public class OrdersListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);

        ListView listView = findViewById(R.id.ordersList);
        TextView emptyText = findViewById(R.id.emptyOrdersText);

        OrderRepository repository = new OrderRepository(this);
        List<String> names = repository.getAllOrderNames();

        if (names.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        }

        emptyText.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = names.get(position);
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("orderName", name);
            startActivity(intent);
        });
    }
}
