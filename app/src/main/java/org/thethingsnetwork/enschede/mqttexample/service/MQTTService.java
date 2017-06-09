package org.thethingsnetwork.enschede.mqttexample.service;

import android.content.Context;
import android.content.SharedPreferences;
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
    private final String MQTT_DEFAULT_TOPIC = "+/devices/#";

    private final String clientId = "ttn-mqtt-client";

    private String mqttRegion;
    private String mqttUser;
    private String mqttPassword;
    private String mqttTopic;

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
    public String getMqttTopic() {
        if (mqttTopic == null) {
            mqttTopic = MQTT_DEFAULT_TOPIC;
        }
        return mqttTopic;
    }

    /**
     * Set the MQTT topic to subcribe to.
     * @param mqttTopic The MQTT topic to subcribe to.
     */
    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
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
            mqttAndroidClient.subscribe(getMqttTopic(), 0, null, new IMqttActionListener() {
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
}
