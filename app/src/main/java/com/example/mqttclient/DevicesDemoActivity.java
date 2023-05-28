package com.example.mqttclient;

import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.mqttclient.mqtt.MqttService;

import androidx.appcompat.app.AppCompatActivity;

public class DevicesDemoActivity extends AppCompatActivity implements MqttService.MqttEventCallBack,
        CompoundButton.OnCheckedChangeListener {

    private TextView connectState;
}
