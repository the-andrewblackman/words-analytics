package com.words.controller;

import com.words.service.PosTaggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/screen")
public class ScreenplayController {

    @Autowired
    private PosTaggingService posTaggingService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Map<String, Map<String, Integer>>>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.endsWith(".fdx")) {
                file = posTaggingService.convertFdxToFountain(file);
            }
            Map<String, Map<String, Map<String, Integer>>> result = posTaggingService.processFountainFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }




}
