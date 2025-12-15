package com.example.secretweapon.service;

import com.example.secretweapon.ocr.OcrResponseDto;
import com.example.secretweapon.ocr.OcrService;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrServiceTest {

    @InjectMocks
    private OcrService ocrService;

    @Test
    @DisplayName("extractDataFromReceipt_ValidImage_shouldReturnDto")
    void extractDataFromReceipt_ValidImage_shouldReturnDto() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake-bytes".getBytes());

        // Mock Static ImageAnnotatorClient
        try (MockedStatic<ImageAnnotatorClient> mockedStatic = Mockito.mockStatic(ImageAnnotatorClient.class)) {
            
            // Mock Client instance
            ImageAnnotatorClient mockClient = mock(ImageAnnotatorClient.class);
            mockedStatic.when(ImageAnnotatorClient::create).thenReturn(mockClient);

            // Mock Response
            BatchAnnotateImagesResponse batchResponse = mock(BatchAnnotateImagesResponse.class);
            AnnotateImageResponse response = mock(AnnotateImageResponse.class);
            TextAnnotation textAnnotation = mock(TextAnnotation.class);

            when(mockClient.batchAnnotateImages(anyList())).thenReturn(batchResponse);
            when(batchResponse.getResponses(0)).thenReturn(response);
            when(response.hasError()).thenReturn(false);
            when(response.getFullTextAnnotation()).thenReturn(textAnnotation);

            // Mock Content: Line 1 = Title, Line containing "TOTAL" = Amount
            String extractedText = "Highlands Coffee\nAddress: 123 ABC\nTOTAL: 55,000 VND\nThank you";
            when(textAnnotation.getText()).thenReturn(extractedText);

            // Act
            OcrResponseDto result = ocrService.extractDataFromReceipt(file);

            // Assert
            assertNotNull(result);
            assertEquals("Highlands Coffee", result.getDetectedTitle());
            assertEquals(55000.0, result.getDetectedAmount());
        }
    }

    @Test
    @DisplayName("extractDataFromReceipt_NoTotalKeyword_shouldFindLargestNumber")
    void extractDataFromReceipt_NoTotalKeyword_shouldFindLargestNumber() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake-bytes".getBytes());

        try (MockedStatic<ImageAnnotatorClient> mockedStatic = Mockito.mockStatic(ImageAnnotatorClient.class)) {
            ImageAnnotatorClient mockClient = mock(ImageAnnotatorClient.class);
            mockedStatic.when(ImageAnnotatorClient::create).thenReturn(mockClient);

            BatchAnnotateImagesResponse batchResponse = mock(BatchAnnotateImagesResponse.class);
            AnnotateImageResponse response = mock(AnnotateImageResponse.class);
            TextAnnotation textAnnotation = mock(TextAnnotation.class);

            when(mockClient.batchAnnotateImages(anyList())).thenReturn(batchResponse);
            when(batchResponse.getResponses(0)).thenReturn(response);
            when(response.hasError()).thenReturn(false);
            when(response.getFullTextAnnotation()).thenReturn(textAnnotation);

            // Mock Content: No "TOTAL", just numbers
            String extractedText = "Taxi Invoice\nTrip: 50.000\nSurcharge: 10.000\nFinal: 60.000";
            when(textAnnotation.getText()).thenReturn(extractedText);

            // Act
            OcrResponseDto result = ocrService.extractDataFromReceipt(file);

            // Assert
            assertEquals("Taxi Invoice", result.getDetectedTitle());
            assertEquals(60000.0, result.getDetectedAmount()); // Should pick largest number
        }
    }
    
    @Test
    @DisplayName("extractDataFromReceipt_EmptyText_shouldReturnDefault")
    void extractDataFromReceipt_EmptyText_shouldReturnDefault() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake-bytes".getBytes());

        try (MockedStatic<ImageAnnotatorClient> mockedStatic = Mockito.mockStatic(ImageAnnotatorClient.class)) {
            ImageAnnotatorClient mockClient = mock(ImageAnnotatorClient.class);
            mockedStatic.when(ImageAnnotatorClient::create).thenReturn(mockClient);

            BatchAnnotateImagesResponse batchResponse = mock(BatchAnnotateImagesResponse.class);
            AnnotateImageResponse response = mock(AnnotateImageResponse.class);
            TextAnnotation textAnnotation = mock(TextAnnotation.class);

            when(mockClient.batchAnnotateImages(anyList())).thenReturn(batchResponse);
            when(batchResponse.getResponses(0)).thenReturn(response);
            when(response.hasError()).thenReturn(false);
            when(response.getFullTextAnnotation()).thenReturn(textAnnotation);

            when(textAnnotation.getText()).thenReturn("");

            OcrResponseDto result = ocrService.extractDataFromReceipt(file);

            assertEquals("", result.getDetectedTitle());
            assertEquals(0.0, result.getDetectedAmount());
        }
    }
}