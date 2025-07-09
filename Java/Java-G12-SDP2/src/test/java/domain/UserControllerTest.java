package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import dto.AddressDTO;
import dto.UserDTO;
import exceptions.InformationRequiredExceptionUser;
import exceptions.InvalidInputException;
import interfaces.Observer;
import repository.UserDao;
import util.AuthenticationUtil;
import util.DTOMapper;
import util.Role;
import util.Status;

class UserControllerTest
{

	@Mock
	private UserDao userRepo;

	@Mock
	private AuthenticationUtil authenticationUtil;

	@Mock
	private Observer notificationObserver;

	@InjectMocks
	private UserController userController;

	@BeforeEach
	void setUp()
	{
		MockitoAnnotations.openMocks(this);
		userController.addObserver(notificationObserver);
	}

	@Test
	void authenticate_ValidCredentials_CallsAuthenticationUtil() throws InvalidInputException
	{
		String email = "test@example.com";
		String password = "password123";

		try (MockedStatic<AuthenticationUtil> utilities = mockStatic(AuthenticationUtil.class))
		{
			userController.authenticate(email, password);

			utilities.verify(() -> AuthenticationUtil.authenticate(email, password, userRepo));
		}
	}

	@Test
	void logout_CallsAuthenticationUtil()
	{
		try (MockedStatic<AuthenticationUtil> utilities = mockStatic(AuthenticationUtil.class))
		{
			userController.logout();

			utilities.verify(AuthenticationUtil::logout);
		}
	}

	@Test
	void getAllTechniekers_ReturnsListOfTechniekerDTOs()
	{
		User technieker1 = createTestUser(1, Role.TECHNIEKER);
		User technieker2 = createTestUser(2, Role.TECHNIEKER);
		when(userRepo.getAllTechniekers()).thenReturn(Arrays.asList(technieker1, technieker2));

		List<UserDTO> result = userController.getAllTechniekers();

		assertEquals(2, result.size());
		assertEquals(Role.TECHNIEKER, result.get(0).role());
		assertEquals(Role.TECHNIEKER, result.get(1).role());
	}

	@Test
	void getAllUsers_ReturnsListOfAllUserDTOs()
	{
		User user1 = createTestUser(1, Role.VERANTWOORDELIJKE);
		User user2 = createTestUser(2, Role.TECHNIEKER);
		when(userRepo.findAll()).thenReturn(Arrays.asList(user1, user2));

		List<UserDTO> result = userController.getAllUsers();

		assertEquals(2, result.size());
	}

	@Test
	void convertToUser_WithExistingUser_ReturnsUpdatedUser()
	{
		User existingUser = createTestUser(1, Role.VERANTWOORDELIJKE);
		UserDTO dto = new UserDTO(1, "New", "Name", "new@email.com", "123456789", LocalDate.now(),
				new AddressDTO(5, "Straat", 3, 1234, "Stad"), Role.TECHNIEKER, Status.ACTIEF, "password");
		when(userRepo.getByEmail(dto.email())).thenReturn(existingUser);

		User result = DTOMapper.toUser(dto);

		assertEquals(dto.firstName(), result.getFirstName());
		assertEquals(dto.lastName(), result.getLastName());
		assertEquals(dto.email(), result.getEmail());
	}

	@Test
	void getUserDTOById_ReturnsCorrectUserDTO()
	{
		User user = createTestUser(1, Role.VERANTWOORDELIJKE);
		when(userRepo.get(1)).thenReturn(user);

		UserDTO result = userController.getUserById(1);

		assertEquals(user.getId(), result.id());
		assertEquals(user.getFirstName(), result.firstName());
		assertEquals(user.getLastName(), result.lastName());
	}

	@Test
	void getUserByEmail_ReturnsCorrectUser()
	{
		User expectedUser = createTestUser(1, Role.VERANTWOORDELIJKE);
		when(userRepo.getByEmail("test@example.com")).thenReturn(expectedUser);

		User result = userController.getUserByEmail("test@example.com");

		assertEquals(expectedUser, result);
	}

