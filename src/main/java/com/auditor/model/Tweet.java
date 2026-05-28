package com.auditor.model;


public class Tweet {
    private String id;
    private String text;
    private String createdAt;
    private String url;

    public Tweet (String id, String text, String createdAt, String url) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId (String id) {
        this.id = id;
    }

    public void setText (String text) {
        this.text = text;
    }

    public void setCreatedAt (String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
