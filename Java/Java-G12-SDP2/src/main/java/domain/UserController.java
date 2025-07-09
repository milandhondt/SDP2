package domain;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import dto.UserDTO;
import exceptions.InvalidInputException;
import interfaces.Observer;
import interfaces.Subject;
import lombok.Getter;
import lombok.Setter;
import repository.UserDao;
import repository.UserDaoJpa;
import util.AuthenticationUtil;
import util.DTOMapper;
import util.I18n;
import util.PasswordHasher;
import util.Role;
import util.Status;

/**
 * Controller class for managing user-related operations in the system. This
 * class serves as the main interface between the presentation layer and the
 * data access layer for all user management functionality.
 */
public class UserController implements Subject
{
	@Setter
	@Getter
	private UserDao userRepo;

	private List<Observer> observers = new ArrayList<>();

	/**
	 * Constructs a new UserController with default dependencies. Initializes the
	 * UserDaoJpa implementation and adds a default NotificationObserver.
	 */
	public UserController()
	{
		userRepo = new UserDaoJpa();
		addObserver(new NotificationObserver());

	}

	/**
	 * Authenticates a user with the provided credentials.
	 * 
	 * @param email    The user's email address
	 * @param password The user's password
	 * @throws InvalidInputException If authentication fails due to invalid
	 *                               credentials
	 */
	public void authenticate(String email, String password) throws InvalidInputException
	{
		AuthenticationUtil.authenticate(email, password, userRepo);
	}

	/**
	 * Logs out the currently authenticated user.
	 */

	public void logout()
	{
		AuthenticationUtil.logout();
	}

	/**
	 * Retrieves all users with the 'Technieker' role.
	 * 
	 * @return List of UserDTO objects representing all techniekers
	 */
	public List<UserDTO> getAllTechniekers()
	{
		List<User> techniekers = userRepo.getAllTechniekers();
		return techniekers.stream().map(technieker -> DTOMapper.toUserDTO(technieker)).toList();
	}

	/**
	 * Retrieves all users in the system.
	 * 
	 * @return List of UserDTO objects representing all users
	 */
	public List<UserDTO> getAllUsers()
	{
		List<User> users = userRepo.findAll();
		return users.stream().map(user -> DTOMapper.toUserDTO(user)).toList();
	}

	/**
	 * Retrieves a user DTO by their ID.
	 * 
	 * @param id The ID of the user to retrieve
	 * @return The UserDTO object with the specified ID
	 */
	public UserDTO getUserById(int id)
	{
		return DTOMapper.toUserDTO(userRepo.get(id));
	}

	/**
	 * Retrieves a user by their email address.
	 * 
	 * @param email The email address of the user to retrieve
	 * @return The User object with the specified email
	 */
	// TODO HIER MAG NIEG USER STAAAAAN!!!!!!!!!!!!!!
	public User getUserByEmail(String email)
	{
		return userRepo.getByEmail(email);
	}

	/**
	 * Retrieves a user DTO by their name.
	 * 
	 * @param name The name of the user to retrieve
	 * @return The UserDTO object with the specified name
	 */
	public UserDTO getUserByName(String name)
	{
		if (name == null || !name.contains(" "))
		{
			return null; // or throw an exception TODO!!!!!!!!!!!!!!!!!
		}

		String[] parts = name.trim().split(" ", 2);
		String firstname = parts[0];
		String lastname = parts[1]; // everything after the first space

		return getAllVerantwoordelijken().stream()
				.filter(v -> v.firstName().equalsIgnoreCase(firstname) && v.lastName().equalsIgnoreCase(lastname))
				.findFirst().orElse(null);
	}

	/**
	 * Retrieves a user DTO by their email address.
	 * 
	 * @param email The email address of the user to retrieve
	 * @return The UserDTO object with the specified email
	 */
	public UserDTO getUserDTOByEmail(String email)
	{
		User user = getUserByEmail(email);
		return DTOMapper.toUserDTO(user);
	}

	/**
	 * Retrieves all users with the 'Verantwoordelijke' role.
	 * 
	 * @return List of UserDTO objects representing all verantwoordelijken
	 */
	public List<UserDTO> getAllVerantwoordelijken()
	{
		return getAllUsers().stream().filter(user -> user.role().equals(Role.VERANTWOORDELIJKE)).toList();
	}

