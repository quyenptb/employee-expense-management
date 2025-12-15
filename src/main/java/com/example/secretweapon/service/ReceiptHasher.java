package com.example.secretweapon.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
@Slf4j
public class ReceiptHasher {

    private static final HashingAlgorithm HASHER = new PerceptiveHash(32);
    private static final double SIMILARITY_THRESHOLD = 0.2;
    private final String COLLECTION_NAME = "expense-request";
    private final Firestore db;
    private static final int ALGORITHM_ID = HASHER.algorithmId();

    private final ExecutorService hashingExecutor = Executors.newCachedThreadPool();

    public ReceiptHasher(Firestore firestoreInstance) {
        this.db = firestoreInstance;
    }

    /**
     * Tải ảnh và tính toán pHash một cách bất đồng bộ.
     * @return CompletableFuture chứa Hash của ảnh.
     */
    public CompletableFuture<Hash> calculatePHashAsync(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Downloading image from: {}", imageUrl);
                URL url = new URL(imageUrl);
                BufferedImage img = ImageIO.read(url);
                if (img == null) {
                    throw new IOException("Cannot read image (img is null) from URL: " + imageUrl);
                }
                log.info("Image downloaded. Calculating hash...");
                return HASHER.hash(img);
            } catch (IOException e) {
                log.error("Error reading image: ", e);
                throw new RuntimeException(e);
            }
        }, hashingExecutor);
    }

    /**
     * Lấy danh sách các chuỗi hash đã lưu từ Firestore (Bất đồng bộ).
     * @return CompletableFuture chứa danh sách các chuỗi hash (String).
     */
    public CompletableFuture<List<String>> fetchHashesFromFirestoreAsync() {
        // get() của Firebase Admin SDK trả về ApiFuture, có thể chuyển đổi thành CompletableFuture
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Chúng ta vẫn phải block luồng trong supplyAsync này, 
                // nhưng đây là một luồng nền do CompletableFuture quản lý, 
                // không phải luồng xử lý request chính của server.
                List<String> hashes = future.get().getDocuments().stream()
                    .map(document -> document.getString("imageHash"))
                    .filter(hash -> hash != null)
                    .collect(Collectors.toList());
                return hashes;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    // Hàm lưu hash mới vào Firestore (Bất đồng bộ - Fire and Forget)
    public void saveHashToFirestoreAsync(String hashString, String imageUrl) {
        java.util.Map<String, Object> docData = new java.util.HashMap<>();
        docData.put("imageHash", hashString);
        docData.put("imageUrl", imageUrl);
        docData.put("timestamp", com.google.cloud.Timestamp.now());
        
        // add() cũng trả về ApiFuture, ta không cần get() nó nếu chỉ muốn lưu mà không chờ kết quả.
        db.collection(COLLECTION_NAME).add(docData);
        System.out.println("Saved new hash to Firestore (async).");
    }


    // Quy trình xử lý chính (trả về CompletableFuture để người gọi xử lý kết quả)
    public CompletableFuture<String> processReceiptAsync(String imageUrlFromFirebase) {
        
        CompletableFuture<Hash> newHashFuture = calculatePHashAsync(imageUrlFromFirebase);
        CompletableFuture<List<String>> existingHashesFuture = fetchHashesFromFirestoreAsync();

        return newHashFuture.thenCombineAsync(existingHashesFuture, (newImageHash, existingHashStrings) -> {
            try {
                log.info("Combining futures. Existing hashes count: {}", existingHashStrings.size());
                
                // Convert String (Hex) -> Hash Object
                List<Hash> existingHashes = new ArrayList<>();
                for (String s : existingHashStrings) {
                    try {
                        // Logic cũ của bạn đúng nếu s là Hex. 
                        // Nếu s là rác do lần trước lưu sai, dòng này sẽ throw exception.
                        BigInteger hashValue = new BigInteger(s, 16);
                        existingHashes.add(new Hash(hashValue, 32, ALGORITHM_ID));
                    } catch (NumberFormatException nfe) {
                        log.warn("Skipping invalid hash string in DB: {}", s);
                    }
                }

                // So sánh
                for (Hash existingHash : existingHashes) {
                    double distance = newImageHash.normalizedHammingDistance(existingHash);
                    log.info("Comparing with existing hash. Distance: {}", distance); // Log distance để debug
                    
                    if (distance < SIMILARITY_THRESHOLD) {
                        log.warn("Duplicate found! Distance: {}", distance);
                        return "ALERT: Duplicate image found! Distance Score: " + distance;
                    }
                }

                // QUAN TRỌNG: Lưu dưới dạng HEX String để lần sau đọc được bằng BigInteger(s, 16)
                // Trước đó bạn dùng newImageHash.toString() có thể gây lỗi định dạng
                String hexHash = newImageHash.getHashValue().toString(16);
                
                log.info("Image is unique. Saving new hash (Hex): {}", hexHash);
                saveHashToFirestoreAsync(hexHash, imageUrlFromFirebase);
                
                return "Image is unique. New hash saved.";
            } catch (Exception e) {
                log.error("Error inside thenCombineAsync: ", e);
                throw new RuntimeException(e);
            }
        });
    }
}
