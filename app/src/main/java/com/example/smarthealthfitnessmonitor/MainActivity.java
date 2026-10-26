package com.example.smarthealthfitnessmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editName, editAge, editHeight, editWeight;
    RadioGroup radioGender;
    Button btnSaveProfile;
    TextView textWelcome;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        radioGender = findViewById(R.id.radioGender);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        textWelcome = findViewById(R.id.textWelcome);

        preferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        loadSavedProfile();

        btnSaveProfile.setOnClickListener(view -> saveProfileAndOpenDashboard());
    }

    private void saveProfileAndOpenDashboard() {
        String name = editName.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String height = editHeight.getText().toString().trim();
        String weight = editWeight.getText().toString().trim();

        int selectedGenderId = radioGender.getCheckedRadioButtonId();

        if (name.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()
                || selectedGenderId == -1) {
            Toast.makeText(this,
                    "Please fill in every box and choose a gender.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int ageNumber = Integer.parseInt(age);

        if (ageNumber <= 0) {
            editAge.setError("Age must be bigger than 0.");
            return;
        }

        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", name);
        editor.putString("age", age);
        editor.putString("gender", gender);
        editor.putString("height", height);
        editor.putString("weight", weight);
        editor.apply();

        textWelcome.setText("Profile saved. Opening your dashboard...");
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
    }

    private void loadSavedProfile() {
        String savedName = preferences.getString("name", "");

        if (!savedName.isEmpty()) {
            editName.setText(savedName);
            editAge.setText(preferences.getString("age", ""));
            editHeight.setText(preferences.getString("height", ""));

            editWeight.setText(preferences.getString("weight", ""));

            String savedGender = preferences.getString("gender", "");

            if (savedGender.equals("Male")) {
                radioGender.check(R.id.radioMale);
            } else if (savedGender.equals("Female")) {
                radioGender.check(R.id.radioFemale);
            } else if (savedGender.equals("Other")) {
                radioGender.check(R.id.radioOther);
            }

            textWelcome.setText("Welcome back, " + savedName + "!");
        }
    }
}