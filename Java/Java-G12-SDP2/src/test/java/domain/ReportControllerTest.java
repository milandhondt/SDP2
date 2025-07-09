package domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import domain.Maintenance;
import domain.Report;
import domain.ReportController;
import domain.Site;
import domain.User;
import dto.AddressDTO;
import dto.MachineDTO;
import dto.MaintenanceDTO;
import dto.ReportDTO;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;
import exceptions.InformationRequiredExceptionReport;
import exceptions.InvalidReportException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import repository.GenericDaoJpa;
import util.MachineStatus;
import util.MaintenanceStatus;
import util.ProductionStatus;
import util.Role;
import util.Status;

class ReportControllerTest
{

	@Mock
	private GenericDaoJpa<Site> siteDao;

	@Mock
	private GenericDaoJpa<User> userDao;

	@Mock
	private GenericDaoJpa<Report> reportDao;

	@Mock
	private EntityManager entityManager;

	@Mock
	private TypedQuery<Report> typedQuery;

	@InjectMocks
	private ReportController reportController;

	private User testTechnician;
	private Site testSite;
	private Maintenance testMaintenance;
	private Report testReport;

	@BeforeEach
	void setUp()
	{
		MockitoAnnotations.openMocks(this);

		testTechnician = new User();
		testTechnician.setRole(Role.TECHNIEKER);

		testSite = new Site();

		testMaintenance = new Maintenance();

		testReport = new Report.Builder().buildSite(testSite).buildTechnician(testTechnician)
				.buildMaintenance(testMaintenance).buildstartDate(LocalDate.now()).buildStartTime(LocalTime.now())
				.buildEndDate(LocalDate.now()).buildEndTime(LocalTime.now()).buildReason("Test reason")
				.buildRemarks("Test remarks").build();

		when(userDao.findAll()).thenReturn(Arrays.asList(testTechnician));
		when(entityManager.createNamedQuery(anyString(), eq(Report.class))).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(Arrays.asList(testReport));
	}

	@Test
	void getTechnicians_ShouldReturnListOfTechnicians()
	{
		List<User> technicians = reportController.getTechnicians();

		assertNotNull(technicians);
	}

	@Test
	void createReport_WithValidData_ShouldReturnReportDTO() throws InvalidReportException
	{

		AddressDTO addressDTO = new AddressDTO(1, "Street", 1, 1234, "City");
		AddressDTO addressDTO2 = new AddressDTO(2, "Street2", 2, 9876, "City2");

		UserDTO verantwoordelijkeDTO = new UserDTO(1, "John", "Doe", "John.Doe@email.com", "123456789",
				LocalDate.now().minusYears(50), addressDTO, Role.VERANTWOORDELIJKE, Status.ACTIEF, "password");

		SiteDTOWithoutMachines siteDTO = new SiteDTOWithoutMachines(1, "siteName", verantwoordelijkeDTO, Status.ACTIEF,
				addressDTO);

		UserDTO technicianDTO = new UserDTO(1, "Kate", "Moss", "Kate.Moss@email.com", "123456789",
				LocalDate.now().minusYears(25), addressDTO2, Role.TECHNIEKER, Status.ACTIEF, "password");

		MachineDTO machineDTO = new MachineDTO(1, siteDTO, technicianDTO, "AB123", MachineStatus.IN_ONDERHOUD,
				ProductionStatus.NOOD_ONDERHOUD, "Row 2", "Catfood", LocalDate.now().minusMonths(2),
				LocalDate.now().plusMonths(2), 5, 200);

		MaintenanceDTO maintenanceDTO = new MaintenanceDTO(1, LocalDate.now(), LocalDateTime.now(),
				LocalDateTime.now().plusHours(5), technicianDTO, "Test reason", "Test remarks",
				MaintenanceStatus.INGEPLAND, machineDTO);

		ReportDTO result = reportController.createReport(siteDTO, maintenanceDTO, technicianDTO, LocalDate.now(),
				LocalTime.now(), LocalDate.now(), LocalTime.now(), "Test reason", "Test remarks");

		assertNotNull(result);
	}

