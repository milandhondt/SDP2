package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import exceptions.InformationRequiredExceptionUser;
import interfaces.Observer;
import util.Role;
import util.Status;

class UserTest
{

	private User user;

	@Mock
	private Observer observer;

	@Mock
	private Address address;

	@BeforeEach
	void setUp()
	{
		MockitoAnnotations.openMocks(this);
		user = new User();
		user.addObserver(observer);

		when(address.getStreet()).thenReturn("Street");
		when(address.getNumber()).thenReturn(1);
		when(address.getPostalcode()).thenReturn(1234);
		when(address.getCity()).thenReturn("City");
	}

	@Test
	void testGetAge()
	{
		LocalDate birthdate = LocalDate.now().minusYears(25);
		user.setBirthdate(birthdate);

		assertEquals(25, user.getAge());
	}

	@Test
	void testGetFullName()
	{
		user.setFirstName("John");
		user.setLastName("Doe");

		assertEquals("John Doe", user.getFullName());
	}

	@Test
	void testObserverPattern()
	{
		String message = "Test message";
		user.notifyObservers(message);

		verify(observer).update(message);
	}

	@Test
	void testBuilderWithAllRequiredFields() throws InformationRequiredExceptionUser
	{
		User builtUser = new User.Builder().buildFirstName("John").buildLastName("Doe")
				.buildEmail("john.doe@example.com").buildPhoneNumber("1234567890")
				.buildBirthdate(LocalDate.of(1990, 1, 1)).buildAddress("Street", 1, 1234, "City")
				.buildRole(Role.VERANTWOORDELIJKE).buildStatus(Status.ACTIEF).build();

		assertNotNull(builtUser);
		assertEquals("John", builtUser.getFirstName());
	}

	@Test
	void testConstructorWithAllParameters()
	{
		User builtUser = new User.Builder().buildFirstName("John").buildLastName("Doe")
				.buildEmail("john.doe@example.com").buildPhoneNumber("1234567890").buildPassword("hashedPassword")
				.buildBirthdate(LocalDate.of(1990, 1, 1)).buildAddress("Street", 1, 1234, "City")
				.buildRole(Role.VERANTWOORDELIJKE).buildStatus(Status.ACTIEF).build();

		assertEquals("John", builtUser.getFirstName());
		assertEquals("Doe", builtUser.getLastName());
		assertEquals("john.doe@example.com", builtUser.getEmail());
		assertEquals("1234567890", builtUser.getPhoneNumber());
		assertEquals("hashedPassword", builtUser.getPassword());
		assertEquals(LocalDate.of(1990, 1, 1), builtUser.getBirthdate());
		assertEquals(Status.ACTIEF, builtUser.getStatus());
		assertEquals(Role.VERANTWOORDELIJKE, builtUser.getRole());
	}
}