package org.thethingsnetwork.enschede.mqttexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.thethingsnetwork.enschede.mqttexample.model.Model;
import org.thethingsnetwork.enschede.mqttexample.model.TTNMessage;
import org.thethingsnetwork.enschede.mqttexample.service.MQTTService;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements MQTTService.MQTTMessageListener {

    private MQTTService mqttService;

    //TODO: Configure these properties to connect to MQTT broker.
    // You can find the values in the TTN dashboard.
    private final String region = "eu";
    private final String applicationId = "";
    private final String applicationAccessKey = "";

    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ListViewAdapter(this, Model.getInstance().getMessages());
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        mqttService = new MQTTService();
        mqttService.setMqttRegion(region);
        mqttService.setApplicationId(applicationId);
        mqttService.setApplicationAccessKey(applicationAccessKey);

        mqttService.registerCallback(this);
        mqttService.connect(this);
    }

    @Override
    public void onMessageReceived(String message) {
        JSONObject jsonObject = null;
        try {
            // Parse the received message.
            jsonObject = new JSONObject(message);
            TTNMessage ttnMessage = new TTNMessage(jsonObject);
            Model.getInstance().addMessage(ttnMessage);

            // Notify adapter in order to refresh the listview.
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
