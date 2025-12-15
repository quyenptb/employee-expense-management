package com.example.secretweapon.controller;

import com.example.secretweapon.payload.response.FileResponse;
import com.example.secretweapon.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // Cho phép Frontend gọi
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new FileResponse(null, "File không được để trống"));
        }

        String fileUrl = fileStorageService.uploadFile(file);
        
        return ResponseEntity.ok(new FileResponse(fileUrl, "Upload thành công"));
    }
    
    public record FileResponse(String url, String message) {}
}