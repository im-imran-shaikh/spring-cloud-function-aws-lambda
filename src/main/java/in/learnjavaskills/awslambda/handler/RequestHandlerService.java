package in.learnjavaskills.awslambda.handler;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.learnjavaskills.awslambda.dto.AmazonS3DTO;
import in.learnjavaskills.awslambda.dto.AmazonSdkCredentialsDTO;
import in.learnjavaskills.awslambda.dto.EnvironmentVariable;
import in.learnjavaskills.awslambda.dto.FileDetailsDTO;
import in.learnjavaskills.awslambda.dto.GatewayRequest;
import in.learnjavaskills.awslambda.dto.GatewayResponse;
import in.learnjavaskills.awslambda.utility.FileOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RequestHandlerService implements Function<GatewayRequest, GatewayResponse>
{
	private final EnvironmentVariable environmentVariable;
	private final FileOperation fileOperation;
	private final AmazonSdkCredentialsDTO amazonSdkCredentialsDto;
	private final AmazonS3DTO amazonS3Dto;
	
	private final String filePath = "/tmp/";
	private String ipAddress = "0.0.0.0";
	
	@Autowired
	public RequestHandlerService(EnvironmentVariable environmentVariable, FileOperation fileOperation, 
			AmazonSdkCredentialsDTO amazonSdkCredentialsDto, AmazonS3DTO amazonS3Dto)
	{
		this.environmentVariable = environmentVariable;
		this.fileOperation = fileOperation;
		this.amazonSdkCredentialsDto = amazonSdkCredentialsDto;
		this.amazonS3Dto = amazonS3Dto;
	}
	
	
	@Override
	public GatewayResponse apply(final GatewayRequest gatewayRequest) 
	{
		return responseMapper(gatewayRequest);
	}

	private GatewayResponse responseMapper(final GatewayRequest gatewayRequest)
	{
		try { ipAddress = InetAddress.getLocalHost().getHostAddress().trim(); }
		catch (UnknownHostException e) { log.error("Exception occur while fetching ip address ", e); }

		try
		{
			log.info("Gateway Request : " + gatewayRequest.toString());
			
			String absoluteFilePath = filePath + gatewayRequest.getFilename();
			log.info("absoluteFilePath : " + absoluteFilePath);
			
			Optional<File> file = fileOperation.generateFile(gatewayRequest.getFileContent(), absoluteFilePath);
			
			FileDetailsDTO fileDetailsDto = FileDetailsDTO.builder()
				.bucketName(amazonS3Dto.getBucketName())
				.key(amazonS3Dto.getKey())
				.file(file)
				.accessKey(amazonSdkCredentialsDto.getAccessKey())
				.secretKey(amazonSdkCredentialsDto.getSecretKey())
				.build();
			
			boolean isFileUploaded = fileOperation.uplaodToS3(fileDetailsDto);
			
			return GatewayResponse.builder()
					.statusCode(200)
					.isBase64Encoded(false)
					.message(isFileUploaded ? "File Successfully uploaded in S3" : "Fail to upload file in S3")
					.header(Collections.singletonMap("ip address", ipAddress))
					.activeProfile(environmentVariable.getActiveProfile())
					.build();
		}
		catch (Exception exception) 
		{
			log.error("Something went wrong in processing request, Inspect manually", exception);
			return GatewayResponse.builder()
					.statusCode(504)
					.isBase64Encoded(false)
					.message(exception.getMessage())
					.header(Collections.singletonMap("ip address", ipAddress))
					.build();
					
		}
	}

}
