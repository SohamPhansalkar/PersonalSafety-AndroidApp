package com.example.personalsafety;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FakeCallActivity extends AppCompatActivity {

    private Ringtone ringtone;
    private Vibrator vibrator;
    private Handler handler = new Handler();
    private Runnable stopRunnable, ring;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        findViewById(R.id.instantCall).setOnClickListener(v -> {
            ringPhone();
        });

        findViewById(R.id.thirtySec).setOnClickListener(v -> {
            Toast.makeText(this, "Calling in 30 secs", Toast.LENGTH_LONG).show();
            ring = () -> ringPhone();
            handler.postDelayed(ring, 30_000);
        });

        findViewById(R.id.oneMin).setOnClickListener(v -> {
            Toast.makeText(this, "Calling in 1 min", Toast.LENGTH_LONG).show();
            ring = () -> ringPhone();
            handler.postDelayed(ring, 60_000);
        });

        findViewById(R.id.twoMin).setOnClickListener(v -> {
            Toast.makeText(this, "Calling in 2 mins", Toast.LENGTH_LONG).show();
            ring = () -> ringPhone();
            handler.postDelayed(ring, 120_000);
        });

        findViewById(R.id.tenMin).setOnClickListener(v -> {
            Toast.makeText(this, "Calling in 10 mins", Toast.LENGTH_LONG).show();
            ring = () -> ringPhone();
            handler.postDelayed(ring, (10*60_000));
        });


        // Stop button logic
        Button stopBtn = findViewById(R.id.stop);
        stopBtn.setOnClickListener(v -> stopFakeCall());

        Button stopBtn2 = findViewById(R.id.stop2);
        stopBtn2.setOnClickListener(v -> stopFakeCall());
    }

    private void ringPhone(){
        // Play ringtone
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
        if (ringtone != null) {
            ringtone.play();
        }

        // Vibrate
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 1000, 1000, 1000, 1000}, // Delay, vibrate, sleep...
                    0 // Repeat from index 0 (looping)
            ));
        }

        // Auto-stop after 60 seconds
        stopRunnable = () -> stopFakeCall();
        handler.postDelayed(stopRunnable, 60_000); // 60 seconds
    }
    private void stopFakeCall() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }

        handler.removeCallbacks(stopRunnable);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopFakeCall();
    }
}