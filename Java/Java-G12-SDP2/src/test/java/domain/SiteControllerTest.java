package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domain.Machine;
import domain.NotificationObserver;
import domain.Site;
import domain.SiteController;
import domain.User;
import dto.SiteDTOWithMachines;
import exceptions.InformationRequiredExceptionSite;
import interfaces.Observer;
import repository.GenericDaoJpa;
import repository.UserDao;
import util.MachineStatus;
import util.ProductionStatus;
import util.Status;

@ExtendWith(MockitoExtension.class)
class SiteControllerTest
{

	@Mock
	private GenericDaoJpa<Site> siteRepo;

	@Mock
	private UserDao userRepo;

	@Mock
	private NotificationObserver notificationObserver;

	@InjectMocks
	private SiteController siteController;

	private Site testSite;
	private User testUser;
	private Set<Machine> testMachines;

	@BeforeEach
	void setUp()
	{
		testUser = new User();
		testUser.setId(1);
		testUser.setFirstName("John");
		testUser.setLastName("Doe");

		testMachines = new HashSet<>();

		testSite = new Site.Builder().buildSiteName("Test Site").buildVerantwoordelijke(testUser)
				.buildStatus(Status.ACTIEF).buildAddress("Street", 1, 1234, "City").build();
		testSite.setId(1);

		Machine machine1 = new Machine.Builder().buildCode("abc123").buildTechnician(testUser).buildLocation("location")
				.buildProductInfo("productInfo").buildMachineStatus(MachineStatus.DRAAIT)
				.buildProductionStatus(ProductionStatus.GEZOND).buildSite(testSite)
				.buildFutureMaintenance(LocalDate.now().plusDays(40)).build();

		testMachines.add(machine1);

		testSite.addMachine(machine1);

		siteController.addObserver(notificationObserver);
	}

	@Test
	void getSite_shouldReturnSiteDTO()
	{
		when(siteRepo.get(1)).thenReturn(testSite);

		SiteDTOWithMachines result = siteController.getSite(1);

		assertNotNull(result);
		assertEquals(1, result.id());
		assertEquals("Test Site", result.siteName());
		verify(siteRepo).get(1);
	}

	@Test
	void getSites_shouldReturnListOfSites()
	{
		List<Site> sites = Arrays.asList(testSite);
		when(siteRepo.findAll()).thenReturn(sites);

		List<SiteDTOWithMachines> result = siteController.getSites();

		assertEquals(1, result.size());
		assertEquals("Test Site", result.get(0).siteName());
		verify(siteRepo).findAll();
	}

	@Test
	void getAllStatusses_shouldReturnUniqueStatuses()
	{
		Site site2 = new Site();
		site2.setStatus(Status.INACTIEF);

		List<Site> sites = Arrays.asList(testSite, testSite, site2);
		when(siteRepo.findAll()).thenReturn(sites);

		List<String> result = siteController.getAllStatusses();

		assertEquals(2, result.size());
		assertTrue(result.contains("ACTIEF"));
		assertTrue(result.contains("INACTIEF"));
	}

	@Test
	void createSite_shouldCreateNewSite() throws Exception
	{
		when(userRepo.get(1)).thenReturn(testUser);

		SiteDTOWithMachines result = siteController.createSite("New Site", "Street", "123", "2000", "City", 1);

		assertNotNull(result);
		assertEquals("New Site", result.siteName());
		verify(siteRepo).insert(any(Site.class));
		verify(notificationObserver).update(contains("Site aangemaakt"));
	}

	@Test
	void createSite_withInvalidData_shouldThrowException()
	{
		assertThrows(InformationRequiredExceptionSite.class, () -> {
			siteController.createSite("", "Street", "123", "2000", "City", 1);
		});

		assertThrows(NumberFormatException.class, () -> {
			siteController.createSite("Name", "Street", "abc", "2000", "City", 1);
		});
	}

	@Test
	void updateSite_shouldUpdateExistingSite() throws Exception
	{
		when(siteRepo.get(1)).thenReturn(testSite);
		when(userRepo.get(1)).thenReturn(testUser);

		SiteDTOWithMachines result = siteController.updateSite(1, "Updated Site", "New Street", "456", "3000",
				"New City", 1, Status.INACTIEF);

		assertEquals("Updated Site", result.siteName());
		assertEquals("INACTIEF", result.status().toString());
		verify(siteRepo).update(any(Site.class));
		verify(notificationObserver).update(contains("Site bijgewerkt"));
	}

	@Test
	void updateSite_nonExistingSite_shouldThrowException()
	{
		when(siteRepo.get(999)).thenReturn(null);

		assertThrows(IllegalArgumentException.class, () -> {
			siteController.updateSite(999, "Name", "Street", "123", "2000", "City", 1, Status.ACTIEF);
		});
	}

	@Test
	void getAllVerantwoordelijken_shouldReturnUniqueNames()
	{
		User user2 = new User();
		user2.setFirstName("Jane");
		user2.setLastName("Smith");

		Site site2 = new Site();
		site2.setVerantwoordelijke(user2);

		List<Site> sites = Arrays.asList(testSite, testSite, site2);
		when(siteRepo.findAll()).thenReturn(sites);

		List<String> result = siteController.getAllVerantwoordelijken();

		assertEquals(2, result.size());
		assertTrue(result.contains("John Doe"));
		assertTrue(result.contains("Jane Smith"));
	}

	@Test
	void observerMethods_shouldWorkCorrectly()
	{
		Observer testObserver = mock(Observer.class);
		siteController.addObserver(testObserver);
		siteController.notifyObservers("Test message");
		verify(testObserver).update("Test message");

		siteController.removeObserver(testObserver);
		siteController.notifyObservers("Another message");
		verify(testObserver, times(1)).update(anyString());
	}
}