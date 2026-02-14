package com.lobai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * FileStorageService
 *
 * 파일 저장 및 관리 서비스
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 파일 저장
     *
     * @param file 업로드된 파일
     * @param userId 사용자 ID
     * @return 저장된 파일의 상대 경로
     */
    public String saveFile(MultipartFile file, Long userId) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir, "user-" + userId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved: {} (user: {})", filename, userId);

        // 상대 경로 반환
        return "user-" + userId + "/" + filename;
    }

    /**
     * 파일 읽기
     *
     * @param relativePath 상대 경로
     * @return 파일의 절대 경로
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDir, relativePath);
    }

    /**
     * 파일 삭제
     *
     * @param relativePath 상대 경로
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = getFilePath(relativePath);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", relativePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", relativePath, e);
        }
    }

    /**
     * 파일이 이미지인지 확인
     *
     * @param contentType MIME 타입
     * @return 이미지 여부
     */
    public boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 파일이 PDF인지 확인
     *
     * @param contentType MIME 타입
     * @return PDF 여부
     */
    public boolean isPdf(String contentType) {
        return "application/pdf".equals(contentType);
    }
}
