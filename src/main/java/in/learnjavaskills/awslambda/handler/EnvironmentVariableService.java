package in.learnjavaskills.awslambda.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import in.learnjavaskills.awslambda.dto.EnvironmentVariable;
import in.learnjavaskills.awslambda.dto.GatewayResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class EnvironmentVariableService {
	private final EnvironmentVariable environmentVariable;

	@Bean
	public Function<String, GatewayResponse> findSelectedEnvironmentVariable() 
	{
		return this ::responseMapper;
	}

	private GatewayResponse responseMapper(String responseMessage) {

		String ipAddress = "0.0.0.0";
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress().trim();
		} catch (UnknownHostException e) {
			log.error("Exception occur while fetching ip address ", e);
		}

		return GatewayResponse.builder().statusCode(200).isBase64Encoded(false).message(responseMessage)
				.header(Collections.singletonMap("ip address", ipAddress)).variable1(environmentVariable.getVariable1())
				.variable2(environmentVariable.getVariable2()).build();
	}
}
