package in.learnjavaskills.awslambda.dto;

import java.io.File;
import java.util.Optional;

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
public class FileDetailsDTO 
{
	private String bucketName;
	private String key; 
	private Optional<File> file;
	private String accessKey; 
	private String secretKey;
	
}
