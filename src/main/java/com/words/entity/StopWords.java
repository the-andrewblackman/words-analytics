package com.words.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWords {
        private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is",
                "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
                "these", "they", "this", "to", "was", "will", "with", "i", "you", "he", "she", "we",
                "they", "me", "him", "her", "them", "we", "at"
        ));

        public static boolean isStopWord(String word) {
            return STOP_WORDS.contains(word);
        }
    }

