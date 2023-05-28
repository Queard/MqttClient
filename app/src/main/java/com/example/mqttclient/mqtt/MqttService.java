package com.example.mqttclient.mqtt;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

public class MqttService extends Service {

    public static final String TAG = MqttService.class.getSimpleName();
    private static MqttAndroidClient client;
    private MqttEventCallBack mqttEventCallBack;
    private MqttBinder mqttBinder = new MqttBinder();
    MqttConnectOptions conOpt = new MqttConnectOptions();

    @Override
    public void onCreate() {
        super.onCreate();
        connect(MqttParametersManager.readConfig(MqttService.this));
    }

    public interface MqttEventCallBack {
        void onConnectSuccess();
        void onConnectError(String error);
        void onDeliveryComplete();
        void onMqttMessage(String topic, String message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mqttBinder;
    }
    public class MqttBinder extends Binder {
        public void reConnect() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connect(MqttParametersManager.readConfig(MqttService.this));
                }
            }).start();
        }
        public boolean isConnected() {
            return client != null && client.isConnected();
        }
        public void subscribe(String topic) throws MqttException {
            if (client != null && client.isConnected()) {
                client.subscribe(topic, 1);
            }
        }
        public void unSubscribe(String topic) throws MqttException {
            if (client != null && client.isConnected()) {
                client.unsubscribe(topic);
            }
        }

        public void publishMessage(String topic, String message) throws MqttException {
            if (client != null && client.isConnected()) {
                client.publish(topic, message.getBytes(), 0, false);
            }
        }
        public void setMqttEventCallback(MqttEventCallBack callback) {
            mqttEventCallBack = callback;
        }

    }
    private void connect(MqttParameters parameters) {
        disConnect();

        client = new MqttAndroidClient(this, parameters.getUri(), parameters.clientId);
        client.setCallback(mqttCallback);
        conOpt.setCleanSession(true);
        conOpt.setConnectionTimeout(10);
        conOpt.setKeepAliveInterval(20);
        if ((parameters.userName != null) && (!parameters.passWord.trim().equals(""))
            && (parameters.passWord != null) && (!parameters.passWord.trim().equals(""))) {
            conOpt.setUserName(parameters.userName);
            conOpt.setPassword(parameters.passWord.toCharArray());
        }

        String lwtMessage = "{\"terminal_uid\":\"" + parameters.clientId + "\"";
        try {
            conOpt.setWill(parameters.clientId, lwtMessage.getBytes(), 0, false);
        } catch (Exception e) {
            iMqttActionListener.onFailure(null, e);
        }

        try {
            if (!client.isConnected()) {
                client.connect(conOpt, null, iMqttActionListener);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void disConnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            }catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        stopSelf();;
        disConnect();
        super.onDestroy();
    }

    private  IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.i(TAG, "connect success");
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onConnectSuccess();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.i(TAG, "connect fail:" + asyncActionToken.toString());
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onConnectError(exception.toString());
            }
            mqttCallback.connectionLost(exception);
        }
    };

    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onConnectError("Connecting lost! MqttService will reconnect after 5s...");
            }
            try {
                Thread.sleep(5000);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (client != null && !client.isConnected()) {
                try {
                    client.connect(conOpt, null, iMqttActionListener);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String str1 = new String(message.getPayload());
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onMqttMessage(topic, str1);
            }
            Log.i(TAG, "messageArrived:" + str1);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onDeliveryComplete();
            }
        }
    };
}
