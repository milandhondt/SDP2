package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import dto.MachineDTO;
import dto.MaintenanceDTO;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;
import exceptions.InformationRequiredExceptionMaintenance;
import gui.AppServices;
import lombok.Getter;
import lombok.Setter;
import repository.GenericDaoJpa;
import repository.UserDao;
import repository.UserDaoJpa;
import util.DTOMapper;
import util.MaintenanceStatus;

/**
 * Controller class for managing maintenance operations. Handles CRUD operations
 * for maintenance records and converts between DTOs and domain objects.
 */
public class MaintenanceController
{
	private GenericDaoJpa<Maintenance> maintenanceRepo;
	private GenericDaoJpa<Machine> machineRepo;
	private UserDao userRepo;

	/**
	 * Constructs a new MaintenanceController with default repository.
	 */
	public MaintenanceController()
	{
		maintenanceRepo = new GenericDaoJpa<Maintenance>(Maintenance.class);
		machineRepo = new GenericDaoJpa<Machine>(Machine.class);
		userRepo = new UserDaoJpa();
	}

	/**
	 * Constructs a new MaintenanceController with custom repository (mainly for
	 * testing).
	 * 
	 * @param maintenanceRepo the repository implementation to use
	 */
	public MaintenanceController(GenericDaoJpa<Maintenance> maintenanceRepo)
	{
		this.maintenanceRepo = maintenanceRepo;
		machineRepo = new GenericDaoJpa<Machine>(Machine.class);
		userRepo = new UserDaoJpa();
	}

	/**
	 * Gets the maintenance repository instance.
	 * 
	 * @return the maintenance repository
	 */
	public GenericDaoJpa<Maintenance> getMaintenanceDao()
	{
		return maintenanceRepo;
	}

	/**
	 * Retrieves all maintenance records as DTOs.
	 * 
	 * @return list of MaintenanceDTO objects
	 */
	public List<MaintenanceDTO> getMaintenances()
	{
		List<Maintenance> maintenances = maintenanceRepo.findAll();
		return makeMaintenanceDTOs(maintenances);
	}

	/**
	 * Converts a list of Maintenance objects to MaintenanceDTOs.
	 * 
	 * @param maintenances list of Maintenance objects
	 * @return unmodifiable list of MaintenanceDTO objects
	 */
	public List<MaintenanceDTO> makeMaintenanceDTOs(List<Maintenance> maintenances)
	{
		if (maintenances == null)
		{
			return List.of();
		}

		return maintenances.stream().map(this::makeMaintenanceDTO).collect(Collectors.toUnmodifiableList());
	}
	
	public Maintenance makeMaintenance(MaintenanceDTO maintenanceDTO)
	{
		return DTOMapper.toMaintenance(maintenanceDTO);
	}

	/**
	 * Converts a Maintenance object to a MaintenanceDTO.
	 * 
	 * @param maintenance the Maintenance object to convert
	 * @return converted MaintenanceDTO, or null if input is null
	 */
	public MaintenanceDTO makeMaintenanceDTO(Maintenance maintenance)
	{
		return DTOMapper.toMaintenanceDTO(maintenance);
	}
	
	/**
	 * Retrieves a Maintenance by its ID.
	 * 
	 * @param id the ID of the maintenance record
	 * @return the Maintenance object, or null if not found
	 */
	public Maintenance getMaintenance(int id)
	{
		return maintenanceRepo.get(id);
	}

	/**
	 * Retrieves a MaintenanceDTO by its ID.
	 * 
	 * @param id the ID of the maintenance record
	 * @return the MaintenanceDTO object, or null if not found
	 */
	public MaintenanceDTO getMaintenanceDTO(int id)
	{
		Maintenance maintenance = getMaintenance(id);
		return makeMaintenanceDTO(maintenance);
	}

	/**
	 * Creates a new maintenance record in the database.
	 * 
	 * @param maintenance the Maintenance object to create
	 */
	public void createMaintenance(Maintenance maintenance)
	{
		maintenanceRepo.startTransaction();
		maintenanceRepo.insert(maintenance);
		maintenanceRepo.commitTransaction();
	}

