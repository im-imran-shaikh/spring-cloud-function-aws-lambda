package in.learnjavaskills.awslambda.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayResponse 
{
	private String message;
	private Integer statusCode;
	private Map<String, String> header;
	private boolean isBase64Encoded;
	private String variable1;
	private String variable2;
}
