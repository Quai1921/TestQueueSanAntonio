package queue_san_antonio.queues.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import queue_san_antonio.queues.services.FileStorageService;
import queue_san_antonio.queues.web.dto.FileUploadResponse;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        try {
            // Validaciones
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ApiResponseWrapper.error("EMPTY_FILE", "El archivo está vacío")
                );
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (!isValidFileType(contentType)) {
                return ResponseEntity.badRequest().body(
                        ApiResponseWrapper.error("INVALID_FILE_TYPE",
                                "Tipo de archivo no permitido: " + contentType)
                );
            }

            // Validar tamaño (50MB máximo)
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                        ApiResponseWrapper.error("FILE_TOO_LARGE", "El archivo es demasiado grande (máximo 50MB)")
                );
            }

            // Subir archivo
            String fileUrl = fileStorageService.uploadFile(file, folder);

            FileUploadResponse response = FileUploadResponse.builder()
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .url(fileUrl)
                    .folder(folder)
                    .uploadTime(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Archivo subido exitosamente")
            );

        } catch (Exception e) {
            log.error("Error en upload: ", e);
            return ResponseEntity.status(500).body(
                    ApiResponseWrapper.error("UPLOAD_FAILED", "Error al subir archivo: " + e.getMessage())
            );
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            fileStorageService.deleteFile(fileUrl);
            return ResponseEntity.ok(
                    ApiResponseWrapper.success(null, "Archivo eliminado exitosamente")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponseWrapper.error("DELETE_FAILED", "Error al eliminar archivo")
            );
        }
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;

        List<String> allowedTypes = Arrays.asList(
                "image/jpeg", "image/png", "image/gif", "image/webp",
                "video/mp4", "video/avi", "video/mov", "video/webm",
                "audio/mp3", "audio/wav", "audio/mpeg", "audio/ogg"
        );

        return allowedTypes.contains(contentType.toLowerCase());
    }
}