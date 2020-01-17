package network.matic.dagger.test;

import com.google.gson.Gson;
import network.matic.dagger.MqttRegex;

import network.matic.dagger.Strings;
import network.matic.dagger.exceptions.DaggerException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

class Topic {
    private String topic;
    private String[] tokens;
    private Map<String, Boolean> matches;

    public Topic(String topic, String[] tokens, Map<String, Boolean> matches) {
        this.topic = topic;
        this.tokens = tokens;
        this.matches = matches;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String[] getTokens() {
        return tokens;
    }

    public void setTokens(String[] tokens) {
        this.tokens = tokens;
    }

    public Map<String, Boolean> getMatches() {
        return matches;
    }

    public void setMatches(Map<String, Boolean> matches) {
        this.matches = matches;
    }
}

public class TestMqttRegex {
    private Topic[] topics;

    @Before
    public void setup() throws DaggerException, FileNotFoundException {
        Gson gson = new Gson();
        File file = new File(
                getClass().getClassLoader().getResource("mqtt-regex.json").getFile()
        );
        topics = gson.fromJson(new FileReader(file), Topic[].class);
    }

    @Test
    public void test() throws DaggerException {
        for (Topic t: topics) {
            MqttRegex mqttRegex = new MqttRegex(t.getTopic());

            // check tokens
            assertArrayEquals(MqttRegex.tokanize(t.getTopic()), t.getTokens());

            // check matches
            t.getMatches().forEach((key, value)  -> {
                assertEquals(String.format("Topic `%s` should%swith match with `%s`", key, value ? " not " : " ", t.getTopic()), mqttRegex.matches(key), value);
            });
        }
    }
}
