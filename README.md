# Tweet-Auditor

A Java-powered application that analyzes your X (Twitter) archive using Google Gemini AI to identify and flag tweets for deletion based on a set criteria.

### High level flow of application
![alt text](src\main\resources\static\image.png)
---

## Features

- Leverages Google Gemini as intelligent analyser
- Analyzes original tweets and replies from your X archive
- Your own criteria for tweet deletion
- Processes large archives in configurable batches
- Mark tweets locally to delete at convenience

---

## Quick Start

### Prerequisites

- Java 26.0.1
- Springboot 4.0.6
- Google Gemini API key
- Your X (Twitter) archive

### 📋 How to Get Your X Archive

1. Log in to [X (Twitter)](https://x.com)
2. Navigate to: **More → Settings and privacy → Your account → Download an archive of your data**
3. Verify your identity
4. Wait 24-48 hours for the email with download link
5. Download and extract the ZIP file
6. Upload the ZIP file to Tweet Audit

### How to get Google Gemini Api (Free Tier Available)

1. Visit [Google AI Studio](https://aistudio.google.com/)
2. Sign in with your Google account
3. Click **Get API Key**
4. Create a new API key
5. Copy and save the key

**Cost**: Free tier available with generous quotas

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/ifaycodes/tweet-auditor.git
cd tweet-auditor
```

2. **Create config file**

Create a config.json file in the project root:
```{
    "apiKey" = "yourgeminiapikey"
    "username" = "youraccount username"
    "criteria" = [
        "professional check",
        "tone is not respectful and thoughtful etc",

        "list of all your criteria",
        "in sentence form"
    ]
}
```

## Architecture

### Key Components

```
tweet-audit/
├── src/
│   ├── main/java/com/tweetauditor/
│   │   ├── auditor/
|   |   |   └── AuditorApplication.java
│   │   ├── config/
│   │   │   └── AppConfig.java
│   │   ├── model/
│   │   │   └── Tweet.java
│   │   ├── parser/
│   │   │   └── TweetParser.java
│   │   ├── batching/
│   │   │   └── BatchBuilder.java
│   │   ├── gemini/
│   │   │   ├── GeminiClient.java
|   |   |   ├── RateLimitException.java
│   │   │   └── EvaluationResult.java
│   │   ├── checkpoint/
│   │   │   └── CheckPointStore.java
│   │   └── output/
│   │       └── CsvWriter.java
│   └── test/java/com/tweetaudit/
│       ├── config/
│       │   └── AppConfigTest.java
│       ├── parser/
│       │   └── TweetParserTest.java
│       ├── batching/
│       │   └── BatchBuilderTest.java
│       ├── gemini/
│       │   └── GeminiClientTest.java
│       ├── checkpoint/
│       │   └── CheckpointStoreTest.java
│       └── output/
│           └── CsvWriterTest.java
├── data/
│   └── tweets.js
├── config.json
├── flagged_tweets.csv
├── checkpoint.txt
├── README.md
├──TRADEOFFS.md
└── pom.xml
```


---

## Usage Guide

### Step 1: Start an Audit

Add X archive file to the project root

### Step 2: Review Flagged Tweets and Take actions

- View all flagged tweets in the csv file
- See the **reason** each tweet was flagged
- Click on link to see the original tweet on X
- Manually delete it on X

