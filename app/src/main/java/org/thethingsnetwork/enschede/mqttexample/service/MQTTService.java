package org.thethingsnetwork.enschede.mqttexample.service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * A service object to handle connecting and subscribing to the TTN MQTT broker.
 */
public class MQTTService {

    private final String LOG_TAG = "mqtt-service";

    private final String MQTT_PREFS = "mqttPrefs";
    private final String MQTT_REGION_KEY = "mqttregion";
    private final String MQTT_USERNAME_KEY = "mqttusername";
    private final String MQTT_PASSWORD_KEY = "mqttpassword";
    private final String MQTT_TOPIC_KEY = "mqtttopicproduction";
    private final String MQTT_DEFAULT_TOPIC = "/devices/";

    private final String clientId = "ttn-mqtt-client";

    private String mqttRegion;
    private String mqttUser;
    private String mqttPassword;
    private String mqttSubscribeTopic;
    private String mqttPublishTopic;
    private String mqttDevice;

    private MqttAndroidClient mqttAndroidClient;


    /**
     * Get the MQTT broker to connect to.
     * @return  The MQTT broker to connect to.
     */
    public String getMqttServer() {
        return "tcp://" + mqttRegion + ".thethings.network:1883";
    }

    /**
     * Get the MQTT topic to subcribe to.
     * @return  The MQTT topic to subcribe to.
     */
    public String getMqttSubscribeTopic() {
        if (mqttSubscribeTopic == null) {
            String deviceId = "#";
            if (mqttDevice != null && !"".equals(mqttDevice)) {
                deviceId = mqttDevice + "/up";
            }
            mqttSubscribeTopic = mqttUser + MQTT_DEFAULT_TOPIC + deviceId;
        }
        return mqttSubscribeTopic;
    }

    /**
     * Get the MQTT topic to publish to.
     * @return  The MQTT topic to publish to.
     */
    public String getMqttPublishTopic() {
        if (mqttPublishTopic == null) {
            String deviceId = "#";
            if (mqttDevice != null && !"".equals(mqttDevice)) {
                deviceId = mqttDevice + "/down";
            }
            mqttPublishTopic = mqttUser + MQTT_DEFAULT_TOPIC + deviceId;
        }
        return mqttPublishTopic;
    }

    /**
     * Get the TTN region (used to connect to the MQTT broker).
     * @return  The TTN regio of your application.
     */
    public String getMqttRegion() {
        return mqttRegion;
    }

    /**
     * Set the TTN region (used to connect to the MQTT broker).
     * @param mqttRegion    The TTN regio of your application.
     */
    public void setMqttRegion(String mqttRegion) {
        this.mqttRegion = mqttRegion;
    }

    /**
     * Set the id of the TTN application.
     * @param applicationId The id of the TTN application.
     */
    public void setApplicationId(String applicationId) {
        this.mqttUser = applicationId;
    }

    /**
     * Set the access key of the TTN application.
     * @param applicationAccessKey  The access key of the TTN application.
     */
    public void setApplicationAccessKey(String applicationAccessKey) {
        this.mqttPassword = applicationAccessKey;
    }

    /**
     * Get the device id to subribe or publish to.
     * @return  The id of the device
     */
    public String getDeviceId() {
        return mqttDevice;
    }

    /**
     * Set the device id to subribe or publish to.
     * @param deviceId    The id of the device
     */
    public void setDeviceId(String deviceId) {
        this.mqttDevice = deviceId;
    }


    /**
     * A callback interface to notify activity for messages received from the broker.
     */
    public interface MQTTMessageListener {
        void onMessageReceived(String message);
    }

    private MQTTMessageListener mqttMessageListener;

    /**
     * A method for registering a new callback.
     * @param mqttMessageListener
     */
    public void registerCallback(MQTTMessageListener mqttMessageListener){
        this.mqttMessageListener = mqttMessageListener;
    }

    /**
     * Connects to the MQTT broker.
     * @param ctx   The application context.
     */
    public void connect(Context ctx) {
        String mqttClientId = MqttClient.generateClientId();
        mqttAndroidClient = new MqttAndroidClient(ctx, getMqttServer(), mqttClientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.d(LOG_TAG, "Reconnected to : " + serverURI);
                    // Because clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    Log.d(LOG_TAG, "Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(LOG_TAG, "The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(LOG_TAG, "Incoming message: " + new String(message.getPayload()));

                // Notify callback of received message.
                mqttMessageListener.onMessageReceived(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        // Setting to options for the MQTT connection.
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setKeepAliveInterval(30);
        mqttConnectOptions.setUserName(mqttUser);
        mqttConnectOptions.setPassword(mqttPassword.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(LOG_TAG, "Failed to connect to: " + getMqttServer());
                    if (exception instanceof MqttException) {
                        final MqttException mqttException = (MqttException) exception;
                        Log.d(LOG_TAG, "Disconnect reason =" + mqttException.getReasonCode());
                    }
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Subscribes to the configured MQTT topic.
     */
    private void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(getMqttSubscribeTopic(), 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(LOG_TAG, "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(LOG_TAG, "Failed to subscribe");
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publish(String message) {

        // Ignore 'long' messages.
        if (message.length() > 3) {
            Log.d(LOG_TAG, "Will not send downlink message with lenght greater than 3");
            return;
        }

        try {
            // Create a base64 encoded message for the payload.
            byte[] data = message.getBytes("UTF-8");
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);

            // Create JSON payload for raw bytes.
            JSONObject payload = new JSONObject();
            payload.put("payload_raw", base64);

            // Create MQTT message and publish it.
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(payload.toString().getBytes());
            mqttAndroidClient.publish(getMqttPublishTopic(), mqttMessage);

            if(!mqttAndroidClient.isConnected()){
                Log.d(LOG_TAG, mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "Error base64 encoding message: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Error creating JSON payload: " + e.getMessage());
            e.printStackTrace();
        } catch (MqttException e) {
            Log.d(LOG_TAG, "Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
