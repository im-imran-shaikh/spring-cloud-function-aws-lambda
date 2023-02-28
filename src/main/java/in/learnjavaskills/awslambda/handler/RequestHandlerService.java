package in.learnjavaskills.awslambda.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.amazonaws.services.lambda.runtime.Context;

import in.learnjavaskills.awslambda.dto.EnvironmentVariable;
import in.learnjavaskills.awslambda.dto.GatewayResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class RequestHandlerService implements Function<Message<Map<String, String>>, GatewayResponse>
{
	private final EnvironmentVariable environmentVariable;
	private String ipAddress = "0.0.0.0";
	
	@Override
	public GatewayResponse apply(Message<Map<String, String>> input) 
	{
		return responseMapper(input);
	}

	private GatewayResponse responseMapper(final Message<Map<String, String>> input)
	{
		try { ipAddress = InetAddress.getLocalHost().getHostAddress().trim(); }
		catch (UnknownHostException e) { log.error("Exception occur while fetching ip address ", e); }
		
		Map<String, String> payload = input.getPayload(); 
		
		log.info("Input request are printing");
		payload.forEach((key, value) ->  log.info("KEY: " + key + " VALUE: " + value));
		
		Context context = input.getHeaders().get("aws-context", Context.class); // fetching context from the request header
		
		if (Objects.nonNull(context))
		{
			log.info("context printing");
			String awsRequestId = context.getAwsRequestId();
			String functionName = context.getFunctionName();
			log.info("aws-request-id: " + awsRequestId + " and function-name: " + functionName);
			
			log.info("Function version : " + context.getFunctionVersion() + " memory limit: " + context.getMemoryLimitInMB());
			
			log.info("Remaining time in miili second: " +  context.getRemainingTimeInMillis());
		}

		return GatewayResponse.builder()
				.statusCode(200)
				.isBase64Encoded(false)
				.message("SUCCESS")
				.header(Collections.singletonMap("ip address", ipAddress))
				.variable1(environmentVariable.getVariable1())
				.variable2(environmentVariable.getVariable2())
				.payload(payload)
				.build();
	}

}
