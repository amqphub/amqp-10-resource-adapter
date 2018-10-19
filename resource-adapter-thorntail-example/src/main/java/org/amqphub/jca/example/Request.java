package org.amqphub.jca.example;

public class Request {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("Request{text=%s}", text);
    }
}
