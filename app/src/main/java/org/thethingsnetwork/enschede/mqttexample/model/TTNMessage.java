package org.thethingsnetwork.enschede.mqttexample.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A model class to store values from the MQTT message.
 */
public class TTNMessage {

    private String appId;
    private String devId;
    private String payloadRaw;
    private JSONObject payloadFields;

    private Metadata metadata;

    public TTNMessage(JSONObject jsonObject) {
        appId = jsonObject.optString("app_id","");
        devId = jsonObject.optString("dev_id","");
        payloadRaw = jsonObject.optString("payload_raw","");
        payloadFields = jsonObject.optJSONObject("payload_fields");

        metadata = new Metadata(jsonObject.optJSONObject("metadata"));
    }

    public class Metadata {
        private String time;
        private String frequency;
        private String dataRate;

        private List<Gateway> gateways;

        public Metadata(JSONObject jsonObject) {
            if (jsonObject != null) {
                time = jsonObject.optString("time");
                frequency = jsonObject.optString("frequency");
                dataRate = jsonObject.optString("data_rate");

                gateways = new ArrayList<>();

                JSONArray gatewayArray = jsonObject.optJSONArray("gateways");
                if (gatewayArray != null && gatewayArray.length() > 0) {
                    for(int i = 0; i < gatewayArray.length(); i++){
                        JSONObject gatewayJson = gatewayArray.optJSONObject(i);
                        gateways.add(new Gateway(gatewayJson));
                    }
                }
            }
        }

        public class Gateway {
            private String gatewayId;
            private double latitude;
            private double longitude;
            private int altitude;

            public Gateway(JSONObject jsonObject) {
                if (jsonObject != null) {
                    gatewayId = jsonObject.optString("gtw_id");
                    latitude = jsonObject.optDouble("latitude");
                    longitude = jsonObject.optDouble("longitude");
                    altitude = jsonObject.optInt("altitude");
                }
            }

            public String getGatewayId() {
                return gatewayId;
            }

            public double getLatitude() {
                return latitude;
            }

            public double getLongitude() {
                return longitude;
            }

            public int getAltitude() {
                return altitude;
            }
        }

        public String getTime() {
            return time;
        }

        public String getFrequency() {
            return frequency;
        }

        public String getDataRate() {
            return dataRate;
        }

        public List<Gateway> getGateways() {
            return gateways;
        }
    }

    public String getAppId() {
        return appId;
    }

    public String getDevId() {
        return devId;
    }

    public String getPayloadRaw() {
        return payloadRaw;
    }

    public JSONObject getPayloadFields() {
        return payloadFields;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
