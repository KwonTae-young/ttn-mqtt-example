package org.thethingsnetwork.enschede.mqttexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.thethingsnetwork.enschede.mqttexample.model.TTNMessage;

import java.util.List;

/**
 * An adapter for displaying the messages in a list view.
 */
public class ListViewAdapter extends ArrayAdapter<TTNMessage> {

    public ListViewAdapter(Context context, List<TTNMessage> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
        }

        // Get the data item for current position.
        TTNMessage ttnMessage = getItem(position);

        // Lookup views for data population.
        TextView tvAppId = (TextView) convertView.findViewById(R.id.lblAppId);
        TextView tvDevId = (TextView) convertView.findViewById(R.id.lblDevId);
        TextView tvFreq = (TextView) convertView.findViewById(R.id.lblFrequency);
        TextView tvPayload = (TextView) convertView.findViewById(R.id.lblPayload);

        // Populate the views with message data.
        tvAppId.setText(ttnMessage.getAppId());
        tvDevId.setText(ttnMessage.getDevId());

        TTNMessage.Metadata metadata = ttnMessage.getMetadata();
        if (metadata != null && metadata.getFrequency() != null) {
            tvFreq.setText(metadata.getFrequency());
        } else {
            tvFreq.setText("-");
        }

        if (ttnMessage.getPayloadFields() != null) {
            tvPayload.setText(ttnMessage.getPayloadFields().toString());
        } else {
            tvPayload.setText(ttnMessage.getPayloadRaw());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}