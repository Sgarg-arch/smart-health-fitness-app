package com.example.smarthealthfitnessmonitor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecordsActivity extends AppCompatActivity {

    TextView textRecords;
    Button btnBackDashboard;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        textRecords = findViewById(R.id.textRecords);
        btnBackDashboard = findViewById(R.id.btnBackDashboard);

        databaseHelper = new DatabaseHelper(this);

        String records = databaseHelper.getAllActivityRecords();
        textRecords.setText(records);

        btnBackDashboard.setOnClickListener(view -> finish());
    }
}