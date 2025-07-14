package com.example.personalsafety;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PersonalInfoActivity extends AppCompatActivity {

    EditText fullNameInput, birthDateInput, bloodGroupInput;
    RadioGroup genderGroup;
    RadioButton maleRadio, femaleRadio;
    EditText contact1Input, contact2Input, contact3Input, contact4Input, contact5Input;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // Initialize UI elements
        fullNameInput = findViewById(R.id.fullNameInput);
        birthDateInput = findViewById(R.id.birthDateInput);
        bloodGroupInput = findViewById(R.id.bloodGroupInput);
        genderGroup = findViewById(R.id.genderGroup);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);

        contact1Input = findViewById(R.id.contact1Input);
        contact2Input = findViewById(R.id.contact2Input);
        contact3Input = findViewById(R.id.contact3Input);
        contact4Input = findViewById(R.id.contact4Input);
        contact5Input = findViewById(R.id.contact5Input);

        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(v -> {
            String fullName = fullNameInput.getText().toString().trim();
            String birthDate = birthDateInput.getText().toString().trim();
            String bloodGroup = bloodGroupInput.getText().toString().trim();

            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            String gender = "";
            if (selectedGenderId == maleRadio.getId()) {
                gender = "Boy";
            } else if (selectedGenderId == femaleRadio.getId()) {
                gender = "Girl";
            }

            String contact1 = checkNum(contact1Input.getText().toString().trim());
            String contact2 = checkNum(contact2Input.getText().toString().trim());
            String contact3 = checkNum(contact3Input.getText().toString().trim());
            String contact4 = checkNum(contact4Input.getText().toString().trim());
            String contact5 = checkNum(contact5Input.getText().toString().trim());

            if (!fullName.isEmpty() && !birthDate.isEmpty() && !bloodGroup.isEmpty() && !gender.isEmpty() && !contact1.isEmpty()) {

                // Save to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("fullName", fullName);
                editor.putString("birthDate", birthDate);
                editor.putString("bloodGroup", bloodGroup);
                editor.putString("gender", gender);
                editor.putString("contact1", contact1);
                editor.putString("contact2", contact2);
                editor.putString("contact3", contact3);
                editor.putString("contact4", contact4);
                editor.putString("contact5", contact5);

                editor.apply(); // Saves asynchronously

                Toast.makeText(this, "Information saved successfully!", Toast.LENGTH_SHORT).show();

                finish(); // Go back to previous activity (MainActivity)
            } else {
                Toast.makeText(this, "Please fill in all required details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validates number and adds +91 if missing
    private String checkNum(String num) {
        String code = "+91";
        if (num.length() >= 3 && num.startsWith(code)) {
            return num;
        } else if (!num.isEmpty()) {
            Toast.makeText(this, "Added +91 to number", Toast.LENGTH_SHORT).show();
            return code + num;
        } else {
            return ""; // Empty field
        }
    }
}
