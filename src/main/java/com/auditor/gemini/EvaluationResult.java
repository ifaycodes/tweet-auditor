package com.auditor.gemini;
 
public class EvaluationResult {
 
    private String tweetId;
    private boolean flagged;
    private String reason;
    private String tweetUrl;
 
    public EvaluationResult(String tweetId, boolean flagged, String reason, String tweetUrl) {
        this.tweetId = tweetId;
        this.flagged = flagged;
        this.reason = reason;
        this.tweetUrl = tweetUrl;

    }
 
    public String getTweetId() { return tweetId; }
    public boolean isFlagged() { return flagged; }
    public String getReason() { return reason; }
    public String getTweetUrl() { return tweetUrl; }
 
    public void setTweetId(String tweetId) { this.tweetId = tweetId; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    public void setReason(String reason) { this.reason = reason; }
    public void setTweetUrl(String tweetUrl) { this.tweetUrl = tweetUrl; }
}