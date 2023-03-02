package in.learnjavaskills.awslambda.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "amazon.s3")
@Getter
@Setter
public class AmazonS3DTO 
{
	private String bucketName;
	private String key;
}
