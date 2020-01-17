package network.matic.dagger;

import network.matic.dagger.exceptions.DaggerException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;

public class Dagger implements MqttCallback {
    public static final String MESSAGE = "message";


    private String url;
    private Options options;
    private MqttClient client;
    private Map<String, MqttRegex> regexTopics;
    private Map<String, List<Listener>> listeners;

    // Dagger constructor to create dagger instance
    public Dagger(String url) throws DaggerException {
        this(url, null);
    }

    // Dagger constructor to create dagger instance with options
    public Dagger(String url, Options options) throws DaggerException {
        if (url == null || "".equals(url)) {
            throw new DaggerException("Invalid URL");
        }

        // create options if null
        if (options == null) {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);

            // create options
            options = new Options();
            options.setMqttConnectOptions(mqttConnectOptions);
        }

        this.url = url;
        this.options = options;
        this.regexTopics = new HashMap<>();

        MemoryPersistence persistence = new MemoryPersistence();
        UUID clientId = UUID.randomUUID();

        try {
            this.client = new MqttClient(this.url, clientId.toString(), persistence);
        } catch (MqttException e) {
            throw new DaggerException(e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    public Options getOptions() {
        return options;
    }

    public void start() throws DaggerException {
        try {
            this.client.connect(this.options.getMqttConnectOptions());
            this.client.setCallback(this);
            this.client.subscribe("latest:block");
        } catch(MqttException e) {
            throw new DaggerException(e.getMessage());
        }
    }

    public void stop() throws DaggerException {
        try {
            this.client.disconnect();
        } catch(MqttException e) {
            throw new DaggerException(e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.onMessage(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }

    public Dagger on(String eventName, Listener listener) throws DaggerException {
        return this.addListener(eventName, listener);
    }

    public Dagger off(String eventName, Listener listener) throws DaggerException {
        return this.removeListener(eventName, listener);
    }

    public Dagger addListener(String eventName, Listener listener) throws DaggerException {
        MqttRegex mqttRegex = new MqttRegex(eventName);
        if (!this.regexTopics.containsKey(mqttRegex.getTopic())) {
            // subscribe events from server using topic
            try {
                this.client.subscribe(mqttRegex.getTopic());
            } catch (MqttException e) {
                throw new DaggerException(e.getMessage());
            }

            // add MQTT regex into regex topics
            this.regexTopics.put(mqttRegex.getTopic(), mqttRegex);
        }

        List<Listener> list = this.getEventListeners(eventName);
        list.add(listener);
        return this;
    }

    public Dagger removeListener(String eventName, Listener listener) throws DaggerException {
        MqttRegex mqttRegex = new MqttRegex(eventName);

        // if listener count is zero, unsubscribe topic and delete from `_regexTopics`
        if (this.getEventListeners(eventName).size() == 0) {
            // unsubscribe events from server
            try {
                this.client.unsubscribe(mqttRegex.getTopic());
            } catch (MqttException e) {
                throw new DaggerException(e.getMessage());
            }

            // remove MQTT regex from regex topics
            this.regexTopics.remove(mqttRegex.getTopic());
        }

        List<Listener> list = this.getEventListeners(eventName);
        list.remove(listener);
        return this;
    }

    // Remove all listeners
    public void removeAllListeners(String eventName) throws DaggerException {
        MqttRegex mqttRegex = new MqttRegex(eventName);
        try {
            this.client.unsubscribe(mqttRegex.getTopic());
        } catch (MqttException e) {
            throw new DaggerException(e.getMessage());
        }

        this.regexTopics.remove(mqttRegex.getTopic());
        this.listeners.put(eventName, new ArrayList<>());
    }

    // Get matching topics
    public List<String> getMatchingTopics(String eventName) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, MqttRegex> entry : this.regexTopics.entrySet()) {
            MqttRegex me = entry.getValue();
            if (me != null && me.matches(eventName)) {
                result.add(me.getTopic());
            }
        }

        return result;
    }

    // Get all subscriptions
    public Set<String> getSubscriptions() {
        return this.regexTopics.keySet();
    }

    // Get room instance
    public Room of(RoomType roomType) throws DaggerException {
        return new Room(this, roomType);
    }


    //
    // Private methods
    //

    private void onMessage(String topic, MqttMessage message) {
        String payload = message.getPayload().toString();

        // emit any message
        this.emit(MESSAGE, message.getPayload());

        // emit events to matching listeners
        this.getMatchingTopics(topic).forEach(eventName -> {
            this.emit(eventName, message.getPayload());
        });
    }

    private void emit(String eventName, byte[] payload) {
        // execute callback in all events
        this.getEventListeners(eventName).forEach(listener -> {
            listener.Callback(eventName, payload);
        });
    }

    // Get all event listeners
    private List<Listener> getEventListeners(String eventName) {
        if (!this.listeners.containsKey(eventName)) {
            this.listeners.put(eventName, new ArrayList<>());
        }

        return this.listeners.get(eventName);
    }
}
