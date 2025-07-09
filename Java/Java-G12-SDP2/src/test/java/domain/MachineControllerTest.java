package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.Machine;
import domain.NotificationObserver;
import dto.MachineDTO;
import interfaces.Observer;
import interfaces.Subject;
import util.MachineStatus;
import util.ProductionStatus;

class MachineControllerTest
{

	private MachineRepository machineRepo;
	private MachineControllerTestImpl controller;
	private NotificationObserver notificationObserver;

	interface MachineRepository
	{
		List<Machine> findAll();

		Machine get(int id);

		void startTransaction();

		void insert(Machine machine);

		void update(Machine machine);

		void commitTransaction();
	}

	@BeforeEach
	void setUp()
	{
		machineRepo = mock(MachineRepository.class);
		notificationObserver = mock(NotificationObserver.class);

		controller = new MachineControllerTestImpl(machineRepo);
		controller.addObserver(notificationObserver);
	}

	private static class MachineControllerTestImpl implements Subject
	{
		private final MachineRepository machineRepo;
		private final List<Observer> observers = new ArrayList<>();

		public MachineControllerTestImpl(MachineRepository machineRepo)
		{
			this.machineRepo = machineRepo;
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
			observers.forEach(o -> o.update(message));
		}

		public List<MachineDTO> getMachineList()
		{
			List<Machine> machines = machineRepo.findAll();
			if (machines == null)
			{
				return List.of();
			}
			return machines.stream().map(this::toMachineDTO).toList();
		}

		public void addNewMachine(Machine machine)
		{
			machineRepo.startTransaction();
			machineRepo.insert(machine);
			machineRepo.commitTransaction();
			notifyObservers("Nieuwe machine toegevoegd: " + machine.getCode());
		}

		public void updateMachine(Machine machine)
		{
			machineRepo.startTransaction();
			machineRepo.update(machine);
			machineRepo.commitTransaction();
			notifyObservers("Machine bijgewerkt: " + machine.getCode());
		}

		public MachineDTO getMachineById(int machineId)
		{
			return toMachineDTO(machineRepo.get(machineId));
		}

		private MachineDTO toMachineDTO(Machine machine)
		{
			if (machine == null)
				return null;
			return new MachineDTO(machine.getId(), null, null, machine.getCode(), machine.getMachineStatus(),
					machine.getProductionStatus(), machine.getLocation(), machine.getProductInfo(), null,
					machine.getFutureMaintenance(), 0, 0.0);
		}
	}

	@Test
	void testGetMachineListEmpty()
	{
		when(machineRepo.findAll()).thenReturn(List.of());

		List<MachineDTO> result = controller.getMachineList();

		assertTrue(result.isEmpty());
		verify(machineRepo, times(1)).findAll();
	}

	@Test
	void testGetMachineListWithItems()
	{
		Machine machine1 = createTestMachine(1, "M001");
		Machine machine2 = createTestMachine(2, "M002");
		when(machineRepo.findAll()).thenReturn(List.of(machine1, machine2));

		List<MachineDTO> result = controller.getMachineList();

		assertEquals(2, result.size());
		assertEquals("M001", result.get(0).code());
		assertEquals("M002", result.get(1).code());
		verify(machineRepo, times(1)).findAll();
	}

	@Test
	void testAddNewMachine()
	{
		Machine machine = createTestMachine(1, "M001");

		controller.addNewMachine(machine);

		verify(machineRepo, times(1)).startTransaction();
		verify(machineRepo, times(1)).insert(machine);
		verify(machineRepo, times(1)).commitTransaction();
		verify(notificationObserver, times(1)).update("Nieuwe machine toegevoegd: M001");
	}

	@Test
	void testUpdateMachine()
	{
		Machine machine = createTestMachine(1, "M001");

		controller.updateMachine(machine);

		verify(machineRepo, times(1)).startTransaction();
		verify(machineRepo, times(1)).update(machine);
		verify(machineRepo, times(1)).commitTransaction();
		verify(notificationObserver, times(1)).update("Machine bijgewerkt: M001");
	}

	@Test
	void testGetMachineById()
	{
		Machine machine = createTestMachine(1, "M001");
		when(machineRepo.get(1)).thenReturn(machine);

		MachineDTO result = controller.getMachineById(1);

		assertNotNull(result);
		assertEquals(1, result.id());
		assertEquals("M001", result.code());
		verify(machineRepo, times(1)).get(1);
	}

	@Test
	void testGetMachineByIdNotFound()
	{
		when(machineRepo.get(999)).thenReturn(null);

		MachineDTO result = controller.getMachineById(999);

		assertNull(result);
		verify(machineRepo, times(1)).get(999);
	}

	@Test
	void testObserverManagement()
	{
		Observer testObserver = mock(Observer.class);

		controller.addObserver(testObserver);
		controller.notifyObservers("Test message");

		verify(testObserver, times(1)).update("Test message");

		controller.removeObserver(testObserver);
		controller.notifyObservers("Another message");

		verify(testObserver, times(1)).update("Test message");
	}

	private Machine createTestMachine(int id, String code)
	{
		Machine machine = new Machine();
		machine.setId(id);
		machine.setCode(code);
		machine.setMachineStatus(MachineStatus.DRAAIT);
		machine.setProductionStatus(ProductionStatus.GEZOND);
		machine.setLocation("Test Location");
		machine.setProductInfo("Test Product");
		machine.setFutureMaintenance(LocalDate.now().plusMonths(1));
		return machine;
	}
}