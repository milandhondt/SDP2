package exceptions;

public class InvalidReportException extends IllegalArgumentException
{
	private static final long serialVersionUID = 1L;

	public InvalidReportException(String message)
	{
		super(message);
	}
}
