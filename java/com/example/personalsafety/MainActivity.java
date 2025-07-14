package com.example.personalsafety;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    ImageButton alertBtn;
    Button personalInfoBtn, importantBtn, SirenBtn;

    double Lat, Lon;

    MediaPlayer sirenPlayer;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissionsIfFirstTime();

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2001);
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        personalInfoBtn = findViewById(R.id.PIBtn);
        importantBtn = findViewById(R.id.ImportantBtn);
        alertBtn = findViewById(R.id.AlertBtn);
        SirenBtn = findViewById(R.id.sirenBtn);

        TextView personalInfoView = findViewById(R.id.personalInfoView);

        SharedPreferences prefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE);
        String name = prefs.getString("fullName", "Not set");
        String birthDate = prefs.getString("birthDate", "Not set");
        String bloodGroup = prefs.getString("bloodGroup", "Not set");
        String gender = prefs.getString("gender", "Not set");
        String contact1 = prefs.getString("contact1", "--");
        String contact2 = prefs.getString("contact2", "100");
        String contact3 = prefs.getString("contact3", "102");
        String contact4 = prefs.getString("contact4", "100");
        String contact5 = prefs.getString("contact5", "100");

        alertBtn.setOnClickListener(v -> {
            mainSafety();
        });

        personalInfoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PersonalInfoActivity.class);
            startActivity(intent);
        });

        SirenBtn.setOnClickListener(v ->{
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            if (sirenPlayer == null) {
                sirenPlayer = MediaPlayer.create(this, R.raw.siren); // your wav file
                sirenPlayer.setLooping(true); // Repeat continuously
                sirenPlayer.start();

                mainSafety();
            } else if (sirenPlayer.isPlaying()) {
                sirenPlayer.pause(); // You can change this to stop() if preferred
            } else {
                sirenPlayer.start();
                mainSafety();
            }
        });

        importantBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ImportantNosActivity.class);
            startActivity(intent);
        });

        if(name.equals("Not set") && bloodGroup.equals("Not set") && contact1.equals("--")){
            Intent intent = new Intent(MainActivity.this, PersonalInfoActivity.class);
            startActivity(intent);
        }else{
            String info = "Name: " + name + "\n"
                    + "Gender: " + gender + "\n"
                    + "Birth Date: " + birthDate + "\n"
                    + "Blood Group: " + bloodGroup + "\n\n"
                    + "Emergency Contacts:\n"
                    + "1. " + contact1 + "\n"
                    + "2. " + contact2 + "\n"
                    + "3. " + contact3 + "\n"
                    + "4. " + contact4 + "\n"
                    + "5. " + contact5;

            personalInfoView.setText(info);
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                String locString = "Latitude: " + lat + ", Longitude: " + lon;

                //Toast.makeText(this, "Location: " + locString, Toast.LENGTH_LONG).show();
                Lat = lat;
                Lon = lon;

                // sendLocationAndInfoToContacts(lat, lon); ← this is where you'll send the data
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void triggerEmergencyAlert() {
        SharedPreferences prefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE);

        String name = prefs.getString("fullName", "Unknown");
        String bloodGroup = prefs.getString("bloodGroup", "N/A");
        String contact1 = prefs.getString("contact1", "");
        String contact2 = prefs.getString("contact2", "");
        String contact3 = prefs.getString("contact3", "");
        String contact4 = prefs.getString("contact4", "");
        String contact5 = prefs.getString("contact5", "");


        // Use last known location (must already have been fetched earlier)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                String locationUrl = "https://maps.google.com/?q=" + lat + "," + lon;

                String message = "🚨 EMERGENCY 🚨\n" +
                        "Name: " + name + "\n" +
                        "Blood Group: " + bloodGroup + "\n" +
                        "Location: " + locationUrl + "\n"+
                        "🚨 Needs Urgent ATTENTION 🚨";

                for(int i = 0; i <= 5; i++){
                    switch (i){
                        case 1:
                            if(!contact1.isEmpty()){
                                //sendSMS(contact1, message);
                                sendViaWhatsApp(contact1, message);
                            }
                            break;
                        case 2:
                            if(!contact2.isEmpty()){
                                //sendSMS(contact2, message);
                                sendViaWhatsApp(contact2, message);
                            }
                            break;
                        case 3:
                            if(!contact3.isEmpty()){
                                //sendSMS(contact3, message);
                                sendViaWhatsApp(contact3, message);
                            }
                            break;
                        case 4:
                            if(!contact4.isEmpty()){
                                //sendSMS(contact4, message);
                                sendViaWhatsApp(contact4, message);
                            }
                            break;
                        case 5:
                            if(!contact5.isEmpty()){
                                //sendSMS(contact5, message);
                                sendViaWhatsApp(contact5, message);
                            }
                            break;
                    }
                }

            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                String message = "🚨 EMERGENCY 🚨\n" +
                        "Name: " + name + "\n" +
                        "Blood Group: " + bloodGroup + "\n" +
                        "Location: Not Available"+ "\n"+
                        "🚨 Needs Urgent ATTENTION 🚨";

                for(int i = 0; i <= 5; i++){
                    switch (i){
                        case 1:
                            if(!contact1.isEmpty()){
                                //sendSMS(contact1, message);
                                sendViaWhatsApp(contact1, message);
                            }
                            break;
                        case 2:
                            if(!contact2.isEmpty()){
                                //sendSMS(contact2, message);
                                sendViaWhatsApp(contact2, message);
                            }
                            break;
                        case 3:
                            if(!contact3.isEmpty()){
                                //sendSMS(contact3, message);
                                sendViaWhatsApp(contact3, message);
                            }
                            break;
                        case 4:
                            if(!contact4.isEmpty()){
                                //sendSMS(contact4, message);
                                sendViaWhatsApp(contact4, message);
                            }
                            break;
                        case 5:
                            if(!contact5.isEmpty()){
                                //sendSMS(contact5, message);
                                sendViaWhatsApp(contact5, message);
                            }
                            break;
                    }
                }
            }
        });
    }

