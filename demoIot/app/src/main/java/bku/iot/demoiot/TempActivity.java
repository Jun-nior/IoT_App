package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TempActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    TextView txtTemp;
    private ImageView icreturn;

    private LineChart lineChart;
    private List<String> xValues;
    private LineDataSet lineDataSet;
    private List<Entry> entries= new ArrayList<>();

    private int count=0;
    private boolean isWiFiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        txtTemp = findViewById(R.id.txtTemperature);
        icreturn=findViewById(R.id.icreturn);
        lineChart=findViewById(R.id.chart);
        Description description= new Description();
        description.setText("Temperature");
        description.setPosition(150f,15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);



        xValues = new ArrayList<>();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(10f);
        yAxis.setAxisMaximum(40f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        lineDataSet = new LineDataSet(entries, "Temperature");
        lineDataSet.setColor(Color.RED);
        LineData data= new LineData(lineDataSet);
        lineChart.setData(data);
        lineChart.invalidate();


        icreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start a new Activity
                Intent intent = new Intent(TempActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        startMQTT();
    }



    public void startMQTT() {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Toast.makeText(getApplicationContext(), "WiFi connection available", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getApplicationContext(), "No WiFi connection available", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (isWiFiConnected()) {
                    Log.d("TEST", topic + "***" + message.toString());
                    if (topic.contains("cambien1")) {
//                        txtTemp.setText(message.toString() + "°C");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtTemp.setText(message.toString() + "°C");
                                try {
                                    // Parse the temperature value from the MQTT message
                                    float tempValue = Float.parseFloat(message.toString());
                                    xValues.add(String.valueOf(count+1));
                                    // Create a new data entry with the received temperature
                                    if (count>=10) {
                                        entries.remove(0);
                                        entries.add(new Entry(count, tempValue));
                                        count++;
                                    } else {
                                        entries.add(new Entry(count, tempValue));
                                        count++;
                                    }
                                    lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
                                    lineDataSet = new LineDataSet(entries, "Temperature");
                                    lineDataSet.setColor(Color.RED);
                                    LineData data= new LineData(lineDataSet);
                                    lineChart.setData(data);
                                    lineChart.invalidate();
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    // Handle exception if the message is not a valid float
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}