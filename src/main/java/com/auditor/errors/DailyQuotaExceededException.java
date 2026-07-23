package com.auditor.errors;

//thrown when Gemini's 429 response indicates the daily quota (not the per-minute one) is exhausted. retrying this within seconds won't help,
//since the quota won't reset for hours, so callers should stop instead of burning through more attempts.

public class DailyQuotaExceededException extends RuntimeException {
    public DailyQuotaExceededException(String message) { super(message); }
}
