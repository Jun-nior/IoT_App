package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi, txtLux;
    LabeledSwitch btnLED, btnPUMP;

    private boolean isWiFiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
        }
        return false;
    }

//    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            updateButtonStates();
//        }
//    };
//
    private void updateButtonStates() {
        boolean isConnected = isWiFiConnected();
        btnLED.setEnabled(isConnected);
        btnPUMP.setEnabled(isConnected);
        if (!isConnected) {
            Toast.makeText(MainActivity.this, "WiFi is disconnected. Buttons are disabled.", Toast.LENGTH_SHORT).show();
        }
    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(networkStateReceiver, filter);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(networkStateReceiver);
//    }

    private final BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateButtonStates(); // Update button states whenever network connectivity changes
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        txtLux = findViewById(R.id.txtLux);
        btnLED = findViewById(R.id.btnLED);
        btnPUMP = findViewById(R.id.btnPUMP);

        btnLED.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (!isWiFiConnected()) {
                    // Optionally, inform the user that there is no WiFi connection
                    Toast.makeText(getApplicationContext(), "No WiFi connection available", Toast.LENGTH_SHORT).show();
                    toggleableView.setEnabled(false); // Disable the button
                    return; // Exit the method
                }
                toggleableView.setEnabled(true); // Ensure the button is enabled
                // Proceed with your original code
                if (isOn) {
                    sendDataMQTT("Jun_nior/feeds/nutnhan1", "1");
                } else {
                    sendDataMQTT("Jun_nior/feeds/nutnhan1", "0");
                }
            }
        });

        btnPUMP.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (!isWiFiConnected()) {
                    // Optionally, inform the user that there is no WiFi connection
                    Toast.makeText(getApplicationContext(), "No WiFi connection available", Toast.LENGTH_SHORT).show();
                    toggleableView.setEnabled(false); // Disable the button
                    return; // Exit the method
                }
                toggleableView.setEnabled(true); // Ensure the button is enabled
                // Proceed with your original code
                if (isOn) {
                    sendDataMQTT("Jun_nior/feeds/nutnhan2", "1");
                } else {
                    sendDataMQTT("Jun_nior/feeds/nutnhan2", "0");
                }
            }
        });


        startMQTT();
    }


    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){
        }
    }

    public void startMQTT() {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (isWiFiConnected()) {
                    Log.d("TEST", topic + "***" + message.toString());
                    if (topic.contains("cambien1")) {
                        txtTemp.setText(message.toString() + "°C");
                    } else if (topic.contains("cambien3")) {
                        txtHumi.setText(message.toString() + "%");
                    } else if (topic.contains("cambien2")) {
                        txtLux.setText(message.toString());
                    } else if (topic.contains("nutnhan1")) {
                        if (message.toString().equals("1")) {
                            btnLED.setOn(true);
                        } else {
                            btnLED.setOn(false);
                        }
                    } else if (topic.contains("nutnhan2")) {
                        if (message.toString().equals("1")) {
                            btnPUMP.setOn(true);
                        } else {
                            btnPUMP.setOn(false);
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}