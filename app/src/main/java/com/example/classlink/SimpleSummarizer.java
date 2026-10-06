package com.example.classlink;

import java.util.*;
import java.util.regex.Pattern;

public class SimpleSummarizer {

    // Small English stopword list; add more if needed
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a","an","the","is","are","was","were","in","on","and","or","of","to","for","with","that",
            "this","it","by","as","at","from","be","has","have","had","but","not","they","their","we",
            "you","I","he","she","them","his","her","its","which","will","can","would","should"
    ));

    // Summarize: return up to maxSentences most-important sentences (keeps original order)
    public static String summarize(String text, int maxSentences) {
        if (text == null) return "";
        text = text.trim();
        if (text.length() == 0) return "";

        // split into sentences (works for English punctuation; fallback to newline)
        String[] rawSentences = text.split("(?<=[.!?])\\s+");
        if (rawSentences.length == 1) {
            // maybe no punctuation, split by newline or by length segments
            if (text.contains("\n")) rawSentences = text.split("\\n+");
            else rawSentences = splitByLength(text, 200); // fallback
        }

        List<String> sentences = new ArrayList<>();
        for (String s : rawSentences) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) sentences.add(trimmed);
        }

        if (sentences.size() <= maxSentences) {
            // text already short
            return String.join(" ", sentences);
        }

        // build word frequencies across entire text
        Map<String, Integer> freq = new HashMap<>();
        Pattern wordPattern = Pattern.compile("\\w+");

        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            MatcherIter(wordPattern, lower, token -> {
                if (token.length() <= 1) return;
                if (STOPWORDS.contains(token)) return;
                freq.put(token, freq.getOrDefault(token, 0) + 1);
            });
        }

        // if no freq tokens (maybe language different), fallback: first N sentences
        if (freq.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(maxSentences, sentences.size()); i++) {
                sb.append(sentences.get(i)).append(" ");
            }
            return sb.toString().trim();
        }

        // compute sentence scores
        double maxFreq = Collections.max(freq.values());
        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            normalized.put(e.getKey(), e.getValue() / maxFreq);
        }

        Map<Integer, Double> sentenceScore = new HashMap<>();
        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i).toLowerCase();
            final double[] score = {0.0};
            MatcherIter(wordPattern, s, token -> {
                Double v = normalized.get(token);
                if (v != null) score[0] += v;
            });
            sentenceScore.put(i, score[0]);
        }

        // pick top N sentence indices by score
        List<Integer> indices = new ArrayList<>(sentenceScore.keySet());
        indices.sort((a,b) -> Double.compare(sentenceScore.get(b), sentenceScore.get(a))); // desc by score

        List<Integer> top = indices.subList(0, Math.min(maxSentences, indices.size()));
        // keep original order
        Collections.sort(top);

        StringBuilder out = new StringBuilder();
        for (int idx : top) {
            out.append(sentences.get(idx)).append(" ");
        }
        return out.toString().trim();
    }

    // small helper to iterate regex matches (avoids exposing java.util.regex.Matcher in lambdas)
    private interface TokenConsumer { void accept(String token); }
    private static void MatcherIter(Pattern p, String text, TokenConsumer c) {
        java.util.regex.Matcher m = p.matcher(text);
        while (m.find()) {
            c.accept(m.group());
        }
    }

    // fallback: split a long string into approximate sentence-chunks by length
    private static String[] splitByLength(String text, int chunkSize) {
        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(text.length(), i + chunkSize);
            parts.add(text.substring(i, end));
            i = end;
        }
        return parts.toArray(new String[0]);
    }
}
