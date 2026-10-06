


package com.example.classlink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExtractiveSummarizer {

    public static CharSequence getSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "⚡ No content to summarize.";
        }

        // 1. Split text into sentences
        List<String> sentences = splitIntoSentences(text);

        // 2. Rank sentences based on keyword frequency and position
        List<SentenceScore> scoredSentences = new ArrayList<>();
        int sentenceIndex = 0;
        for (String sentence : sentences) {
            int score = calculateScore(sentence, sentences.size(), sentenceIndex);
            scoredSentences.add(new SentenceScore(sentence, score, sentenceIndex));
            sentenceIndex++;
        }

        // 3. Sort by score in descending order
        Collections.sort(scoredSentences, Comparator.comparingInt(SentenceScore::getScore).reversed());

        // 4. Select a percentage of top sentences for the summary (e.g., 30%)
        int summarySize = (int) Math.ceil(scoredSentences.size() * 0.3);
        if (summarySize == 0 && !scoredSentences.isEmpty()) {
            summarySize = 1;
        }

        List<SentenceScore> topSentences = scoredSentences.stream()
                .limit(summarySize)
                .collect(Collectors.toList());

        // 5. Sort the top sentences by their original position to maintain flow
        Collections.sort(topSentences, Comparator.comparingInt(SentenceScore::getOriginalIndex));

        // 6. Build the final bulleted summary
        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("✨ AI Summary:\n\n");
        for (SentenceScore s : topSentences) {
            summaryBuilder.append("• ").append(s.getSentence().trim()).append("\n\n");
        }

        return summaryBuilder.toString().trim();
    }

    private static List<String> splitIntoSentences(String text) {
        Pattern pattern = Pattern.compile("(?<=[.!?])\\s*");
        return Arrays.asList(pattern.split(text));
    }

    private static int calculateScore(String sentence, int totalSentences, int index) {
        // Simple scoring based on keywords and position
        int score = 0;

        // Keywords related to acknowledgements
        String[] keywords = {"express", "thankful", "thanks", "deepest", "gratitude", "support", "help", "appreciation"};
        for (String keyword : keywords) {
            if (sentence.toLowerCase().contains(keyword)) {
                score += 10;
            }
        }

        // Position bonus: give higher scores to sentences at the beginning or end
        if (index == 0 || index == totalSentences - 1) {
            score += 20;
        } else if (index < 3 || index > totalSentences - 4) {
            score += 10;
        }

        return score;
    }

    private static class SentenceScore {
        private final String sentence;
        private final int score;
        private final int originalIndex;

        public SentenceScore(String sentence, int score, int originalIndex) {
            this.sentence = sentence;
            this.score = score;
            this.originalIndex = originalIndex;
        }

        public String getSentence() {
            return sentence;
        }

        public int getScore() {
            return score;
        }

        public int getOriginalIndex() {
            return originalIndex;
        }
    }
}

/*
package com.example.classlink;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExtractiveSummarizer {

    private static final Random random = new Random();

    public static CharSequence getSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "⚡ No content to summarize.";
        }

        // Split text into sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");
        List<String> refinedSentences = new ArrayList<>();

        int total = sentences.length;
        int points;

        // Dynamic bullet count
        if (total <= 3) {
            points = total; // short notes
        } else if (total <= 6) {
            points = total; // medium notes
        } else {
            points = Math.min(10, total); // long notes
        }

        // Refine sentences and replace synonyms
        for (int i = 0; i < points; i++) {
            String refined = refineSentence(sentences[i]);
            refined = replaceSynonyms(refined);
            refinedSentences.add(refined);
        }

        // Shuffle for natural flow
        Collections.shuffle(refinedSentences);

        // Build bullet-style summary
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("✨ AI Summary:\n\n");

        for (String s : refinedSentences) {
            SpannableString spannable = new SpannableString(s);
            spannable.setSpan(new BulletSpan(20), 0, spannable.length(), 0);
            builder.append(spannable).append("\n\n");
        }

        return builder;
    }

    // 🔹 Refine sentence: full sentence, remove filler, capitalize
    private static String refineSentence(String sentence) {
        String refined = sentence.trim();

        // Remove common filler words
        refined = refined.replaceAll("\\b(however|therefore|overall|basically|actually|very|just|really|so|then)\\b", "")
                .replaceAll("\\s+", " ").trim();

        // Capitalize first letter
        if (!refined.isEmpty()) {
            refined = refined.substring(0, 1).toUpperCase() + refined.substring(1);
        }

        return refined;
    }

    // 🔹 Synonym replacement
    private static String replaceSynonyms(String sentence) {
        String[][] synonyms = {
                {"good", "excellent|superb|great"},
                {"help", "assist|support|aid"},
                {"understand", "grasp|comprehend|learn"},
                {"use", "utilize|employ|apply"},
                {"show", "demonstrate|display|present"},
                {"big", "large|major|significant"},
                {"important", "crucial|vital|key"},
                {"many", "numerous|several|countless"},
                {"need", "require|necessitate|demand"},
                {"increase", "boost|enhance|raise"},
                {"reduce", "decrease|lower|minimize"}
        };

        String result = sentence;
        for (String[] pair : synonyms) {
            String word = pair[0];
            String[] options = pair[1].split("\\|");
            String replacement = options[random.nextInt(options.length)];
            result = result.replaceAll("(?i)\\b" + word + "\\b", replacement);
        }

        return result;
    }
}
*/




