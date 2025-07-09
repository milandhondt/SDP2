package util;

import domain.User;
import exceptions.InvalidInputException;
import exceptions.UserNotFoundWithEmailException;
import repository.UserDao;

public class AuthenticationUtil {

	private static User authenticatedUser = null;

	public static void authenticate(String email, String inputPassword, UserDao userRepo) throws InvalidInputException {
	    User user;
	    try {
	        user = userRepo.getByEmail(email);
	    } catch (UserNotFoundWithEmailException e) {
	        authenticatedUser = null;
	        throw new InvalidInputException("E-mailadres en wachtwoord komen niet overeen. Probeer het opnieuw.");
	    }
	    
	    if (!PasswordHasher.verify(inputPassword, user.getPassword())) {
	        authenticatedUser = null;
	        throw new InvalidInputException("E-mailadres en wachtwoord komen niet overeen. Probeer het opnieuw.");
	    }
	    
	    authenticatedUser = user;
	}

	public static void logout() {
		authenticatedUser = null;
	}

	public static boolean isAuth() {
		return authenticatedUser != null;
	}

	public static User getAuthenticatedUser() {
		return authenticatedUser;
	}

	public static boolean hasRole(Role role) {
		return authenticatedUser.getRole() == role;
	}
}
