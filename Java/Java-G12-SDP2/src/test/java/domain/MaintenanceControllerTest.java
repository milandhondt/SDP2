package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dto.MaintenanceDTO;
import exceptions.InformationRequiredExceptionMachine;
import exceptions.InformationRequiredExceptionSite;
import exceptions.InformationRequiredExceptionUser;
import repository.GenericDaoJpa;
import util.MachineStatus;
import util.MaintenanceStatus;
import util.ProductionStatus;
import util.Role;
import util.Status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaintenanceController Tests")
class MaintenanceControllerTest
{

	@Mock
	private GenericDaoJpa<Maintenance> maintenanceRepo;

	private MaintenanceController maintenanceController;
	private User technician;
	private User siteManager;
	private Site site;
	private Machine machine;
	private LocalDate defaultExecutionDate;
	private LocalDateTime defaultStartDate;
	private LocalDateTime defaultEndDate;

	@BeforeEach
	void setUp() throws InformationRequiredExceptionUser, InformationRequiredExceptionMachine,
			InformationRequiredExceptionSite
	{
		maintenanceController = new MaintenanceController(maintenanceRepo);
		defaultExecutionDate = LocalDate.of(2025, 5, 1);
		defaultStartDate = LocalDateTime.of(2025, 5, 1, 9, 0);
		defaultEndDate = LocalDateTime.of(2025, 5, 1, 11, 0);

		setupTestUsers();
		setupTestSite();
		setupTestMachine();
	}

	private void setupTestUsers() throws InformationRequiredExceptionUser
	{
		technician = new User.Builder().buildFirstName("John").buildLastName("Doe").buildEmail("john.doe@example.com")
				.buildPhoneNumber("123456789").buildPassword("securePassword").buildBirthdate(LocalDate.of(1985, 5, 15))
				.buildRole(Role.TECHNIEKER).buildStatus(Status.ACTIEF).buildAddress("Main Street", 42, 1000, "Brussels")
				.build();
		technician.setId(1);

		siteManager = new User.Builder().buildFirstName("Jane").buildLastName("Smith")
				.buildEmail("jane.smith@example.com").buildPhoneNumber("987654321").buildPassword("securePassword")
				.buildBirthdate(LocalDate.of(1980, 8, 20)).buildRole(Role.VERANTWOORDELIJKE).buildStatus(Status.ACTIEF)
				.buildAddress("Manager Street", 10, 1000, "Brussels").build();
		siteManager.setId(2);
	}

	private void setupTestSite() throws InformationRequiredExceptionSite
	{
		// Test site aanmaken:
		site = new Site.Builder().buildSiteName("Production Site Alpha")
				.buildAddress("Factory Road", 123, 1050, "Brussels").buildVerantwoordelijke(siteManager)
				.buildStatus(Status.ACTIEF).buildMachines(new HashSet<>()).build();
		site.setId(1);
	}

	private void setupTestMachine() throws InformationRequiredExceptionMachine
	{
		// test Machine aanmaken:
		machine = new Machine.Builder().buildSite(site).buildTechnician(technician).buildCode("MACH-001")
				.buildLocation("Production Floor A").buildProductInfo("Product X-100")
				.buildMachineStatus(MachineStatus.DRAAIT).buildProductionStatus(ProductionStatus.GEZOND)
				.buildFutureMaintenance(LocalDate.now().plusMonths(1)).build();
		machine.setId(1);
		site.addMachine(machine);
	}

	private Maintenance createTestMaintenance(int id, LocalDate executionDate, LocalDateTime startDate,
			LocalDateTime endDate, String reason, String comments, MaintenanceStatus status)
	{
		Maintenance maintenance = new Maintenance.Builder().buildExecutionDate(executionDate).buildStartDate(startDate)
				.buildEndDate(endDate).buildTechnician(technician).buildReason(reason).buildComments(comments)
				.buildMaintenanceStatus(status).buildMachine(machine).build();
		maintenance.setId(id);
		return maintenance;
	}

