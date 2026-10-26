package com.example.smarthealthfitnessmonitor;

import android.app.AlertDialog;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.SharedPreferences;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener {
    private static final String CHANNEL_ID = "fitness_goal_channel";
    private boolean goalNotificationShown = false;
    TextView textDashboardGreeting, textDashboardGoal, textStepCount;
    TextView textDuration, textSensorStatus;
    Button btnEditGoal, btnStartMonitoring, btnSaveActivity, btnViewRecords;

    SharedPreferences preferences;
    DatabaseHelper databaseHelper;

    SensorManager sensorManager;
    Sensor accelerometer;

    boolean monitoring = false;
    int stepCount = 0;
    float previousMagnitude = 0;
    long lastStepTime = 0;
    long monitoringStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        textDashboardGreeting = findViewById(R.id.textDashboardGreeting);
        textDashboardGoal = findViewById(R.id.textDashboardGoal);
        textStepCount = findViewById(R.id.textStepCount);
        textDuration = findViewById(R.id.textDuration);
        textSensorStatus = findViewById(R.id.textSensorStatus);

        btnEditGoal = findViewById(R.id.btnEditGoal);
        btnStartMonitoring = findViewById(R.id.btnStartMonitoring);
        btnSaveActivity = findViewById(R.id.btnSaveActivity);
        btnViewRecords = findViewById(R.id.btnViewRecords);

        preferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        createNotificationChannel();
        requestNotificationPermission();
        databaseHelper = new DatabaseHelper(this);

        loadUserDetails();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            textSensorStatus.setText("This device does not have a movement sensor.");
            btnStartMonitoring.setEnabled(false);
        }

        btnEditGoal.setOnClickListener(view -> showGoalBox());
        btnStartMonitoring.setOnClickListener(view -> startMonitoring());
        btnSaveActivity.setOnClickListener(view -> saveTodayActivity());

        btnViewRecords.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, RecordsActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserDetails() {
        String name = preferences.getString("name", "Friend");
        int goal = preferences.getInt("stepGoal", 0);

        textDashboardGreeting.setText("Hello, " + name + "!");
        textDashboardGoal.setText("Goal: " + goal + " steps");
    }

    private void showGoalBox() {
        EditText goalInput = new EditText(this);
        goalInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        goalInput.setHint("For example: 5000");

        new AlertDialog.Builder(this)
                .setTitle("Set Your Daily Step Goal")
                .setMessage("How many steps would you like to take today?")
                .setView(goalInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String goalText = goalInput.getText().toString().trim();

                    if (goalText.isEmpty()) {
                        Toast.makeText(this, "Please enter a number.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int goal = Integer.parseInt(goalText);

                    if (goal <= 0) {
                        Toast.makeText(this, "Goal must be bigger than 0.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    preferences.edit().putInt("stepGoal", goal).apply();
                    textDashboardGoal.setText("Goal: " + goal + " steps");
                    Toast.makeText(this, "Daily goal saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startMonitoring() {
        if (accelerometer == null) {
            return;
        }

        monitoring = true;
        monitoringStartTime = System.currentTimeMillis();

        sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
        );

        textSensorStatus.setText("Monitoring movement now. Keep your phone with you.");
        btnStartMonitoring.setText("Activity Monitoring Started");
        btnStartMonitoring.setEnabled(false);
    }

    private void saveTodayActivity() {
        long durationMilliseconds = 0;

        if (monitoringStartTime > 0) {
            durationMilliseconds = System.currentTimeMillis() - monitoringStartTime;
        }

        int durationMinutes = (int) (durationMilliseconds / 60000);

        String todayDate = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        databaseHelper.saveActivity(todayDate, stepCount, durationMinutes);

        Toast.makeText(
                this,
                "Today's activity was saved!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!monitoring) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        long currentTime = System.currentTimeMillis();

        if (magnitude > 12 && previousMagnitude <= 12
                && currentTime - lastStepTime > 350) {

            stepCount++;
            lastStepTime = currentTime;
            textStepCount.setText(stepCount + " Steps");
            checkGoalReached();
        }

        previousMagnitude = magnitude;
        updateDuration();
    }

    private void updateDuration() {
        long milliseconds = System.currentTimeMillis() - monitoringStartTime;
        long minutes = milliseconds / 60000;
        textDuration.setText("Activity time: " + minutes + " minutes");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing is needed here.
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (monitoring) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (monitoring && accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fitness Goal Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }

    private void checkGoalReached() {
        int goal = preferences.getInt("stepGoal", 0);

        if (goal > 0 && stepCount >= goal && !goalNotificationShown) {
            goalNotificationShown = true;

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle("Daily goal achieved!")
                            .setContentText("Great job! You reached " + goal + " steps.")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            manager.notify(1, builder.build());

            textSensorStatus.setText("Amazing! You achieved your daily goal.");
        }
    }
}