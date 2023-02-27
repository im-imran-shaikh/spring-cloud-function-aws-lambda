package in.learnjavaskills.awslambda.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component
@ConfigurationProperties(prefix = "environment")
@Getter
@Setter
public class EnvironmentVariable 
{
	private String variable1;
	private String variable2;
}