	/**
	 * Creates a new user with the provided information.
	 * 
	 * @param firstName   The user's first name
	 * @param lastName    The user's last name
	 * @param email       The user's email address
	 * @param phoneNumber The user's phone number
	 * @param birthdate   The user's birthdate
	 * @param street      The street name of the user's address
	 * @param houseNumber The house number of the user's address
	 * @param postalCode  The postal code of the user's address
	 * @param city        The city of the user's address
	 * @param role        The user's role
	 * @return The created user as a UserDTO
	 * @throws InformationRequiredException If required information is missing
	 * @throws NumberFormatException        If houseNumber or postalCode are not
	 *                                      valid numbers
	 */
	public UserDTO createUser(String firstName, String lastName, String email, String phoneNumber, LocalDate birthdate,
			String street, String houseNumber, String postalCode, String city, Role role)
			throws IllegalArgumentException, NumberFormatException
	{
		User existingUserWithEmail = userRepo.findAll().stream().filter((u) -> u.getEmail().equals(email)).findFirst()
				.orElse(null);
		if (existingUserWithEmail != null)
		{
			throw new IllegalArgumentException(String.format("User with email %s already exists", email));
		}

		int houseNumberInt = Integer.parseInt(houseNumber);
		int postalCodeInt = Integer.parseInt(postalCode);

		User newUser = new User.Builder().buildFirstName(firstName).buildLastName(lastName).buildEmail(email)
				.buildPhoneNumber(phoneNumber).buildBirthdate(birthdate)
				.buildAddress(street, postalCodeInt, houseNumberInt, city).buildRole(role).buildStatus(Status.ACTIEF)
				.build();

		String password = generatePassword();
		newUser.setPassword(PasswordHasher.hash(password));

		// Logs the password, but this could be changed to sending an email or something
		// else.
		// Momentarily it's done like this so the admin could give the password to the
		// created user by copy and pasting.
		System.out.println("Added new user with password: " + password);

		userRepo.startTransaction();
		userRepo.insert(newUser);
		userRepo.commitTransaction();

		notifyObservers("Gebruiker bijgewerkt: " + newUser.getId() + " " + newUser.getFullName());

		return DTOMapper.toUserDTO(newUser);
	}

	/**
	 * Updates an existing user with the provided information.
	 * 
	 * @param userId      The ID of the user to update
	 * @param firstName   The updated first name
	 * @param lastName    The updated last name
	 * @param email       The updated email address
	 * @param phoneNumber The updated phone number
	 * @param birthdate   The updated birthdate
	 * @param street      The updated street name
	 * @param houseNumber The updated house number
	 * @param postalCode  The updated postal code
	 * @param city        The updated city
	 * @param role        The updated role
	 * @param status      The updated status
	 * @return The updated user as a UserDTO
	 * @throws InformationRequiredException If required information is missing
	 * @throws NumberFormatException        If houseNumber or postalCode are not
	 *                                      valid numbers
	 * @throws IllegalArgumentException     If no user exists with the specified ID
	 */
	public UserDTO updateUser(int userId, String firstName, String lastName, String email, String phoneNumber,
			LocalDate birthdate, String street, String houseNumber, String postalCode, String city, Role role,
			Status status) throws IllegalArgumentException, NumberFormatException
	{
		User existingUser = userRepo.get(userId);
		if (existingUser == null)
		{
			throw new IllegalArgumentException("User with ID " + userId + " not found");
		}

		User existingUserWithEmail = userRepo.findAll().stream()
				.filter((u) -> u.getEmail().equals(email) && u.getId() != userId).findFirst().orElse(null);
		if (existingUserWithEmail != null)
		{
			throw new IllegalArgumentException(String.format("User with email %s already exists", email));
		}

		int houseNumberInt = Integer.parseInt(houseNumber);
		int postalCodeInt = Integer.parseInt(postalCode);

		User updatedUser = new User.Builder().buildFirstName(firstName).buildLastName(lastName).buildEmail(email)
				.buildPhoneNumber(phoneNumber).buildBirthdate(birthdate)
				.buildAddress(street, postalCodeInt, houseNumberInt, city).buildRole(role).buildStatus(status).build();

		updatedUser.setId(existingUser.getId());
		updatedUser.setPassword(existingUser.getPassword());

		if (existingUser.getAddress() != null && updatedUser.getAddress() != null)
		{
			updatedUser.getAddress().setId(existingUser.getAddress().getId());
		}

		userRepo.startTransaction();
		try
		{
			userRepo.update(updatedUser);
			userRepo.commitTransaction();
		} catch (Exception e)
		{
			userRepo.rollbackTransaction();
			throw new RuntimeException("Error updating user: " + e.getMessage(), e);
		}

		notifyObservers("Gebruiker bijgewerkt: " + updatedUser.getId() + " " + updatedUser.getFullName());

		return DTOMapper.toUserDTO(updatedUser);
	}

