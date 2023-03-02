package in.learnjavaskills.awslambda.exception;


public class FileOperationException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;
	
	public FileOperationException(String message)
	{
		super(message);
	}
	
}
