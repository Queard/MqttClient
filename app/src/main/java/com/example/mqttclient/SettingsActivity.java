package com.example.mqttclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;

import com.example.mqttclient.mqtt.MqttService;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private MqttService.MqttBinder mqttBinder;
    private String TAG = "SettingsActivity";
    private EditText etServerIp, etPort, etClinetId, etUserName, etPassword;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle saveInstaceState) {
        super.onCreate(saveInstaceState);
        setContentView(R.layout.activity_settings);
        final Intent intent = new Intent(this, MqttService.class);

    }

}