	@Test
	void createReport_WithNullSite_ShouldThrowInvalidReportException()
	{

		AddressDTO addressDTO = new AddressDTO(2, "Street2", 2, 9876, "City2");

		UserDTO verantwoordelijkeDTO = new UserDTO(1, "John", "Doe", "John.Doe@email.com", "123456789",
				LocalDate.now().minusYears(50), addressDTO, Role.VERANTWOORDELIJKE, Status.ACTIEF, "password");

		SiteDTOWithoutMachines siteDTO = new SiteDTOWithoutMachines(1, "siteName", verantwoordelijkeDTO, Status.ACTIEF,
				addressDTO);

		UserDTO technicianDTO = new UserDTO(1, "Kate", "Moss", "Kate.Moss@email.com", "123456789",
				LocalDate.now().minusYears(25), addressDTO, Role.TECHNIEKER, Status.ACTIEF, "password");

		MachineDTO machineDTO = new MachineDTO(1, siteDTO, technicianDTO, "AB123", MachineStatus.IN_ONDERHOUD,
				ProductionStatus.NOOD_ONDERHOUD, "Row 2", "Catfood", LocalDate.now().minusMonths(2),
				LocalDate.now().plusMonths(2), 5, 200);

		assertThrows(InvalidReportException.class, () -> {
			reportController.createReport(null,
					new MaintenanceDTO(1, LocalDate.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(5),
							technicianDTO, "Test reason", "Test remarks", MaintenanceStatus.INGEPLAND, machineDTO),
					technicianDTO, LocalDate.now(), LocalTime.now(), LocalDate.now(), LocalTime.now(), "Test reason",
					"Test remarks");
		});
	}

	@Test
	void createReport_WithNullTechnician_ShouldThrowInvalidReportException()
	{
		AddressDTO addressDTO = new AddressDTO(2, "Street2", 2, 9876, "City2");

		UserDTO verantwoordelijkeDTO = new UserDTO(1, "John", "Doe", "John.Doe@email.com", "123456789",
				LocalDate.now().minusYears(50), addressDTO, Role.VERANTWOORDELIJKE, Status.ACTIEF, "password");

		UserDTO technicianDTO = new UserDTO(1, "Kate", "Moss", "Kate.Moss@email.com", "123456789",
				LocalDate.now().minusYears(25), addressDTO, Role.TECHNIEKER, Status.ACTIEF, "password");

		SiteDTOWithoutMachines siteDTO = new SiteDTOWithoutMachines(1, "siteName", verantwoordelijkeDTO, Status.ACTIEF,
				addressDTO);

		MachineDTO machineDTO = new MachineDTO(1, siteDTO, technicianDTO, "AB123", MachineStatus.IN_ONDERHOUD,
				ProductionStatus.NOOD_ONDERHOUD, "Row 2", "Catfood", LocalDate.now().minusMonths(2),
				LocalDate.now().plusMonths(2), 5, 200);

		MaintenanceDTO maintenanceDTO = new MaintenanceDTO(1, LocalDate.now(), LocalDateTime.now(),
				LocalDateTime.now().plusHours(5), technicianDTO, "Test reason", "Test remarks",
				MaintenanceStatus.INGEPLAND, machineDTO);

		assertThrows(InvalidReportException.class, () -> {
			reportController.createReport(siteDTO, maintenanceDTO, null, LocalDate.now(), LocalTime.now(),
					LocalDate.now(), LocalTime.now(), "Test reason", "Test remarks");
		});

	}

	@Test
	void getReportsByTechnician_WithNullTechnician_ShouldThrowInvalidReportException()
	{
		assertThrows(InvalidReportException.class, () -> {
			reportController.getReportsByTechnician(null);
		});
	}

	@Test
	void getReportsBySite_WithNullSite_ShouldThrowInvalidReportException()
	{
		assertThrows(InvalidReportException.class, () -> {
			reportController.getReportsBySite(null);
		});
	}

	@Test
	void validateReport_WithValidReport_ShouldNotThrowException()
	{
		assertDoesNotThrow(() -> {
			reportController.validateReport(testReport);
		});
	}

	@Test
	void validateReport_WithNullReport_ShouldThrowInvalidReportException()
	{
		assertThrows(InvalidReportException.class, () -> {
			reportController.validateReport(null);
		});
	}

	@Test
	void validateReport_WithNullTechnician_ShouldThrowInformationRequiredExceptionReport()
	{
		assertThrows(InformationRequiredExceptionReport.class, () -> {
			new Report.Builder().buildSite(testSite).buildTechnician(null).build();
		});
	}

	@Test
	void validateReport_WithNullSite_ShouldThrowInformationRequiredExceptionReport()
	{
		assertThrows(InformationRequiredExceptionReport.class, () -> {
			new Report.Builder().buildSite(null).buildTechnician(testTechnician).build();
		});
	}
}