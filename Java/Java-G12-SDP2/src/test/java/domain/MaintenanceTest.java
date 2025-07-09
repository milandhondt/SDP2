package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dto.MaintenanceDTO;
import exceptions.InformationRequiredExceptionMaintenance;
import gui.AppServices;
import repository.GenericDaoJpa;
import util.MaintenanceStatus;

@ExtendWith(MockitoExtension.class)
public class MaintenanceTest
{
	@Mock
	private GenericDaoJpa<Maintenance> maintenanceRepo;

	@Mock
	private MaintenanceController maintenanceController;

	@Mock
	private UserController userController;

	@Mock
	private MachineController machineController;

	@Mock
	private Machine machine;

	@Mock
	private User technician;

	@Mock
	private AppServices appServices;

	@Mock
	private Site site;

	private Maintenance maintenance;
	private List<MaintenanceDTO> maintenances;

	@BeforeEach
	void setUp() throws InformationRequiredExceptionMaintenance
	{

		LocalDateTime now = LocalDateTime.now();

		maintenance = new Maintenance.Builder().buildExecutionDate(LocalDate.now()).buildStartDate(now)
				.buildEndDate(now.plusDays(1).minusHours(2)).buildTechnician(technician).buildReason("Test reason")
				.buildComments("Test comments").buildMaintenanceStatus(MaintenanceStatus.IN_UITVOERING)
				.buildMachine(machine).build();

		maintenance.setId(1);

		MaintenanceDTO maintenanceDTO = new MaintenanceDTO(maintenance.getId(), maintenance.getExecutionDate(),
				maintenance.getStartDate(), maintenance.getEndDate(), null, maintenance.getReason(),
				maintenance.getComments(), maintenance.getStatus(), null);

		maintenances = new ArrayList<>();
		maintenances.add(maintenanceDTO);
	}

	@Test
	void getMaintenances_ShouldReturnListOfMaintenanceDTOs()
	{
		when(maintenanceController.getMaintenances()).thenReturn(maintenances);

		List<MaintenanceDTO> result = maintenanceController.getMaintenances();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(maintenance.getId(), result.get(0).id());
	}

	@Test
	void getMaintenance_ShouldReturnMaintenance()
	{
		when(maintenanceController.getMaintenance(1)).thenReturn(maintenance);

		Maintenance result = maintenanceController.getMaintenance(1);

		assertNotNull(result);
		assertEquals(maintenance.getId(), result.getId());
	}

	@ParameterizedTest
	@CsvSource({ "2025-05-01T10:00,2025-05-01T09:00", "2025-05-02T15:00,2025-05-01T15:00" })
	void build_withInvalidDates_throwsInformationRequiredException(String start, String end)
	{
		LocalDateTime startDate = LocalDateTime.parse(start);
		LocalDateTime endDate = LocalDateTime.parse(end);

		Maintenance.Builder builder = new Maintenance.Builder().buildExecutionDate(LocalDate.now())
				.buildStartDate(startDate).buildEndDate(endDate).buildTechnician(technician).buildReason("Test reason")
				.buildComments("Test comments").buildMaintenanceStatus(MaintenanceStatus.IN_UITVOERING)
				.buildMachine(machine);

		assertThrows(InformationRequiredExceptionMaintenance.class, builder::build);
	}

}
