package domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.Machine;
import domain.Site;
import domain.User;
import exceptions.InformationRequiredExceptionMachine;
import util.MachineStatus;
import util.ProductionStatus;

class MachineTest
{

	private Site mockSite;
	private User mockTechnician;
	private Machine machine;

	@BeforeEach
	void setUp()
	{
		mockSite = mock(Site.class);
		when(mockSite.getMachines()).thenReturn(new HashSet<>());

		mockTechnician = mock(User.class);

		machine = new Machine.Builder().buildSite(mockSite).buildTechnician(mockTechnician).buildCode("MCH-001")
				.buildLocation("Zone A").buildProductInfo("Widget Maker").buildMachineStatus(MachineStatus.DRAAIT)
				.buildProductionStatus(ProductionStatus.GEZOND).buildFutureMaintenance(LocalDate.now().plusMonths(1))
				.build();
	}

	@Test
	void builder_buildsMachineWithAllFields()
	{
		LocalDate futureMaintenance = LocalDate.of(2025, 12, 1);
		Machine constructed = new Machine.Builder().buildSite(mockSite).buildTechnician(mockTechnician)
				.buildCode("MCH-123").buildLocation("Room B").buildProductInfo("Info")
				.buildMachineStatus(MachineStatus.DRAAIT).buildProductionStatus(ProductionStatus.FALEND)
				.buildFutureMaintenance(futureMaintenance).build();

		assertAll(() -> assertEquals(mockSite, constructed.getSite()),
				() -> assertEquals(mockTechnician, constructed.getTechnician()),
				() -> assertEquals("MCH-123", constructed.getCode()),
				() -> assertEquals("Room B", constructed.getLocation()),
				() -> assertEquals("Info", constructed.getProductInfo()),
				() -> assertEquals(MachineStatus.DRAAIT, constructed.getMachineStatus()),
				() -> assertEquals(ProductionStatus.FALEND, constructed.getProductionStatus()),
				() -> assertEquals(futureMaintenance, constructed.getFutureMaintenance()));
	}

	@Test
	void getUpTimeInHours_lastMaintenanceToday_returnsLessThan24()
	{
		double hours = machine.getUpTimeInHours();
		assertTrue(hours >= 0 && hours < 24);
	}

	@Test
	void setSite_removesMachineFromOldSiteAndAddsToNewSite()
	{
		Site oldSite = mock(Site.class);
		HashSet<Machine> oldMachines = new HashSet<>();
		when(oldSite.getMachines()).thenReturn(oldMachines);
		oldMachines.add(machine);

		Site newSite = mock(Site.class);
		HashSet<Machine> newMachines = new HashSet<>();
		when(newSite.getMachines()).thenReturn(newMachines);

		machine.setSite(oldSite);
		assertEquals(oldSite, machine.getSite());
		assertTrue(oldMachines.contains(machine));

		machine.setSite(newSite);
		assertEquals(newSite, machine.getSite());
		assertFalse(oldMachines.contains(machine));
		assertTrue(newMachines.contains(machine));
	}

	@Test
	void builder_missingRequiredFields_throwsException()
	{
		Machine.Builder builder = new Machine.Builder();

		InformationRequiredExceptionMachine thrown = assertThrows(InformationRequiredExceptionMachine.class,
				builder::build);

		assertTrue(thrown.getRequiredElements().containsKey("site"));
		assertTrue(thrown.getRequiredElements().containsKey("technician"));
		assertTrue(thrown.getRequiredElements().containsKey("code"));
		assertTrue(thrown.getRequiredElements().containsKey("machineStatus"));
		assertTrue(thrown.getRequiredElements().containsKey("productionStatus"));
		assertTrue(thrown.getRequiredElements().containsKey("location"));
		assertTrue(thrown.getRequiredElements().containsKey("productInfo"));
		assertTrue(thrown.getRequiredElements().containsKey("futureMaintenance"));
	}
}