	/**
	 * Generates a secure random password with a mix of character types. The
	 * password will contain at least one lowercase letter, one uppercase letter,
	 * one digit, and one special character. The total length of the password will
	 * be between 10 and 20 characters (inclusive).
	 * 
	 * The password is generated using cryptographically secure random numbers and
	 * includes characters from the following categories: - Lowercase letters (a-z)
	 * - Uppercase letters (A-Z) - Digits (0-9) - Special characters
	 * (!@#$%^&*()-_=+[]{}|;:'",.<>/?)
	 * 
	 * @return A randomly generated secure password as a String
	 * @see SecureRandom
	 */
	private static String generatePassword()
	{
		SecureRandom random = new SecureRandom();
		String lower = "abcdefghijklmnopqrstuvwxyz";
		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String digits = "0123456789";
		String special = "!@#$%^&*()-_=+[]{}|;:'\",.<>/?";
		String allChars = lower + upper + digits + special;

		int length = 10 + random.nextInt(11);

		ArrayList<Character> password = new ArrayList<>();

		password.add(lower.charAt(random.nextInt(lower.length())));
		password.add(upper.charAt(random.nextInt(upper.length())));
		password.add(digits.charAt(random.nextInt(digits.length())));
		password.add(special.charAt(random.nextInt(special.length())));

		for (int i = 4; i < length; i++)
		{
			password.add(allChars.charAt(random.nextInt(allChars.length())));
		}

		Collections.shuffle(password, random);

		StringBuilder sb = new StringBuilder();
		for (char c : password)
		{
			sb.append(c);
		}

		return sb.toString();
	}

	@Override
	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer)
	{
		observers.remove(observer);

	}

	@Override
	public void notifyObservers(String message)
	{
		for (Observer o : observers)
			o.update(message);

	}

	/**
	 * Retrieves all distinct status values from users in the system.
	 * 
	 * @return List of all distinct status values as strings
	 */
	public List<String> getAllStatusses()
	{
		List<UserDTO> allUsers = getAllUsers();
		return allUsers.stream().map(u -> u.status().toString()).distinct().sorted().collect(Collectors.toList());
	}

	/**
	 * Retrieves all distinct role values from users in the system.
	 * 
	 * @return List of all distinct role values as strings
	 */
	public List<String> getAllRoles()
	{
		List<UserDTO> allUsers = getAllUsers();
		return allUsers.stream().map(u -> u.role().toString()).distinct().sorted().collect(Collectors.toList());
	}

	/**
	 * Retrieves users filtered by search criteria, status, and role.
	 * 
	 * @param searchFilter   The string to filter user names (case insensitive)
	 * @param selectedStatus The status to filter by (null for no status filter)
	 * @param selectedRole   The role to filter by (null for no role filter)
	 * @return List of UserDTO objects matching the filter criteria
	 */
	public List<UserDTO> getFilteredUsers(String searchFilter, String selectedStatus, String selectedRole)
	{
		String lowerCaseSearchFilter = searchFilter == null ? "" : searchFilter.toLowerCase();
		return getAllUsers().stream()
				.filter(user -> selectedStatus == null
						|| I18n.convertStatus(user.status().toString()).equals(I18n.convertStatus(selectedStatus)))
				.filter(user -> selectedRole == null
						|| I18n.convertRole(user.role().toString()).equals(I18n.convertRole(selectedRole)))
				.filter(user -> user.firstName().toLowerCase().contains(lowerCaseSearchFilter)
						|| user.lastName().toLowerCase().contains(lowerCaseSearchFilter))
				.collect(Collectors.toList());
	}

}