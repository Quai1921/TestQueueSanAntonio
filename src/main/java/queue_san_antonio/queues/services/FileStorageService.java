//package queue_san_antonio.queues.services;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//import queue_san_antonio.queues.config.BackblazeConfig;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Base64;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FileStorageService {
//
//    private final BackblazeConfig config;
//
//    @Value("${b2.api-url:https://api.backblazeb2.com}")
//    private String b2ApiUrl;
//
//    private String authToken;
//    private String apiUrl;
//    private String downloadUrl;
//    private String bucketId;
//    private LocalDateTime tokenExpiry;
//
//    public String uploadFile(MultipartFile file, String folder) throws IOException {
//        try {
//            if (needsAuthentication()) {
//                authenticate();
//            }
//
//            UploadUrlInfo uploadInfo = getUploadUrl();
//            String fileName = generateUniqueFileName(file.getOriginalFilename(), folder);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", uploadInfo.getAuthorizationToken());
//            headers.set("X-Bz-File-Name", fileName);
//            headers.set("Content-Type", file.getContentType());
//            headers.set("X-Bz-Content-Sha1", "unverified");
//
//            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                    uploadInfo.getUploadUrl(),
//                    entity,
//                    Map.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                String publicUrl = String.format("%s/file/%s/%s",
//                        downloadUrl, config.getBucketName(), fileName);
//
//                log.info("Archivo subido: {} -> {}", file.getOriginalFilename(), publicUrl);
//                return publicUrl;
//            } else {
//                throw new IOException("Error en upload: " + response.getBody());
//            }
//
//        } catch (Exception e) {
//            log.error("Error subiendo archivo: {}", e.getMessage());
//            throw new IOException("Error al subir archivo: " + e.getMessage());
//        }
//    }
//
//    public void deleteFile(String fileUrl) {
//        // Implementación para eliminar archivo
//        log.info("Eliminando archivo: {}", fileUrl);
//    }
//
//    private boolean needsAuthentication() {
//        return authToken == null || tokenExpiry == null ||
//                LocalDateTime.now().isAfter(tokenExpiry.minusMinutes(5));
//    }
//
//    private void authenticate() throws IOException {
//        try {
//            String auth = Base64.getEncoder()
//                    .encodeToString((config.getApplicationKeyId() + ":" + config.getApplicationKey())
//                            .getBytes());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Basic " + auth);
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    b2ApiUrl + "/b2api/v2/b2_authorize_account",
//                    HttpMethod.GET,
//                    entity,
//                    Map.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                Map<String, Object> body = response.getBody();
//                this.authToken = (String) body.get("authorizationToken");
//                this.apiUrl = (String) body.get("apiUrl");
//                this.downloadUrl = (String) body.get("downloadUrl");
//                this.tokenExpiry = LocalDateTime.now().plusHours(12);
//
//                // Obtener bucket ID
//                getBucketId();
//            } else {
//                throw new IOException("Error de autenticación: " + response.getBody());
//            }
//
//        } catch (Exception e) {
//            throw new IOException("Error autenticando con B2: " + e.getMessage());
//        }
//    }
//
//    private void getBucketId() throws IOException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", authToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String requestBody = "{\"accountId\":\"" + config.getApplicationKeyId().substring(0, 12) + "\"}";
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                apiUrl + "/b2api/v2/b2_list_buckets",
//                entity,
//                Map.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            Map<String, Object> body = response.getBody();
//            java.util.List<Map<String, Object>> buckets = (java.util.List<Map<String, Object>>) body.get("buckets");
//
//            for (Map<String, Object> bucket : buckets) {
//                if (config.getBucketName().equals(bucket.get("bucketName"))) {
//                    this.bucketId = (String) bucket.get("bucketId");
//                    break;
//                }
//            }
//
//            if (bucketId == null) {
//                throw new IOException("Bucket no encontrado: " + config.getBucketName());
//            }
//        } else {
//            throw new IOException("Error obteniendo buckets: " + response.getBody());
//        }
//    }
//
//    private UploadUrlInfo getUploadUrl() throws IOException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", authToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String requestBody = "{\"bucketId\":\"" + bucketId + "\"}";
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                apiUrl + "/b2api/v2/b2_get_upload_url",
//                entity,
//                Map.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            Map<String, Object> body = response.getBody();
//            return new UploadUrlInfo(
//                    (String) body.get("uploadUrl"),
//                    (String) body.get("authorizationToken")
//            );
//        } else {
//            throw new IOException("Error obteniendo upload URL: " + response.getBody());
//        }
//    }
//
//    private String generateUniqueFileName(String originalName, String folder) {
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        String randomId = UUID.randomUUID().toString().substring(0, 8);
//        String extension = getFileExtension(originalName);
//
//        return String.format("%s/%s_%s%s", folder, timestamp, randomId, extension);
//    }
//
//    private String getFileExtension(String fileName) {
//        if (fileName == null) return "";
//        int lastDot = fileName.lastIndexOf('.');
//        return lastDot > 0 ? fileName.substring(lastDot) : "";
//    }
//
//    private static class UploadUrlInfo {
//        private final String uploadUrl;
//        private final String authorizationToken;
//
//        public UploadUrlInfo(String uploadUrl, String authorizationToken) {
//            this.uploadUrl = uploadUrl;
//            this.authorizationToken = authorizationToken;
//        }
//
//        public String getUploadUrl() { return uploadUrl; }
//        public String getAuthorizationToken() { return authorizationToken; }
//    }
//}

