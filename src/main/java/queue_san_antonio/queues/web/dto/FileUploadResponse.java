package queue_san_antonio.queues.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String originalName;
    private String contentType;
    private Long size;
    private String url;
    private String folder;
    private LocalDateTime uploadTime;
}