	/**
	 * Creates a new maintenance record with detailed parameters.
	 * 
	 * @param executionDate the date when maintenance was executed
	 * @param startDate     the planned start date/time
	 * @param endDate       the planned end date/time
	 * @param technicianId  the ID of the technician
	 * @param reason        the reason for maintenance
	 * @param comments      additional comments
	 * @param status        the maintenance status
	 * @param machineId     the ID of the machine being maintained
	 * @return the created MaintenanceDTO
	 * @throws InformationRequiredExceptionMaintenance if required fields are
	 *                                                 missing
	 */
	public MaintenanceDTO createMaintenance(LocalDate executionDate, LocalDateTime startDate, LocalDateTime endDate,
			int technicianId, String reason, String comments, MaintenanceStatus status, int machineId)
			throws InformationRequiredExceptionMaintenance
	{
		
		User technician = userRepo.get(technicianId);
		Machine machine = machineRepo.get(machineId);
		
		Maintenance maintenance = new Maintenance.Builder()
				.buildExecutionDate(executionDate)
				.buildStartDate(startDate)
				.buildEndDate(endDate)
				.buildTechnician(technician)
				.buildReason(reason)
				.buildComments(comments)
				.buildMaintenanceStatus(status)
				.buildMachine(machine)
				.build();

		createMaintenance(maintenance);

		if (status == MaintenanceStatus.VOLTOOID
				&& (machine.getLastMaintenance() == null || executionDate.isAfter(machine.getLastMaintenance())))
		{
			machine.setLastMaintenance(executionDate);
			updateMachine(machine);
		}
		
		return makeMaintenanceDTO(maintenance);
	}

	/**
	 * Updates an existing maintenance record.
	 * 
	 * @param maintenance the Maintenance object to update
	 */
	public void updateMaintenance(Maintenance maintenance)
	{
		maintenanceRepo.startTransaction();
		maintenanceRepo.update(maintenance);
		maintenanceRepo.commitTransaction();
	}

	/**
	 * Updates an existing maintenance record with detailed parameters.
	 * 
	 * @param maintenanceId the ID of the maintenance to update
	 * @param executionDate the new execution date
	 * @param startDate     the new start date/time
	 * @param endDate       the new end date/time
	 * @param technicianId  the new technician ID
	 * @param reason        the new reason
	 * @param comments      the new comments
	 * @param status        the new status
	 * @param machineId     the new machine ID
	 * @return the updated MaintenanceDTO
	 * @throws InformationRequiredExceptionMaintenance if required fields are
	 *                                                 missing
	 * @throws IllegalArgumentException                if maintenance with given ID
	 *                                                 is not found
	 */
	public MaintenanceDTO updateMaintenance(int maintenanceId, LocalDate executionDate, LocalDateTime startDate,
			LocalDateTime endDate, int technicianId, String reason, String comments, MaintenanceStatus status,
			int machineId) throws InformationRequiredExceptionMaintenance
	{

		Maintenance existingMaintenance = getMaintenance(maintenanceId);
		if (existingMaintenance == null)
		{
			throw new IllegalArgumentException("Maintenance with ID " + maintenanceId + " not found");
		}

		User technician = getUserById(technicianId);
		Machine machine = getMachineById(machineId);

		Maintenance maintenance = new Maintenance.Builder()
				.buildExecutionDate(executionDate)
				.buildStartDate(startDate)
				.buildEndDate(endDate)
				.buildTechnician(technician)
				.buildReason(reason)
				.buildComments(comments)
				.buildMaintenanceStatus(status)
				.buildMachine(machine)
				.build();

		maintenance.setId(existingMaintenance.getId());
		updateMaintenance(maintenance);

		if (status == MaintenanceStatus.VOLTOOID
				&& (machine.getLastMaintenance() == null || executionDate.isAfter(machine.getLastMaintenance())))
		{
			machine.setLastMaintenance(executionDate);
			updateMachine(machine);
		}

		return makeMaintenanceDTO(maintenance);
	}

	/**
	 * Helper method to get a User by ID.
	 * 
	 * @param userId the ID of the user
	 * @return the User object
	 */
	private User getUserById(int userId)
	{
		UserController uc = AppServices.getInstance().getUserController();
		UserDTO userDTO = uc.getUserById(userId);
		return DTOMapper.toUser(userDTO);
	}

	/**
	 * Helper method to get a Machine by ID.
	 * 
	 * @param machineId the ID of the machine
	 * @return the Machine object
	 */
	private Machine getMachineById(int machineId)
	{
		MachineController mc = AppServices.getInstance().getMachineController();
		MachineDTO machine = mc.getMachineById(machineId);
		return DTOMapper.toMachine(machine);
	}

	/**
	 * Helper method to update a Machine.
	 * 
	 * @param machine the Machine object to update
	 */
	private void updateMachine(Machine machine)
	{
		MachineController mc = AppServices.getInstance().getMachineController();
		mc.updateMachine(machine);
	}
}