	private void assertMaintenanceDTOEquals(MaintenanceDTO dto, int id, LocalDate executionDate,
			LocalDateTime startDate, LocalDateTime endDate, String reason, String comments, MaintenanceStatus status,
			int technicianId, String technicianFirstName, int machineId)
	{
		assertEquals(id, dto.id());
		assertEquals(executionDate, dto.executionDate());
		assertEquals(startDate, dto.startDate());
		assertEquals(endDate, dto.endDate());
		assertEquals(reason, dto.reason());
		assertEquals(comments, dto.comments());
		assertEquals(status, dto.status());
		assertEquals(technicianId, dto.technician().id());
		assertEquals(technicianFirstName, dto.technician().firstName());
		assertEquals(machineId, dto.machine().id());
	}

	@Test
	@DisplayName("getMaintenances should return all maintenances as DTOs")
	void getMaintenances_ShouldReturnAllMaintenancesAsDTO()
	{
		Maintenance maintenance1 = createTestMaintenance(1, defaultExecutionDate, defaultStartDate, defaultEndDate,
				"Regular check", "All good", MaintenanceStatus.VOLTOOID);

		Maintenance maintenance2 = createTestMaintenance(2, defaultExecutionDate.plusDays(1),
				defaultStartDate.plusDays(1), defaultEndDate.plusDays(1), "Repair", "Fixed issue",
				MaintenanceStatus.INGEPLAND);

		List<Maintenance> maintenances = Arrays.asList(maintenance1, maintenance2);

		when(maintenanceRepo.findAll()).thenReturn(maintenances);
		List<MaintenanceDTO> result = maintenanceController.getMaintenances();

		assertNotNull(result);
		assertEquals(2, result.size());

		assertMaintenanceDTOEquals(result.get(0), 1, defaultExecutionDate, defaultStartDate, defaultEndDate,
				"Regular check", "All good", MaintenanceStatus.VOLTOOID, 1, "John", 1);

		assertMaintenanceDTOEquals(result.get(1), 2, defaultExecutionDate.plusDays(1), defaultStartDate.plusDays(1),
				defaultEndDate.plusDays(1), "Repair", "Fixed issue", MaintenanceStatus.INGEPLAND, 1, "John", 1);

		verify(maintenanceRepo, times(1)).findAll();
	}

	@Test
	@DisplayName("getMaintenances should return empty list when repository returns empty list")
	void getMaintenances_ShouldReturnEmptyListWhenRepoReturnsEmpty()
	{
		when(maintenanceRepo.findAll()).thenReturn(Collections.emptyList());

		List<MaintenanceDTO> result = maintenanceController.getMaintenances();

		assertNotNull(result);
		assertTrue(result.isEmpty());

		verify(maintenanceRepo, times(1)).findAll();
	}

	@Test
	@DisplayName("makeMaintenanceDTOs should convert list of maintenances to DTOs correctly")
	void makeMaintenanceDTOs_ShouldConvertListCorrectly()
	{
		Maintenance maintenance1 = createTestMaintenance(1, defaultExecutionDate, defaultStartDate, defaultEndDate,
				"Regular check", "All good", MaintenanceStatus.VOLTOOID);

		Maintenance maintenance2 = createTestMaintenance(2, defaultExecutionDate.plusDays(1),
				defaultStartDate.plusDays(1), defaultEndDate.plusDays(1), "Repair", "Fixed issue",
				MaintenanceStatus.INGEPLAND);

		List<Maintenance> maintenances = Arrays.asList(maintenance1, maintenance2);

		List<MaintenanceDTO> result = maintenanceController.makeMaintenanceDTOs(maintenances);

		assertNotNull(result);
		assertEquals(2, result.size());

		assertMaintenanceDTOEquals(result.get(0), 1, defaultExecutionDate, defaultStartDate, defaultEndDate,
				"Regular check", "All good", MaintenanceStatus.VOLTOOID, 1, "John", 1);

		assertMaintenanceDTOEquals(result.get(1), 2, defaultExecutionDate.plusDays(1), defaultStartDate.plusDays(1),
				defaultEndDate.plusDays(1), "Repair", "Fixed issue", MaintenanceStatus.INGEPLAND, 1, "John", 1);
	}

