package queue_san_antonio.queues.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "b2")
@Data
public class BackblazeConfig {
    private String applicationKeyId;
    private String applicationKey;
    private String bucketName;
    private String baseUrl;
}