	@Test
	void getUserDTOByEmail_ReturnsCorrectUserDTO()
	{
		User user = createTestUser(1, Role.VERANTWOORDELIJKE);
		when(userRepo.getByEmail("test@example.com")).thenReturn(user);

		UserDTO result = userController.getUserDTOByEmail("test@example.com");

		assertEquals(user.getId(), result.id());
		assertEquals(user.getEmail(), result.email());
	}

	@Test
	void getUserByName_WithValidName_ReturnsCorrectUserDTO()
	{
		User user = createTestUser(1, Role.VERANTWOORDELIJKE);
		user.setFirstName("John");
		user.setLastName("Doe");
		when(userRepo.findAll()).thenReturn(Arrays.asList(user));

		UserDTO result = userController.getUserByName("John Doe");

		assertNotNull(result);
		assertEquals("John", result.firstName());
		assertEquals("Doe", result.lastName());
	}

	@Test
	void getUserByName_WithInvalidName_ReturnsNull()
	{
		assertNull(userController.getUserByName("Invalid Name Format"));
	}

	@Test
	void getAllVerantwoordelijken_ReturnsOnlyVerantwoordelijkeUsers()
	{
		User verantwoordelijke = createTestUser(1, Role.VERANTWOORDELIJKE);
		User technieker = createTestUser(2, Role.TECHNIEKER);
		when(userRepo.findAll()).thenReturn(Arrays.asList(verantwoordelijke, technieker));

		List<UserDTO> result = userController.getAllVerantwoordelijken();

		assertEquals(1, result.size());
		assertEquals(Role.VERANTWOORDELIJKE, result.get(0).role());
	}

	@Test
	void createUser_ValidInput_CreatesAndReturnsUserDTO() throws InformationRequiredExceptionUser
	{

		User newUser = new User.Builder().buildFirstName("John").buildLastName("Doe").buildEmail("john@example.com")
				.buildPhoneNumber("123456789").buildBirthdate(LocalDate.now()).buildAddress("Street", 1, 1234, "City")
				.buildRole(Role.VERANTWOORDELIJKE).buildStatus(Status.ACTIEF).build();

		UserDTO result = userController.createUser("John", "Doe", "john@example.com", "123456789", LocalDate.now(),
				"Street", "1", "1234", "City", Role.VERANTWOORDELIJKE);

		assertNotNull(result);
		assertEquals("John", result.firstName());
		assertEquals("Doe", result.lastName());
		verify(userRepo).startTransaction();
		verify(userRepo).commitTransaction();
		verify(notificationObserver).update(anyString());
	}

	@Test
	void updateUser_ValidInput_UpdatesAndReturnsUserDTO() throws InformationRequiredExceptionUser
	{
		User existingUser = createTestUser(1, Role.VERANTWOORDELIJKE);
		when(userRepo.get(1)).thenReturn(existingUser);

		User updatedUser = createTestUser(1, Role.TECHNIEKER);
		when(userRepo.update(any(User.class))).thenReturn(updatedUser);

		UserDTO result = userController.updateUser(1, "Updated", "Name", "updated@example.com", "987654321",
				LocalDate.now(), "New Street", "456", "2000", "New City", Role.TECHNIEKER, Status.INACTIEF);

		assertNotNull(result);
		assertEquals("Updated", result.firstName());
		assertEquals("Name", result.lastName());
		verify(userRepo).startTransaction();
		verify(userRepo).commitTransaction();
		verify(notificationObserver).update(anyString());
	}

	@Test
	void updateUser_NonExistentUser_ThrowsIllegalArgumentException()
	{
		when(userRepo.get(999)).thenReturn(null);

		assertThrows(IllegalArgumentException.class,
				() -> userController.updateUser(999, "Updated", "Name", "updated@example.com", "987654321",
						LocalDate.now(), "New Street", "456", "2000", "New City", Role.TECHNIEKER, Status.INACTIEF));
	}

