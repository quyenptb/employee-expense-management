package com.example.secretweapon.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping("/upload")
    public ResponseEntity<OcrResponseDto> uploadReceipt(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            OcrResponseDto data = ocrService.extractDataFromReceipt(file);
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.status(500).build();
        }
    }
}