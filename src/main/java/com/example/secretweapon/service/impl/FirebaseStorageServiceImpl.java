package com.example.secretweapon.service.impl;

import com.example.secretweapon.service.FileStorageService;
import com.google.cloud.storage.Acl; // Import mới để set quyền Public
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageServiceImpl implements FileStorageService {

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());

            Bucket bucket = StorageClient.getInstance().bucket();

            // 1. Upload file lên bucket
            Blob blob = bucket.create(fileName, file.getInputStream(), file.getContentType());

            // 2. Set quyền truy cập Public (Ai có link đều xem được)
            // Giải quyết triệt để vấn đề link bị hết hạn sau 7 ngày.
            // Bảo mật dựa trên tên file ngẫu nhiên (UUID) khó đoán.
            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            // 3. Trả về Public URL vĩnh viễn
            // Format: https://storage.googleapis.com/<bucket_name>/<file_name>
            return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), fileName);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload file lên Firebase: " + e.getMessage());
        }
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
}