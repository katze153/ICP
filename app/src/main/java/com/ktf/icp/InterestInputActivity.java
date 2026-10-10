package com.ktf.icp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ktf.icp.model.CalculationResult;
import com.ktf.icp.model.OrderData;
import com.ktf.icp.repository.OrderRepository;
import com.ktf.icp.util.DateUtils;

import java.time.LocalDate;

public class InterestInputActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_CALCULATE = "calculate";
    public static final String MODE_CREATE_ORDER = "create_order";

    private EditText inputDays;
    private EditText inputStartDate;
    private EditText inputEndDate;
    private EditText inputMoney;
    private EditText inputZins;
    private EditText inputOrderName;
    private EditText inputOrderDate;
    private Spinner inputMethod;
    private TextView labelOrderName;
    private TextView labelOrderDate;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_input);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) {
            mode = MODE_CALCULATE;
        }

        RadioGroup inputType = findViewById(R.id.inputTypeGroup);
        inputDays = findViewById(R.id.inputDays);
        inputStartDate = findViewById(R.id.inputStartDate);
        inputEndDate = findViewById(R.id.inputEndDate);
        inputMoney = findViewById(R.id.inputMoney);
        inputZins = findViewById(R.id.inputZins);
        inputOrderName = findViewById(R.id.inputOrderName);
        inputOrderDate = findViewById(R.id.inputOrderDate);
        inputMethod = findViewById(R.id.inputMethod);
        labelOrderName = findViewById(R.id.labelOrderName);
        labelOrderDate = findViewById(R.id.labelOrderDate);
        btnSubmit = findViewById(R.id.btnSubmit);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"täglich", "jährlich"});
        inputMethod.setAdapter(adapter);

        inputType.setOnCheckedChangeListener((group, checkedId) -> updateInputVisibility());
        updateInputVisibility();

        boolean createMode = MODE_CREATE_ORDER.equals(mode);
        if (createMode) {
            setTitle("Auftrag erstellen");
            btnSubmit.setText("Auftrag speichern");
            labelOrderName.setVisibility(View.VISIBLE);
            inputOrderName.setVisibility(View.VISIBLE);
            labelOrderDate.setVisibility(View.VISIBLE);
            inputOrderDate.setVisibility(View.VISIBLE);
        } else {
            setTitle("Zinsen berechnen");
            btnSubmit.setText("Berechnen");
            labelOrderName.setVisibility(View.GONE);
            inputOrderName.setVisibility(View.GONE);
            labelOrderDate.setVisibility(View.GONE);
            inputOrderDate.setVisibility(View.GONE);
        }

        String finalMode = mode;
        btnSubmit.setOnClickListener(v -> onSubmit(finalMode));
    }

    private void updateInputVisibility() {
        boolean useDays = ((RadioGroup) findViewById(R.id.inputTypeGroup)).getCheckedRadioButtonId() == R.id.radioDays;
        inputDays.setVisibility(useDays ? View.VISIBLE : View.GONE);
        inputStartDate.setVisibility(useDays ? View.GONE : View.VISIBLE);
        inputEndDate.setVisibility(useDays ? View.GONE : View.VISIBLE);
    }

    private void onSubmit(String mode) {
        try {
            boolean useDays = ((RadioGroup) findViewById(R.id.inputTypeGroup)).getCheckedRadioButtonId() == R.id.radioDays;
            int days;
            String startDate = null;
            String endDate = null;

            if (useDays) {
                days = Integer.parseInt(inputDays.getText().toString().trim());
            } else {
                LocalDate start = DateUtils.parseDate(inputStartDate.getText().toString().trim());
                LocalDate end = DateUtils.parseDate(inputEndDate.getText().toString().trim());
                days = DateUtils.daysBetween(start, end);
                startDate = DateUtils.formatDate(start);
                endDate = DateUtils.formatDate(end);
            }

            double money = Double.parseDouble(inputMoney.getText().toString().trim());
            double zins = Double.parseDouble(inputZins.getText().toString().trim());
            int methode = inputMethod.getSelectedItemPosition() == 0 ? InterestCalculator.METHOD_DAILY : InterestCalculator.METHOD_YEARLY;

            InterestCalculator calculator = new InterestCalculator();
            double finalAmount = calculator.calculateFinalAmount(money, zins, methode, days);
            CalculationResult result = new CalculationResult(money, days, zins, methode, finalAmount, startDate, endDate);

            if (MODE_CREATE_ORDER.equals(mode)) {
                String orderName = inputOrderName.getText().toString().trim();
                if (orderName.isEmpty()) {
                    Toast.makeText(this, "Auftragsname darf nicht leer sein.", Toast.LENGTH_LONG).show();
                    return;
                }

                OrderRepository repository = new OrderRepository(this);
                if (repository.exists(orderName)) {
                    Toast.makeText(this, "Ein Auftrag mit diesem Namen existiert bereits.", Toast.LENGTH_LONG).show();
                    return;
                }

                String orderDate = inputOrderDate.getText().toString().trim();
                DateUtils.parseDate(orderDate);
                repository.save(new OrderData(orderName, money, zins, methode, orderDate));
                Toast.makeText(this, "Auftrag gespeichert.", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(this, CalculationResultActivity.class);
            intent.putExtra("money", result.getMoney());
            intent.putExtra("days", result.getDays());
            intent.putExtra("zins", result.getZins());
            intent.putExtra("methode", result.getMethode());
            intent.putExtra("finalAmount", result.getFinalAmount());
            intent.putExtra("startDate", result.getStartDate());
            intent.putExtra("endDate", result.getEndDate());
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(this, "Ungültige Eingaben. Bitte Format prüfen (Datum: dd.MM.yyyy).", Toast.LENGTH_LONG).show();
        }
    }
}