	@Test
	void getAllStatusses_ReturnsDistinctStatusValues()
	{
		User activeUser = createTestUser(1, Role.VERANTWOORDELIJKE);
		activeUser.setStatus(Status.ACTIEF);
		User inactiveUser = createTestUser(2, Role.TECHNIEKER);
		inactiveUser.setStatus(Status.INACTIEF);
		when(userRepo.findAll()).thenReturn(Arrays.asList(activeUser, inactiveUser, activeUser));

		List<String> result = userController.getAllStatusses();

		assertEquals(2, result.size());
		assertTrue(result.contains("ACTIEF"));
		assertTrue(result.contains("INACTIEF"));
	}

	@Test
	void getAllRoles_ReturnsDistinctRoleValues()
	{
		User verantwoordelijke = createTestUser(1, Role.VERANTWOORDELIJKE);
		User technieker = createTestUser(2, Role.TECHNIEKER);
		when(userRepo.findAll()).thenReturn(Arrays.asList(verantwoordelijke, technieker, verantwoordelijke));

		List<String> result = userController.getAllRoles();

		assertEquals(2, result.size());
		assertTrue(result.contains("VERANTWOORDELIJKE"));
		assertTrue(result.contains("TECHNIEKER"));
	}

	@Test
	void getFilteredUsers_WithSearchFilter_ReturnsMatchingUsers()
	{
		User user1 = createTestUser(1, Role.VERANTWOORDELIJKE);
		user1.setFirstName("John");
		user1.setLastName("Doe");
		User user2 = createTestUser(2, Role.TECHNIEKER);
		user2.setFirstName("Jane");
		user2.setLastName("Smith");
		when(userRepo.findAll()).thenReturn(Arrays.asList(user1, user2));

		List<UserDTO> result = userController.getFilteredUsers("John", null, null);

		assertEquals(1, result.size());
		assertEquals("John", result.get(0).firstName());
	}

	@Test
	void getFilteredUsers_WithStatusFilter_ReturnsMatchingUsers()
	{
		User activeUser = createTestUser(1, Role.VERANTWOORDELIJKE);
		activeUser.setStatus(Status.ACTIEF);
		User inactiveUser = createTestUser(2, Role.TECHNIEKER);
		inactiveUser.setStatus(Status.INACTIEF);
		when(userRepo.findAll()).thenReturn(Arrays.asList(activeUser, inactiveUser));

		List<UserDTO> result = userController.getFilteredUsers("", "ACTIEF", null);

		assertEquals(1, result.size());
		assertEquals(Status.ACTIEF, result.get(0).status());
	}

	@Test
	void getFilteredUsers_WithRoleFilter_ReturnsMatchingUsers()
	{
		User verantwoordelijke = createTestUser(1, Role.VERANTWOORDELIJKE);
		User technieker = createTestUser(2, Role.TECHNIEKER);
		when(userRepo.findAll()).thenReturn(Arrays.asList(verantwoordelijke, technieker));

		List<UserDTO> result = userController.getFilteredUsers("", null, "TECHNIEKER");

		assertEquals(1, result.size());
		assertEquals(Role.TECHNIEKER, result.get(0).role());
	}

	@Test
	void addObserver_AddsObserverToList()
	{
		Observer newObserver = mock(Observer.class);
		userController.addObserver(newObserver);

		userController.notifyObservers("Test message");

		verify(newObserver).update("Test message");
	}

	@Test
	void removeObserver_RemovesObserverFromList()
	{
		Observer observerToRemove = mock(Observer.class);
		userController.addObserver(observerToRemove);
		userController.removeObserver(observerToRemove);

		userController.notifyObservers("Test message");

		verify(observerToRemove, never()).update(anyString());
	}

	private User createTestUser(int id, Role role)
	{
		User user = new User();
		user.setId(id);
		user.setFirstName("Test");
		user.setLastName("User");
		user.setEmail("test@example.com");
		user.setPhoneNumber("123456789");
		user.setBirthdate(LocalDate.now());
		user.setAddress(new Address());
		user.setRole(role);
		user.setStatus(Status.ACTIEF);
		return user;
	}
}