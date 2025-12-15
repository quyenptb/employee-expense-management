package com.example.secretweapon.service.impl;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseStorageServiceImplTest {

    @InjectMocks
    private FirebaseStorageServiceImpl firebaseStorageService;

    @Test
    @DisplayName("uploadFile_Success_shouldReturnPublicUrl")
    void uploadFile_Success_shouldReturnPublicUrl() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes()
        );

        // Mock Static StorageClient
        try (MockedStatic<StorageClient> mockedStorage = Mockito.mockStatic(StorageClient.class)) {
            StorageClient mockClient = mock(StorageClient.class);
            Bucket mockBucket = mock(Bucket.class);
            Blob mockBlob = mock(Blob.class);

            mockedStorage.when(StorageClient::getInstance).thenReturn(mockClient);
            when(mockClient.bucket()).thenReturn(mockBucket);
            when(mockBucket.getName()).thenReturn("test-bucket");
            
            // Mock create blob
            when(mockBucket.create(anyString(), any(InputStream.class), anyString()))
                    .thenReturn(mockBlob);

            // Act
            String url = firebaseStorageService.uploadFile(file);

            // Assert
            assertNotNull(url);
            assertTrue(url.startsWith("https://storage.googleapis.com/test-bucket/"));
            assertTrue(url.endsWith("_test.jpg"));
            
            verify(mockBlob).createAcl(any(Acl.class));
        }
    }

    @Test
    @DisplayName("uploadFile_IOException_shouldThrowRuntimeException")
    void uploadFile_IOException_shouldThrowRuntimeException() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        //when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream()).thenThrow(new IOException("Disk error"));

        // QUAN TRỌNG: Phải mock Static StorageClient cả trong case lỗi này,
        // nếu không nó sẽ cố gọi Google Cloud thật và sinh ra lỗi Permission Denied.
        try (MockedStatic<StorageClient> mockedStorage = Mockito.mockStatic(StorageClient.class)) {
            StorageClient mockClient = mock(StorageClient.class);
            Bucket mockBucket = mock(Bucket.class);

            mockedStorage.when(StorageClient::getInstance).thenReturn(mockClient);
            when(mockClient.bucket()).thenReturn(mockBucket);
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                firebaseStorageService.uploadFile(file)
            );
            
            String expectedMessage = "Lỗi khi upload file";
            assertTrue(exception.getMessage().contains(expectedMessage), 
                "Message should contain '" + expectedMessage + "' but was: " + exception.getMessage());
        }
    }
}