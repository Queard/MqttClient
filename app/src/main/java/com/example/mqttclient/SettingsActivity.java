package com.example.mqttclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mqttclient.mqtt.MqttParameters;
import com.example.mqttclient.mqtt.MqttParametersManager;
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
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        etServerIp = findViewById(R.id.et_server_ip);
        etPort = findViewById(R.id.et_server_port);
        etClinetId = findViewById(R.id.et_client_id);
        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);
        MqttParameters mqttParameters = MqttParametersManager.readConfig(SettingsActivity.this);
        etServerIp.setText(mqttParameters.serverIp);
        etPort.setText(mqttParameters.port);
        etClinetId.setText(mqttParameters.clientId);
        etUserName.setText(mqttParameters.userName);
        etPassword.setText(mqttParameters.passWord);

        findViewById(R.id.save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MqttParametersManager.saveConfig(SettingsActivity.this,
                        new MqttParameters(etServerIp.getText().toString()
                        , etPort.getText().toString()
                        , etClinetId.getText().toString()
                        , etUserName.getText().toString()
                        , etPassword.getText().toString()));
                Toast.makeText(SettingsActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                mqttBinder.reConnect();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }
}
