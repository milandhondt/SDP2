package exceptions;

public class NoPermissionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NoPermissionException(String message)
	{
		super(message);
	}
}
