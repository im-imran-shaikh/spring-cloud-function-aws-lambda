package in.learnjavaskills.awslambda.handler;

import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;

import in.learnjavaskills.awslambda.dto.GatewayRequest;
import in.learnjavaskills.awslambda.dto.GatewayResponse;

@SuppressWarnings("deprecation")
public class RequestHandler extends SpringBootRequestHandler<GatewayRequest, GatewayResponse>
{

}
