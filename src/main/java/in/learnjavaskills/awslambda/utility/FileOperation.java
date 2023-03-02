package in.learnjavaskills.awslambda.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import in.learnjavaskills.awslambda.dto.FileDetailsDTO;
import in.learnjavaskills.awslambda.exception.FileOperationException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public final class FileOperation 
{
	
	private final String ERROR_MESSAGE = "Something went wrong in uploading file into the amazon s3 bucket, Inspect manually";
	private final String FILE_CREATION_ERROR = "Something went wrong in writing content into file";
	
	/**
	 * @param fileDetailsDto - details of the file to upload into the  aws s3 
	 * @return true if file uploaded successfully else false
	 */
	@SuppressWarnings("deprecation")
	public boolean uplaodToS3(final FileDetailsDTO fileDetailsDto)
	{	
		try
		{
			if (Objects.isNull(fileDetailsDto) || !fileDetailsDto.getFile().isPresent())
				throw new FileOperationException("File catn't be empty or null in the file detailsDTO");
			
			File file = fileDetailsDto.getFile().get();
			log.info("Started uploading file : " + file.getAbsolutePath() + 
					" into s3 bucket : " + fileDetailsDto.getBucketName() + " in s3 key : " + fileDetailsDto.getKey());
			
			final BasicAWSCredentials basicAwsCredentials = new BasicAWSCredentials(fileDetailsDto.getAccessKey(), fileDetailsDto.getSecretKey());
			final AWSCredentialsProvider awsCredentialsProvider = new StaticCredentialsProvider(basicAwsCredentials);
			
			final AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
					.withRegion(Regions.US_EAST_1)
					.withCredentials(awsCredentialsProvider)
					.build();
			
			PutObjectRequest putObjectRequest = new PutObjectRequest(fileDetailsDto.getBucketName(),
					fileDetailsDto.getKey() + File.separator + file.getName(), file);
			
			
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.addUserMetadata("source", "aws-lambda-function");
		
			putObjectRequest.setMetadata(objectMetadata);
			
			
			amazonS3.putObject(putObjectRequest);
			
			log.info("File upload completted");
			return true;
		} 
		catch (AmazonServiceException exception) { log.error(ERROR_MESSAGE, exception); }
		catch (SdkClientException exception) { log.error(ERROR_MESSAGE, exception); }
		catch (Exception exception) { log.error(ERROR_MESSAGE, exception); }
		return false;
	}
	
	
	/**
	 * Generating a file with the given data
	 * @param content - writing data into the file
	 * @param filePath - absolute path is required
	 * @return Optional of File if filePath non-empty or non null else optional of empty.
	 * @throws IOException 
	 */
	public Optional<File> generateFile(final String content, final String filePath) throws IOException
	{
		log.info("Generationg new file at : " + filePath + " with file content : " + content);
		if (Objects.isNull(filePath) || filePath.isEmpty())
			return Optional.empty();
	
		File file = new File(filePath);
		if (!file.exists())
			file.createNewFile();
		
		try(FileWriter fileWriter = new FileWriter(file))
		{
			fileWriter.write(content);
		}
		catch (Exception exception) { log.error(FILE_CREATION_ERROR, exception); return Optional.empty(); }
		
		log.info("File generation completed and create file at " + file.getAbsolutePath());
		return Optional.of(file);
	}
}
