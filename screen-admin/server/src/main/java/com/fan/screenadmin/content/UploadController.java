package com.fan.screenadmin.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class UploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/webp",
            "image/svg+xml"
    );

    private static final Set<String> ALLOWED_MODEL_TYPES = Set.of(
            "model/gltf-binary",
            "application/octet-stream"
    );

    private final Path uploadDir;

    public UploadController(@Value("${app.upload-dir}") Path uploadDir) {
        this.uploadDir = uploadDir.toAbsolutePath().normalize();
    }

    @PostMapping("/uploads")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = extensionOf(originalFilename);
        boolean isModel = Set.of(".glb").contains(extension);
        String normalizedType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (isModel) {
            if (!ALLOWED_MODEL_TYPES.contains(normalizedType)) {
                throw new IllegalArgumentException("模型仅支持 GLB 格式");
            }
        } else if (!ALLOWED_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("仅支持 PNG、JPG、GIF、WebP、SVG 图片或 GLB 模型");
        }
        String filename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(filename));
        } catch (IOException exception) {
            throw new UncheckedIOException("文件保存失败", exception);
        }
        return Map.of("url", "/uploads/" + filename);
    }

    private String extensionOf(String filename) {
        if (filename == null || filename.lastIndexOf('.') < 0) {
            throw new IllegalArgumentException("图片文件名不合法");
        }
        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".glb").contains(extension)) {
            throw new IllegalArgumentException("文件格式不支持");
        }
        return extension;
    }
}