	@Test
	@DisplayName("makeMaintenanceDTOs should handle null input by returning empty list")
	void makeMaintenanceDTOs_ShouldHandleNullInput()
	{
		List<MaintenanceDTO> result = maintenanceController.makeMaintenanceDTOs(null);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("makeMaintenanceDTOs should handle empty list by returning empty list")
	void makeMaintenanceDTOs_ShouldHandleEmptyList()
	{
		List<MaintenanceDTO> result = maintenanceController.makeMaintenanceDTOs(Collections.emptyList());

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("getMaintenance should return maintenance by ID")
	void getMaintenance_ShouldReturnMaintenanceById()
	{
		int maintenanceId = 1;
		Maintenance expectedMaintenance = createTestMaintenance(maintenanceId, defaultExecutionDate, defaultStartDate,
				defaultEndDate, "Regular check", "All good", MaintenanceStatus.VOLTOOID);

		when(maintenanceRepo.get(maintenanceId)).thenReturn(expectedMaintenance);

		Maintenance result = maintenanceController.getMaintenance(maintenanceId);

		assertNotNull(result);
		assertEquals(maintenanceId, result.getId());
		assertEquals(defaultExecutionDate, result.getExecutionDate());
		assertEquals(defaultStartDate, result.getStartDate());
		assertEquals(defaultEndDate, result.getEndDate());
		assertEquals("Regular check", result.getReason());
		assertEquals("All good", result.getComments());
		assertEquals(MaintenanceStatus.VOLTOOID, result.getStatus());
		assertEquals(technician, result.getTechnician());
		assertEquals(machine, result.getMachine());

		verify(maintenanceRepo, times(1)).get(maintenanceId);
	}

	@Test
	@DisplayName("getMaintenance should return null when maintenance not found")
	void getMaintenance_ShouldReturnNullWhenNotFound()
	{
		int nonExistentId = 999;
		when(maintenanceRepo.get(nonExistentId)).thenReturn(null);

		Maintenance result = maintenanceController.getMaintenance(nonExistentId);

		assertNull(result);
		verify(maintenanceRepo, times(1)).get(nonExistentId);
	}

	@Test
	@DisplayName("getMaintenanceDTO should return maintenance DTO by ID")
	void getMaintenanceDTO_ShouldReturnMaintenanceDTOById()
	{
		int maintenanceId = 1;
		Maintenance maintenance = createTestMaintenance(maintenanceId, defaultExecutionDate, defaultStartDate,
				defaultEndDate, "Regular check", "All good", MaintenanceStatus.VOLTOOID);

		when(maintenanceRepo.get(maintenanceId)).thenReturn(maintenance);

		MaintenanceDTO result = maintenanceController.getMaintenanceDTO(maintenanceId);

		assertNotNull(result);
		assertMaintenanceDTOEquals(result, maintenanceId, defaultExecutionDate, defaultStartDate, defaultEndDate,
				"Regular check", "All good", MaintenanceStatus.VOLTOOID, 1, "John", 1);

		verify(maintenanceRepo, times(1)).get(maintenanceId);
	}

	@Test
	@DisplayName("getMaintenanceDTO should return null when maintenance not found")
	void getMaintenanceDTO_ShouldReturnNullWhenNotFound()
	{
		int nonExistentId = 999;
		when(maintenanceRepo.get(nonExistentId)).thenReturn(null);

		MaintenanceDTO result = maintenanceController.getMaintenanceDTO(nonExistentId);

		assertNull(result);
		verify(maintenanceRepo, times(1)).get(nonExistentId);
	}
}