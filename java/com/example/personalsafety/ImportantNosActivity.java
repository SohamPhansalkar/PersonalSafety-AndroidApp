package com.example.personalsafety;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ImportantNosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_nos);

        // Call buttons
        findViewById(R.id.policeBtn).setOnClickListener(v ->
                dialNumber("100"));

        findViewById(R.id.ambulanceBtn).setOnClickListener(v ->
                dialNumber("102"));

        findViewById(R.id.fireBtn).setOnClickListener(v ->
                dialNumber("101"));

        findViewById(R.id.womenHelplineBtn).setOnClickListener(v ->
                dialNumber("1091"));

        findViewById(R.id.childHelplineBtn).setOnClickListener(v ->
                dialNumber("1098"));

        findViewById(R.id.fakeCallBtn).setOnClickListener(v -> {
            Intent intent = new Intent(ImportantNosActivity.this, FakeCallActivity.class);
            startActivity(intent);
        });
    }

    private void dialNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }
}

