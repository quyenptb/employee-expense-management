package com.example.secretweapon.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptHasherTest {

    @Mock
    private Firestore firestore;
    @Mock
    private CollectionReference collectionReference;
    @Mock
    private ApiFuture<QuerySnapshot> queryFuture;
    @Mock
    private QuerySnapshot querySnapshot;
    @Mock
    private ApiFuture<DocumentReference> addFuture;

    @InjectMocks
    private ReceiptHasher receiptHasher;

    @BeforeEach
    void setUp() {
        // QUAN TRỌNG: Thay thế ExecutorService đa luồng bằng Executor đồng bộ.
        ExecutorService directExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            Runnable command = invocation.getArgument(0);
            command.run(); // Chạy ngay lập tức
            return null;
        }).when(directExecutor).execute(any(Runnable.class));

        ReflectionTestUtils.setField(receiptHasher, "hashingExecutor", directExecutor);
    }

    @Test
    @DisplayName("processReceiptAsync_UniqueImage_shouldSaveHash")
    void processReceiptAsync_UniqueImage_shouldSaveHash() throws ExecutionException, InterruptedException {
        String imageUrl = "http://example.com/receipt.jpg";
        
        // Mock Static ImageIO
        try (MockedStatic<ImageIO> imageIOMocked = Mockito.mockStatic(ImageIO.class)) {
            
            // FIX: Sử dụng BufferedImage thật thay vì Mock
            // Mock object sẽ trả về null cho các field nội bộ (Raster, SampleModel),
            // gây ra NullPointerException khi thuật toán Hash cố đọc pixel.
            BufferedImage realImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            
            // Mock ImageIO.read(URL) trả về ảnh thật
            imageIOMocked.when(() -> ImageIO.read(any(URL.class))).thenReturn(realImage);

            // Mock Firestore fetch
            when(firestore.collection("expense-request")).thenReturn(collectionReference);
            when(collectionReference.get()).thenReturn(queryFuture);
            when(queryFuture.get()).thenReturn(querySnapshot);
            when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList()); // No existing hashes

            // Mock Firestore add (save)
            when(collectionReference.add(any())).thenReturn(addFuture);

            // Act
            String result = receiptHasher.processReceiptAsync(imageUrl).get();

            // Assert
            assertEquals("Image is unique. New hash saved.", result);
            verify(collectionReference).add(any());
        }
    }
}