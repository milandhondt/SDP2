package util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher
{
	// Configure Argon2 parameters (adjust based on security needs)
	private static final int ITERATIONS = 6; // Number of passes
	private static final int MEMORY = (int) Math.pow(2, 17);
	private static final int PARALLELISM = 4; // Threads
	private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

	public static String hash(String password)
	{
		try
		{
			return ARGON2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
		} finally
		{
			ARGON2.wipeArray(password.toCharArray());
		}
	}

	public static boolean verify(String rawPassword, String hashedPassword)
	{
		try
		{
			return ARGON2.verify(hashedPassword, rawPassword.toCharArray());
		} finally
		{
			ARGON2.wipeArray(rawPassword.toCharArray());
		}
	}

}