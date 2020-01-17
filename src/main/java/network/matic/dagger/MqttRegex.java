package network.matic.dagger;

import network.matic.dagger.exceptions.DaggerException;

import java.util.regex.Pattern;

enum TokenType {
    SINGLE, MULTI, RAW;
}

class Token {
    private TokenType type;
    private String name;
    private String piece;
    private String last;

    public Token(TokenType type, String name, String piece, String last) {
        this.type = type;
        this.name = name;
        this.piece = piece;
        this.last = last;
    }

    public TokenType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPiece() {
        return piece;
    }

    public String getLast() {
        return last;
    }
}

public class MqttRegex {
    private String topic;
    private String rawTopic;
    private Pattern regexp;

    public MqttRegex(String t) throws DaggerException {
        String topic = t.toLowerCase();
        String[] tokens = MqttRegex.tokanize(topic);

        this.topic = String.join("/", tokens);
        this.rawTopic = topic;

        Token[] tokenObjects = new Token[tokens.length];
        for (int index = 0; index < tokens.length; index++) {
            tokenObjects[index] = MqttRegex.processToken(tokens[index], index, tokens);
        }

        this.regexp = MqttRegex.makeRegex(tokenObjects);
    }

    public boolean matches(String rawTopic) {
        String topic = rawTopic.toLowerCase();
        return this.regexp.matcher(topic).matches();
    }

    public String getTopic() {
        return topic;
    }

    public String getRawTopic() {
        return rawTopic;
    }

    public Pattern getRegexp() {
        return regexp;
    }

    public static String[] tokanize(String topic) {
        topic = topic.toLowerCase();
        String[] tokens = topic.split("/");
        if (tokens.length >= 4 && tokens[0].contains(":log")) {
            for (int i = 4; i < tokens.length; i++) {
                if (!tokens[i].equals("+") && !tokens[i].equals("#")) {
                    tokens[i] = Numeric.toStringPadded(tokens[i], 64);
                }
            }
        }

        return tokens;
    }

    public static Pattern makeRegex(Token[] tokens) {
        Token lastToken = tokens[tokens.length - 1];
        String[] regexTokens = new String[tokens.length];

        String[] result = new String[tokens.length];
        for (int index = 0; index < tokens.length; index++) {
            Token token = tokens[index];
            boolean isLast = index == tokens.length - 1;
            boolean beforeMulti = index == tokens.length - 2 && lastToken.getType() == TokenType.MULTI;
            result[index] = isLast || beforeMulti ? token.getLast() : token.getPiece();
        }

        return Pattern.compile(String.format("^%s$", String.join("", result)));
    }

    public static Token processToken(String token, int index, String[] tokens) throws DaggerException {
        boolean last = index == tokens.length - 1;

        if (token == null || "".equals(token.trim())) {
            throw new DaggerException("Topic must not be empty in pattern path.");
        }

        String cleanToken = token.trim();
        if (cleanToken.charAt(0) == '+') {
            return new Token(TokenType.SINGLE, "", "([^/#+]+/)", "([^/#+]+/?)");
        } else if (cleanToken.charAt(0) == '#') {
            if (!last) {
                throw new DaggerException("# wildcard must be at the end of the pattern");
            }
            return new Token(TokenType.MULTI, "#", "((?:[^/#+]+/)*)", "((?:[^/#+]+/?)*)");
        }

        return new Token(TokenType.RAW, cleanToken, String.format("%s/", cleanToken), String.format("%s/?", cleanToken));
    }
}
