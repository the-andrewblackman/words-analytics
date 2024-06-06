package com.demo.controller;

import com.demo.service.PosTaggingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/screenplay")
public class ScreenplayController {

    @Autowired
    private PosTaggingService posTaggingService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Map<String, Map<String, Integer>>>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Map<String, Map<String, Integer>>> result = processFountainFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    private Map<String, Map<String, Map<String, Integer>>> processFountainFile(MultipartFile file) throws Exception {
        Map<String, Map<String, Map<String, Integer>>> characterWordCounts = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            String currentCharacter = null;
            while ((line = reader.readLine()) != null) {
                // Skip lines that start with "INT." or "EXT."
                if (line.trim().startsWith("INT.") || line.trim().startsWith("EXT.")) {
                    continue;
                }

                if (line.matches("^[A-Z ]+$")) { // Character name in uppercase
                    currentCharacter = line.trim();
                } else if (currentCharacter != null && !line.trim().isEmpty()) { // Dialogue lines
                    Map<String, Map<String, Integer>> posCounts = characterWordCounts.computeIfAbsent(currentCharacter, k -> new HashMap<>());

                    // Replace spaces, periods, and commas with periods
                    line = line.replaceAll("[ ,.]", ".");

                    // Split the line into words using period as delimiter
                    String[] wordsArray = line.split("\\.");

                    List<String> wordList = Arrays.asList(wordsArray);

                    // Use the posTaggingService to classify the words
                    Map<String, List<String>> taggedWords = posTaggingService.tagWords(wordList);

                    for (Map.Entry<String, List<String>> entry : taggedWords.entrySet()) {
                        String pos = entry.getKey();
                        List<String> classifiedWords = entry.getValue();

                        Map<String, Integer> wordCount = posCounts.computeIfAbsent(pos, k -> new HashMap<>());
                        for (String word : classifiedWords) {
                           // String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z']", ""); // Clean non-alphabetic characters
                            wordCount.merge(word, 1, Integer::sum);
                        }
                    }
                }
            }
        }

        // Sort the word counts for each POS category and character
        Map<String, Map<String, Map<String, Integer>>> sortedCharacterWordCounts = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : characterWordCounts.entrySet()) {
            Map<String, Map<String, Integer>> sortedPosCounts = new HashMap<>();
            for (Map.Entry<String, Map<String, Integer>> posEntry : entry.getValue().entrySet()) {
                Map<String, Integer> sortedWords = posEntry.getValue().entrySet()
                        .stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort by count in descending order
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));
                sortedPosCounts.put(posEntry.getKey(), sortedWords);
            }
            sortedCharacterWordCounts.put(entry.getKey(), sortedPosCounts);
        }

        return sortedCharacterWordCounts;
    }
}