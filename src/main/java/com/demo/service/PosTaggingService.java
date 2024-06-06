package com.demo.service;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        taggedWords.put("CONTRACTIONS", new ArrayList<>()); // Separate category for contractions

        Pattern contractionPattern = Pattern.compile("\\b\\w+('[a-zA-Z]+)?\\b");

        for (String word : words) {
//            System.out.println(word);
            if (word.contains("?") || word.contains(".") || word.contains("!") || word.contains(")")) {
                word = word.substring(0,word.length()-1);
            }

            if(word.contains("'") || word.contains("’")){
//               System.out.println(word);
                word = word.replace("’", "'");
                addWordToTaggedWords(taggedWords,word,"CONTRACTIONS");
                continue;
            }
            Matcher matcher = contractionPattern.matcher(word);

            if (matcher.find() && matcher.group(1) != null) { // If the word is a contraction
                addWordToTaggedWords(taggedWords, word, "CONTRACTIONS");
            } else {
                CoreDocument document = new CoreDocument(word);
                pipeline.annotate(document);

                List<CoreLabel> tokens = document.tokens();
//                System.out.println(Arrays.asList(tokens));
                for (CoreLabel token : tokens) {

                    addWordToTaggedWords(taggedWords, token.word(), token.tag());
                }
            }
        }

        return taggedWords;
    }

    private void addWordToTaggedWords(Map<String, List<String>> taggedWords, String word, String pos) {
        switch (pos) {
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
                taggedWords.get("ADVERB").add(word);
                break;
            case "CONTRACTIONS":
                System.out.println(word);
                taggedWords.get("CONTRACTIONS").add(word);
                break;
            default:
                // Handle other POS tags if needed
                break;
        }
    }
}