package queue_san_antonio.queues.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import queue_san_antonio.queues.config.BackblazeConfig;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final BackblazeConfig config;

    @Value("${b2.api-url:https://api.backblazeb2.com}")
    private String b2ApiUrl;

    private String authToken;
    private String apiUrl;
    private String downloadUrl;
    private String bucketId;
    private String accountId; // Agregar esta variable
    private LocalDateTime tokenExpiry;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            log.info("Iniciando upload de archivo: {} en folder: {}", file.getOriginalFilename(), folder);

            if (needsAuthentication()) {
                log.info("Necesita autenticación, autenticando...");
                authenticate();
            }

            UploadUrlInfo uploadInfo = getUploadUrl();
            String fileName = generateUniqueFileName(file.getOriginalFilename(), folder);

            // Calcular SHA1 del archivo
            String sha1Hash = calculateSHA1(file.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", uploadInfo.getAuthorizationToken());
            headers.set("X-Bz-File-Name", fileName);
            headers.set("Content-Type", file.getContentType());
            headers.set("X-Bz-Content-Sha1", sha1Hash);

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    uploadInfo.getUploadUrl(),
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = String.format("%s/file/%s/%s",
                        downloadUrl, config.getBucketName(), fileName);

                log.info("Archivo subido exitosamente: {} -> {}", file.getOriginalFilename(), publicUrl);
                return publicUrl;
            } else {
                log.error("Error en upload response: {}", response.getBody());
                throw new IOException("Error en upload: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("Error subiendo archivo: {}", e.getMessage(), e);
            throw new IOException("Error al subir archivo: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        // Implementación para eliminar archivo
        log.info("Eliminando archivo: {}", fileUrl);
    }

    private boolean needsAuthentication() {
        boolean needs = authToken == null || tokenExpiry == null ||
                LocalDateTime.now().isAfter(tokenExpiry.minusMinutes(5));
        log.debug("¿Necesita autenticación? {}", needs);
        return needs;
    }

    private void authenticate() throws IOException {
        try {
            log.info("Autenticando con Backblaze B2...");
            log.debug("Using keyId: {}", config.getApplicationKeyId());
            log.debug("Bucket name: {}", config.getBucketName());

            String auth = Base64.getEncoder()
                    .encodeToString((config.getApplicationKeyId() + ":" + config.getApplicationKey())
                            .getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + auth);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(
                    b2ApiUrl + "/b2api/v2/b2_authorize_account",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                this.authToken = (String) body.get("authorizationToken");
                this.apiUrl = (String) body.get("apiUrl");
                this.downloadUrl = (String) body.get("downloadUrl");
                this.accountId = (String) body.get("accountId"); // Obtener accountId de la respuesta
                this.tokenExpiry = LocalDateTime.now().plusHours(12);

                log.info("Autenticación exitosa. AccountId: {}", accountId);

                // Obtener bucket ID
                getBucketId();
            } else {
                log.error("Error de autenticación: {}", response.getBody());
                throw new IOException("Error de autenticación: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("Error autenticando con B2", e);
            throw new IOException("Error autenticando con B2: " + e.getMessage());
        }
    }

    private void getBucketId() throws IOException {
        try {
            log.info("Obteniendo bucket ID para: {}", config.getBucketName());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // CORREGIDO: Usar accountId de la respuesta de autenticación
            String requestBody = "{\"accountId\":\"" + accountId + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl + "/b2api/v2/b2_list_buckets",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> buckets =
                        (java.util.List<Map<String, Object>>) body.get("buckets");

                log.debug("Buckets encontrados: {}", buckets.size());

                for (Map<String, Object> bucket : buckets) {
                    String bucketName = (String) bucket.get("bucketName");
                    log.debug("Evaluando bucket: {}", bucketName);

                    if (config.getBucketName().equals(bucketName)) {
                        this.bucketId = (String) bucket.get("bucketId");
                        log.info("Bucket encontrado. ID: {}", bucketId);
                        return;
                    }
                }

                // Si llegamos aquí, no se encontró el bucket
                log.error("Bucket '{}' no encontrado. Buckets disponibles:", config.getBucketName());
                for (Map<String, Object> bucket : buckets) {
                    log.error("  - {}", bucket.get("bucketName"));
                }
                throw new IOException("Bucket no encontrado: " + config.getBucketName());

            } else {
                log.error("Error obteniendo buckets: {}", response.getBody());
                throw new IOException("Error obteniendo buckets: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error en getBucketId", e);
            throw new IOException("Error obteniendo bucket ID: " + e.getMessage());
        }
    }

    private UploadUrlInfo getUploadUrl() throws IOException {
        try {
            log.debug("Obteniendo upload URL para bucket: {}", bucketId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "{\"bucketId\":\"" + bucketId + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl + "/b2api/v2/b2_get_upload_url",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                return new UploadUrlInfo(
                        (String) body.get("uploadUrl"),
                        (String) body.get("authorizationToken")
                );
            } else {
                log.error("Error obteniendo upload URL: {}", response.getBody());
                throw new IOException("Error obteniendo upload URL: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error en getUploadUrl", e);
            throw new IOException("Error obteniendo upload URL: " + e.getMessage());
        }
    }

    private String generateUniqueFileName(String originalName, String folder) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalName);

        return String.format("%s/%s_%s%s", folder, timestamp, randomId, extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    private String calculateSHA1(byte[] data) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 algorithm not available", e);
        }
    }

    private static class UploadUrlInfo {
        private final String uploadUrl;
        private final String authorizationToken;

        public UploadUrlInfo(String uploadUrl, String authorizationToken) {
            this.uploadUrl = uploadUrl;
            this.authorizationToken = authorizationToken;
        }

        public String getUploadUrl() { return uploadUrl; }
        public String getAuthorizationToken() { return authorizationToken; }
    }
}