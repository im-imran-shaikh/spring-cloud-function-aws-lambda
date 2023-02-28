package in.learnjavaskills.awslambda.handler;

import java.util.Map;

import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;
import org.springframework.messaging.Message;

import com.amazonaws.services.lambda.runtime.Context;

import in.learnjavaskills.awslambda.dto.GatewayResponse;

@SuppressWarnings("deprecation")
public class RequestHandler extends SpringBootRequestHandler<Map<String, String>, GatewayResponse>
{

}
