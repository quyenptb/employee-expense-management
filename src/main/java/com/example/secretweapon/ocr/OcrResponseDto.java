package com.example.secretweapon.ocr;


public class OcrResponseDto {
    private String detectedTitle;
    private Double detectedAmount;

    public OcrResponseDto(String detectedTitle, Double detectedAmount) {
        this.detectedTitle = detectedTitle;
        this.detectedAmount = detectedAmount;
    }

    public String getDetectedTitle() { return detectedTitle; }
    public Double getDetectedAmount() { return detectedAmount; }
}