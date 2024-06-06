package com.words.service;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PosTaggingService {
    private final StanfordCoreNLP pipeline;

    public PosTaggingService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos");
        pipeline = new StanfordCoreNLP(props);
    }

    public Map<String, List<String>> tagWords(List<String> words) {
        Map<String, List<String>> taggedWords = new HashMap<>();
        taggedWords.put("NOUN", new ArrayList<>());
        taggedWords.put("VERB", new ArrayList<>());
        taggedWords.put("ADJECTIVE", new ArrayList<>());
        taggedWords.put("ADVERB", new ArrayList<>());
        taggedWords.put("PRONOUN", new ArrayList<>());
        taggedWords.put("CONTRACTIONS", new ArrayList<>());
        taggedWords.put("PREPOSITION", new ArrayList<>());
        taggedWords.put("CONJUNCTION", new ArrayList<>());
        taggedWords.put("INTERJECTION", new ArrayList<>());
        taggedWords.put("DETERMINER", new ArrayList<>());
        taggedWords.put("PARTICLE", new ArrayList<>());
        taggedWords.put("EXISTENTIAL", new ArrayList<>());
        taggedWords.put("FOREIGN", new ArrayList<>());
        taggedWords.put("LIST_ITEM", new ArrayList<>());
        taggedWords.put("POSSESSIVE", new ArrayList<>());
        taggedWords.put("SYMBOL", new ArrayList<>());
        taggedWords.put("CARDINAL", new ArrayList<>());
        taggedWords.put("SLANG", new ArrayList<>());
        taggedWords.put("ALL", new ArrayList<>()); // To store all words with counts

        Pattern contractionPattern = Pattern.compile("\\b\\w+('[a-zA-Z]+)?\\b");

        for (String word : words) {
            if (word.contains("?") || word.contains(".") || word.contains("!")) {
                word = word.substring(0, word.length() - 1);
            }

            if (word.contains("'") || word.contains("’")) {
                word = word.replace("’", "'");
                addWordToTaggedWords(taggedWords, word, "CONTRACTIONS");
                continue;
            }

            Matcher matcher = contractionPattern.matcher(word);
            if (matcher.find() && matcher.group(1) != null) {
                addWordToTaggedWords(taggedWords, word, "CONTRACTIONS");
            } else {
                CoreDocument document = new CoreDocument(word);
                pipeline.annotate(document);

                List<CoreLabel> tokens = document.tokens();
                for (CoreLabel token : tokens) {
                    addWordToTaggedWords(taggedWords, token.word(), token.tag());
                }
            }
        }

        combineWords(taggedWords); // Consolidate all words and add to "ALL"

        return taggedWords;
    }

    private void addWordToTaggedWords(Map<String, List<String>> taggedWords, String word, String pos) {

        switch (pos) {
            case "PRP":
            case "PRP$":
            case "WP":
            case "WP$":
                taggedWords.get("PRONOUN").add(word);
                break;
            case "NN":
            case "NNS":
            case "NNP":
            case "NNPS":
                taggedWords.get("NOUN").add(word);
                break;
            case "VB":
            case "VBD":
            case "VBG":
            case "VBN":
            case "VBP":
            case "VBZ":
            case "MD":
                taggedWords.get("VERB").add(word);
                break;
            case "JJ":
            case "JJR":
            case "JJS":
                taggedWords.get("ADJECTIVE").add(word);
                break;
            case "RB":
            case "RBR":
            case "RBS":
            case "WRB":
                taggedWords.get("ADVERB").add(word);
                break;
            case "TO":
            case "RP":
                taggedWords.get("PARTICLE").add(word);
                break;
            case "IN":
                taggedWords.get("PREPOSITION").add(word);
                break;
            case "CC":
                taggedWords.get("CONJUNCTION").add(word);
                break;
            case "UH":
                taggedWords.get("INTERJECTION").add(word);
                break;
            case "DT":
            case "PDT":
            case "WDT":
                taggedWords.get("DETERMINER").add(word);
                break;
            case "EX":
                taggedWords.get("EXISTENTIAL").add(word);
                break;
            case "FW":
                taggedWords.get("FOREIGN").add(word);
                break;
            case "LS":
                taggedWords.get("LIST_ITEM").add(word);
                break;
            case "POS":
                taggedWords.get("POSSESSIVE").add(word);
                break;
            case "SYM":
                taggedWords.get("SYMBOL").add(word);
                break;
            case "CD":
                taggedWords.get("CARDINAL").add(word);
                break;
            case "CONTRACTIONS":
                taggedWords.get("CONTRACTIONS").add(word);
                break;
            default:
                taggedWords.get("SLANG").add(word);
                break;
        }
    }

    private void combineWords(Map<String, List<String>> taggedWords) {
        Map<String, Integer> wordCounts = new HashMap<>();

        // Consolidate all words from the POS lists
        for (Map.Entry<String, List<String>> entry : taggedWords.entrySet()) {
            if (!entry.getKey().equals("ALL")) {
                for (String word : entry.getValue()) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Create a sorted list of words with their counts
        List<String> sortedWords = new ArrayList<>();
        wordCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sortedWords.add(entry.getKey() + ": " + entry.getValue()));

        // Add to the "ALL" list
        taggedWords.get("ALL").addAll(sortedWords);
    }
    public MultipartFile convertFdxToFountain(MultipartFile file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file.getInputStream());
        doc.getDocumentElement().normalize();

        NodeList paragraphs = doc.getElementsByTagName("Paragraph");

        StringBuilder fountainContent = new StringBuilder();

        for (int i = 0; i < paragraphs.getLength(); i++) {
            Node paragraphNode = paragraphs.item(i);
            if (paragraphNode.getNodeType() == Node.ELEMENT_NODE) {
                Element paragraphElement = (Element) paragraphNode;
                String paragraphType = paragraphElement.getAttribute("Type");
                NodeList textNodes = paragraphElement.getElementsByTagName("Text");

                StringBuilder paragraphText = new StringBuilder();
                for (int j = 0; j < textNodes.getLength(); j++) {
                    paragraphText.append(textNodes.item(j).getTextContent());
                }

                switch (paragraphType) {
                    case "Scene Heading":
                        fountainContent.append("\n\n").append(paragraphText.toString().toUpperCase()).append("\n");
                        break;
                    case "Action":
                        fountainContent.append(paragraphText.toString()).append("\n\n");
                        break;
                    case "Character":
                        fountainContent.append("\n").append(paragraphText.toString().toUpperCase()).append("\n");
                        break;
                    case "Dialogue":
                        fountainContent.append(paragraphText.toString()).append("\n");
                        break;
                    case "Parenthetical":
                        fountainContent.append("(").append(paragraphText.toString()).append(")\n");
                        break;
                    case "Transition":
                        fountainContent.append("\n> ").append(paragraphText.toString().toUpperCase()).append("\n");
                        break;
                    default:
                        fountainContent.append(paragraphText.toString()).append("\n");
                        break;
                }
            }
        }

        // Write the fountain content to a temporary file
        File tempFile = File.createTempFile("temp", ".fountain");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(fountainContent.toString());
        }

        // Convert the temporary file to MultipartFile
        return new MultipartFile() {
            @Override
            public String getName() {
                return tempFile.getName();
            }

            @Override
            public String getOriginalFilename() {
                return tempFile.getName();
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public boolean isEmpty() {
                return tempFile.length() == 0;
            }

            @Override
            public long getSize() {
                return tempFile.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(tempFile.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(tempFile);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.copy(tempFile.toPath(), dest.toPath());
            }
        };
    }

    public Map<String, Map<String, Map<String, Integer>>> processFountainFile(MultipartFile file) throws Exception {
        Map<String, Map<String, Map<String, Integer>>> characterWordCounts = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            String currentCharacter = null;
            while ((line = reader.readLine()) != null) {
                // Skip lines that start with "INT." or "EXT."
                if (line.trim().startsWith("INT.") || line.trim().startsWith("EXT.")) {
                    continue;
                }
                if (line.trim().startsWith("(") || line.trim().endsWith(")")) {
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
                    Map<String, List<String>> taggedWords = this.tagWords(wordList);

                    for (Map.Entry<String, List<String>> entry : taggedWords.entrySet()) {
                        String pos = entry.getKey();
                        List<String> classifiedWords = entry.getValue();

                        Map<String, Integer> wordCount = posCounts.computeIfAbsent(pos, k -> new HashMap<>());
                        for (String word : classifiedWords) {
                            wordCount.merge(word, 1, Integer::sum);
                        }
                    }

                    // Add the words to the allWords map
                    addAllWords(taggedWords);
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

    public void addAllWords(Map<String, List<String>> taggedWords) {
        Map<String, Integer> wordCounts = new HashMap<>();

        // Consolidate all words from the POS lists
        for (Map.Entry<String, List<String>> entry : taggedWords.entrySet()) {
            if (!entry.getKey().equals("ALL")) {
                for (String word : entry.getValue()) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Create a sorted list of words with their counts
        List<String> sortedWords = wordCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());

        // Add to the "ALL" list
        taggedWords.get("ALL").addAll(sortedWords);
    }
}
