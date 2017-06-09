package org.thethingsnetwork.enschede.mqttexample.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A Singleton class to temporarily store the received messages.
 */
public class Model {
    private static final Model ourInstance = new Model();

    public static Model getInstance() {
        return ourInstance;
    }

    private List<TTNMessage> messages;

    private Model() {
        messages = new ArrayList<>();
    }

    public List<TTNMessage> getMessages() {
        return messages;
    }

    public void addMessage(TTNMessage message) {
        messages.add(message);
    }
}
