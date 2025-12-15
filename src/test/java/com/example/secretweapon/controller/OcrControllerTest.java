package com.example.secretweapon.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.secretweapon.ocr.OcrController;
import com.example.secretweapon.ocr.OcrResponseDto;
import com.example.secretweapon.ocr.OcrService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OcrControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private OcrController ocrController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ocrController).build();
    }

    @Test
    @DisplayName("uploadReceipt_ValidFile_shouldReturnExtractedData")
    void uploadReceipt_ValidFile_shouldReturnExtractedData() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "receipt.jpg", "image/jpeg", "data".getBytes()
        );
        OcrResponseDto mockResponse = new OcrResponseDto("Highlands Coffee", 55000.0);

        when(ocrService.extractDataFromReceipt(any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/ocr/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detectedTitle").value("Highlands Coffee"))
                .andExpect(jsonPath("$.detectedAmount").value(55000.0));
    }

    @Test
    @DisplayName("uploadReceipt_ServiceThrowsIOException_shouldReturn500")
    void uploadReceipt_ServiceThrowsIOException_shouldReturn500() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "error.jpg", "image/jpeg", "data".getBytes()
        );

        when(ocrService.extractDataFromReceipt(any())).thenThrow(new IOException("Vision API Error"));

        // Act & Assert
        mockMvc.perform(multipart("/api/ocr/upload").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("uploadReceipt_EmptyFile_shouldReturnBadRequest")
    void uploadReceipt_EmptyFile_shouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/ocr/upload").file(emptyFile))
                .andExpect(status().isBadRequest());
    }
}