//    private void sendSMS(String phoneNumber, String message) {
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("smsto:" + phoneNumber));  // Use smsto: for SMS apps
//        intent.putExtra("sms_body", message);
//        try {
//            startActivity(intent);
//        } catch (Exception e) {
//            Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void  mainSafety(){
        SharedPreferences prefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE);
        String name = prefs.getString("fullName", "Unknown");
        String bloodGroup = prefs.getString("bloodGroup", "N/A");
        String birthDate = prefs.getString("birthDate", "N/A");
        String contact1 = prefs.getString("contact1", "100");
        String contact2 = prefs.getString("contact2", "101");
        String contact3 = prefs.getString("contact3", "");
        String contact4 = prefs.getString("contact4", "");
        String contact5 = prefs.getString("contact5", "");

        getCurrentLocation();
        triggerEmergencyAlert();
        sendNotification(102, "Phone Numbers", "call : " + contact2 + " - " + contact3 + " - " + contact4 + " - "+ contact5);
        sendNotification(101, "Help line Numbers", "Police : 100, Ambulance : 102");
        sendNotification(100, "Emergency","Help: " + contact1 + "\nBloodGroup: "+ bloodGroup +" DOB: "+ birthDate + " " + name);

    }


    private void sendViaWhatsApp(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + URLEncoder.encode(message, "UTF-8");
            intent.setPackage("com.whatsapp");
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed or error sending", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AlertChannel";
            String description = "Channel for Alert Button Notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("alertChannelId", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void sendNotification(int id, String title, String content){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "alertChannelId")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(id, builder.build());
    }

    private void callPolice(){
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:100"));
        startActivity(dialIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE);
        String name = prefs.getString("fullName", "Not set");
        String birthDate = prefs.getString("birthDate", "Not set");
        String bloodGroup = prefs.getString("bloodGroup", "Not set");
        String gender = prefs.getString("gender", "Not set");
        String contact1 = prefs.getString("contact1", "--");
        String contact2 = prefs.getString("contact2", "--");
        String contact3 = prefs.getString("contact3", "--");
        String contact4 = prefs.getString("contact4", "--");
        String contact5 = prefs.getString("contact5", "--");

        if(name.equals("Not set") && bloodGroup.equals("Not set") && contact1.equals("--")){
            Intent intent = new Intent(MainActivity.this, PersonalInfoActivity.class);
            startActivity(intent);
        }

        String info = "Name: " + name + "\n"
                + "Gender: " + gender + "\n"
                + "Birth Date: " + birthDate + "\n"
                + "Blood Group: " + bloodGroup + "\n\n"
                + "Emergency Contacts:\n"
                + "1. " + contact1 + "\n"
                + "2. " + contact2 + "\n"
                + "3. " + contact3 + "\n"
                + "4. " + contact4 + "\n"
                + "5. " + contact5;

        TextView personalInfoView = findViewById(R.id.personalInfoView);
        personalInfoView.setText(info);
    }

    private void checkAndRequestPermissionsIfFirstTime() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // Request permissions
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.FOREGROUND_SERVICE,
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA
                    },
                    100
            );

            // Mark that permissions were asked
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }
    }

}