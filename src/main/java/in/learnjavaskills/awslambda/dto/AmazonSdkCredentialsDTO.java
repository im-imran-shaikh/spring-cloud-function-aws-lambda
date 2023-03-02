package in.learnjavaskills.awslambda.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "amazon.credentials")
@Component
@Getter
@Setter
public class AmazonSdkCredentialsDTO 
{
	private String accessKey;
	private String secretKey;
}
