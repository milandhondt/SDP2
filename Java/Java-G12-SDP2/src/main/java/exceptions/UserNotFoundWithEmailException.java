package exceptions;

public class UserNotFoundWithEmailException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UserNotFoundWithEmailException(String email)
	{
		super("User with email " + email + " not found");
	}

}