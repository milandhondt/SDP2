package domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import dto.MachineDTO;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;
import exceptions.InformationRequiredExceptionMachine;
import interfaces.Observer;
import interfaces.Subject;
import repository.GenericDaoJpa;
import util.DTOMapper;
import util.I18n;
import util.MachineStatus;
import util.ProductionStatus;

/**
 * Controller class for managing machine operations. Implements the Subject
 * interface for observer pattern functionality. Handles CRUD operations for
 * machines and converts between DTOs and domain objects.
 */
public class MachineController implements Subject
{
	private GenericDaoJpa<Machine> machineRepo;
	private List<Observer> observers = new ArrayList<>();

	/**
	 * Constructs a new MachineController and initializes dependencies.
	 * Automatically adds a NotificationObserver to observe changes.
	 */
	public MachineController()
	{
		machineRepo = new GenericDaoJpa<Machine>(Machine.class);
		addObserver(new NotificationObserver());
	}

	/**
	 * Retrieves all machines and converts them to DTOs.
	 * 
	 * @return an unmodifiable list of MachineDTO objects
	 */
	public List<MachineDTO> getMachineList()
	{
		List<Machine> machines = machineRepo.findAll();
		if (machines == null)
		{
			return List.of();
		}
		return machines.stream().map(machine -> DTOMapper.toMachineDTO(machine)).toList();
	}

	/**
	 * Adds a new machine to the system and notifies observers.
	 * 
	 * @param machine the machine to add
	 */
	public void addNewMachine(Machine machine)
	{
		machineRepo.startTransaction();
		machineRepo.insert(machine);
		machineRepo.commitTransaction();
		notifyObservers("Nieuwe machine toegevoegd: " + machine.getCode());
	}

	/**
	 * Updates an existing machine and notifies observers.
	 * 
	 * @param machine the machine to update
	 */
	public void updateMachine(Machine machine)
	{
		machineRepo.startTransaction();
		machineRepo.update(machine);
		machineRepo.commitTransaction();
		notifyObservers("Machine bijgewerkt: " + machine.getCode());
	}

	/**
	 * Adds a new machine using DTO input.
	 * 
	 * @param machineDTO the machine data transfer object
	 */
	public void addNewMachine(MachineDTO machineDTO)
	{
		Machine machine = DTOMapper.toMachine(machineDTO);
		addNewMachine(machine);
	}

	/**
	 * Creates and adds a new machine with full details.
	 * 
	 * @param siteDTO           the site where the machine is located
	 * @param technicianDTO     the technician responsible for the machine
	 * @param code              the machine code/identifier
	 * @param machineStatus     the operational status of the machine
	 * @param productionStatus  the production status of the machine
	 * @param location          the physical location of the machine
	 * @param productInfo       information about products the machine handles
	 * @param futureMaintenance scheduled maintenance date
	 * @return the created machine as DTO
	 * @throws InformationRequiredExceptionMachine if required fields are missing
	 */
	public MachineDTO createMachine(SiteDTOWithoutMachines siteDTO, UserDTO technicianDTO, String code,
			MachineStatus machineStatus, ProductionStatus productionStatus, String location, String productInfo,
			LocalDate futureMaintenance) throws InformationRequiredExceptionMachine
	{

		Site site = DTOMapper.toSite(siteDTO);
		User technician = DTOMapper.toUser(technicianDTO);

		Machine machine = new Machine.Builder().buildSite(site).buildTechnician(technician).buildCode(code)
				.buildMachineStatus(machineStatus).buildProductionStatus(productionStatus).buildLocation(location)
				.buildProductInfo(productInfo).buildFutureMaintenance(futureMaintenance).build();

		addNewMachine(machine);
		return DTOMapper.toMachineDTO(machine);
	}

	/**
	 * Updates an existing machine with new details.
	 * 
	 * @param id                the ID of the machine to update
	 * @param siteDTO           the new site information
	 * @param technicianDTO     the new technician information
	 * @param code              the new machine code
	 * @param machineStatus     the new machine status
	 * @param productionStatus  the new production status
	 * @param location          the new location
	 * @param productInfo       the new product information
	 * @param futureMaintenance the new maintenance date
	 * @return the updated machine as DTO
	 * @throws InformationRequiredExceptionMachine if required fields are missing
	 */
	public MachineDTO updateMachine(int id, SiteDTOWithoutMachines siteDTO, UserDTO technicianDTO, String code,
			MachineStatus machineStatus, ProductionStatus productionStatus, String location, String productInfo,
			LocalDate futureMaintenance) throws InformationRequiredExceptionMachine
	{

		Machine existingMachine = machineRepo.get(id);
		Site site = DTOMapper.toSite(siteDTO);
		User technician = DTOMapper.toUser(technicianDTO);

		Machine machine = new Machine.Builder().buildSite(site).buildTechnician(technician).buildCode(code)
				.buildMachineStatus(machineStatus).buildProductionStatus(productionStatus).buildLocation(location)
				.buildProductInfo(productInfo).buildFutureMaintenance(futureMaintenance).build();

		machine.setId(existingMachine.getId());
		updateMachine(machine);
		return DTOMapper.toMachineDTO(machine);
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

	/**
	 * Retrieves a machine by its ID.
	 * 
	 * @param machineId the ID of the machine to retrieve
	 * @return the Machine object, or null if not found
	 */
	public MachineDTO getMachineById(int machineId)
	{
		return DTOMapper.toMachineDTO(machineRepo.get(machineId));
	}

	/**
	 * Retrieves all distinct production status values from sites.
	 * 
	 * @return List of unique status strings
	 */
	public Collection<? extends String> getAllProductionStatusses()
	{
		List<MachineDTO> allMachines = getMachineList();
		return allMachines.stream().map(m -> m.productionStatus().toString()).distinct().sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves all distinct machine status values from sites.
	 * 
	 * @return List of unique status strings
	 */
	public Collection<? extends String> getAllMachineStatusses()
	{
		List<MachineDTO> allMachines = getMachineList();
		return allMachines.stream().map(m -> m.machineStatus().toString()).distinct().sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves filtered machines based on multiple criteria.
	 * 
	 * @param searchFilter     general search term to filter by
	 * @param selectedProdStat productionstatus to filter by
	 * @param selectedMachStat machinestatus to filter by
	 * @return List of filtered MachineDTOs
	 */
	public List<MachineDTO> getFilteredMachines(String searchFilter, String selectedProdStat, String selectedMachStat)
	{
		String lowerCaseSearchFilter = searchFilter == null ? "" : searchFilter.toLowerCase();

		return getMachineList().stream()
				.filter(machine -> selectedProdStat == null
						|| I18n.convertStatus(machine.productionStatus().toString()).equals(I18n.convertStatus(selectedProdStat)))
				.filter(machine -> selectedMachStat == null
						|| I18n.convertStatus(machine.machineStatus().toString()).equals(I18n.convertStatus(selectedMachStat)))
				.filter(machine -> machine.code().toLowerCase().contains(lowerCaseSearchFilter)
						|| machine.location().toLowerCase().contains(lowerCaseSearchFilter)
						|| machine.site().siteName().toLowerCase().contains(lowerCaseSearchFilter)
						|| machine.productInfo().toLowerCase().contains(lowerCaseSearchFilter))
				.collect(Collectors.toList());
	}
}