package com.example.secretweapon.ocr;


import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(\\d{1,3}[.,]?\\d{3})|(\\d+)"
    );

    public OcrResponseDto extractDataFromReceipt(MultipartFile file) throws IOException {
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {

            //File Upload -> ByteString
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();


            Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            //Receive result
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                throw new IOException("Error form Google Vision API: " + res.getError().getMessage());
            }

            String fullText = res.getFullTextAnnotation().getText();
            return parseText(fullText);
        }
    }

    //
    private OcrResponseDto parseText(String text) {
        String detectedTitle = "Cannot detect";
        Double detectedAmount = 0.0;

        //split it into lines
        String[] lines = text.split("\n");

        if (lines.length > 0) {
            //Rule 1: Get the first line to be header
            detectedTitle = lines[0];
        }

        //Rule 2: Find the lines has "TOTAL" or "TỔNG"
        for (String line : lines) {
            String upperLine = line.toUpperCase();
            if (upperLine.contains("TOTAL") || upperLine.contains("TỔNG") || upperLine.contains("AMOUNT")) {
                Matcher matcher = AMOUNT_PATTERN.matcher(line);
                if (matcher.find()) {
                    // Xử lý chuỗi số tiền (ví dụ: "150.000" -> 150000)
                    String amountStr = matcher.group(0).replaceAll("[.,]", "");
                    try {
                        detectedAmount = Double.parseDouble(amountStr);
                        break; // Dừng lại khi tìm thấy
                    } catch (NumberFormatException e) {
                        // Bỏ qua
                    }
                }
            }
        }

        // Nếu không tìm thấy "TOTAL", thử tìm số tiền lớn nhất
        if (detectedAmount == 0.0) {
            for (String line : lines) {
                Matcher matcher = AMOUNT_PATTERN.matcher(line);
                while (matcher.find()) {
                    String amountStr = matcher.group(0).replaceAll("[.,]", "");
                    try {
                        double currentAmount = Double.parseDouble(amountStr);
                        if(currentAmount > detectedAmount) {
                            detectedAmount = currentAmount; // Lấy số lớn nhất
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        }

        return new OcrResponseDto(detectedTitle, detectedAmount);
    }
}