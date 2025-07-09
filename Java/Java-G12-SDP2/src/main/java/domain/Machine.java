package domain;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exceptions.InformationRequiredExceptionMachine;
import interfaces.Observer;
import interfaces.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import util.MachineStatus;
import util.MachineStatusConverter;
import util.ProductionStatus;
import util.RequiredElementMachine;

/**
 * Represents a machine in a production site.
 * 
 * A machine is associated with a {@link Site}, a {@link User} (technician), and
 * contains information about its operational status, maintenance dates, and
 * production data.
 * 
 */
@Entity
@ToString
@NoArgsConstructor
@Getter
@Setter
@Table(name = "machines")
public class Machine implements Serializable, Subject
{

	private static final long serialVersionUID = 1L;

	/**
	 * The unique identifier of the machine.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * The site where the machine is located.
	 */
	@ManyToOne
	@JoinColumn(name = "SITE_ID")
	private Site site;

	/**
	 * The technician responsible for the machine.
	 */
	@ManyToOne
	private User technician;

	/**
	 * The observers interested in updates about this machine (not persisted).
	 */
	@Transient
	private List<Observer> observers;

	/**
	 * The unique code identifying the machine.
	 */
	private String code;

	/**
	 * The location of the machine within the site.
	 */
	private String location;

	/**
	 * Product information the machine is currently associated with.
	 */
	private String productInfo;

	/**
	 * The operational status of the machine.
	 */
	@Column(name = "MACHINESTATUS")
	@Convert(converter = MachineStatusConverter.class)
	private MachineStatus machineStatus;

	/**
	 * The production status of the machine.
	 */
	private ProductionStatus productionStatus;

	/**
	 * Date of the last maintenance operation.
	 */
	private LocalDate lastMaintenance;

	/**
	 * Date of the next scheduled maintenance.
	 */
	private LocalDate futureMaintenance;

	/**
	 * Number of days since the last maintenance.
	 */
	private int numberDaysSinceLastMaintenance;

	/**
	 * Constructs a new Machine with the given parameters.
	 *
	 * @param site              the site where the machine is located
	 * @param technician        the responsible technician
	 * @param code              the machine code
	 * @param location          the location of the machine
	 * @param productInfo       information about the product
	 * @param machineStatus     the operational status
	 * @param productionStatus  the production status
	 * @param futureMaintenance the next maintenance date
	 */
	private Machine(Builder builder)
	{
		this.site = builder.site;
		this.technician = builder.technician;
		this.code = builder.code;
		this.location = builder.location;
		this.productInfo = builder.productInfo;
		this.machineStatus = builder.machineStatus;
		this.productionStatus = builder.productionStatus;
		this.futureMaintenance = builder.futureMaintenance;
		this.lastMaintenance = LocalDate.now();
		this.numberDaysSinceLastMaintenance = 0;
	}

	/**
	 * Calculates the uptime of the machine since its last maintenance, in hours.
	 *
	 * @return the uptime in hours, or 0.0 if {@code lastMaintenance} is null
	 */
	public double getUpTimeInHours()
	{
		if (lastMaintenance == null)
		{
			return 0.0;
		}
		LocalDateTime maintenanceDateTime = lastMaintenance.atStartOfDay();
		return Duration.between(maintenanceDateTime, LocalDateTime.now()).toHours();
	}

	/**
	 * Sets the site of the machine and updates the bidirectional relationship.
	 *
	 * @param site the site to set
	 */
	public void setSite(Site site)
	{
		if (this.site != null)
		{
			this.site.getMachines().remove(this);
		}
		this.site = site;
		if (site != null)
		{
			site.getMachines().add(this);
		}
	}

	@Override
	public void addObserver(Observer o)
	{
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o)
	{
		observers.remove(o);
	}

	@Override
	public void notifyObservers(String message)
	{
		observers.stream().forEach((o) -> o.update(message));
	}

	/**
	 * Builder class for constructing {@link Machine} instances.
	 */
	public static class Builder
	{
		private Site site;
		private User technician;
		private String code;
		private String location;
		private String productInfo;
		private MachineStatus machineStatus;
		private ProductionStatus productionStatus;
		private LocalDate futureMaintenance;

		public Builder buildSite(Site site)
		{
			this.site = site;
			return this;
		}

		public Builder buildTechnician(User technician)
		{
			this.technician = technician;
			return this;
		}

		public Builder buildCode(String code)
		{
			this.code = code;
			return this;
		}

		public Builder buildLocation(String location)
		{
			this.location = location;
			return this;
		}

		public Builder buildProductInfo(String productInfo)
		{
			this.productInfo = productInfo;
			return this;
		}

		public Builder buildMachineStatus(MachineStatus machineStatus)
		{
			this.machineStatus = machineStatus;
			return this;
		}

		public Builder buildProductionStatus(ProductionStatus productionStatus)
		{
			this.productionStatus = productionStatus;
			return this;
		}

		public Builder buildFutureMaintenance(LocalDate futureMaintenance)
		{
			this.futureMaintenance = futureMaintenance;
			return this;
		}

		/**
		 * Builds a {@link Machine} object with the configured values.
		 *
		 * @return the constructed {@code Machine} instance
		 * @throws InformationRequiredExceptionMachine if required fields are missing
		 */
		public Machine build() throws InformationRequiredExceptionMachine
		{
			validateRequiredFields();
			return new Machine(this);
		}

		/**
		 * Validates that all required fields for building a machine are provided.
		 *
		 * @throws InformationRequiredExceptionMachine if one or more required fields
		 *                                             are missing
		 */
		private void validateRequiredFields() throws InformationRequiredExceptionMachine
		{
			Map<String, RequiredElementMachine> requiredElements = new HashMap<>();

			if (site == null)
				requiredElements.put("site", RequiredElementMachine.SITE_REQUIRED);

			if (technician == null)
				requiredElements.put("technician", RequiredElementMachine.TECHNICIAN_REQUIRED);

			if (code == null || code.isEmpty())
				requiredElements.put("code", RequiredElementMachine.CODE_REQUIRED);

			if (machineStatus == null)
				requiredElements.put("machineStatus", RequiredElementMachine.MACHINESTATUS_REQUIRED);

			if (productionStatus == null)
				requiredElements.put("productionStatus", RequiredElementMachine.PRODUCTIONSTATUS_REQUIRED);

			if (location == null || location.isEmpty())
				requiredElements.put("location", RequiredElementMachine.LOCATION_REQUIRED);

			if (productInfo == null || productInfo.isEmpty())
				requiredElements.put("productInfo", RequiredElementMachine.PRODUCTINFO_REQUIRED);

			if (futureMaintenance == null)
				requiredElements.put("futureMaintenance", RequiredElementMachine.FUTURE_MAINTENANCE_REQUIRED);

			if (!requiredElements.isEmpty())
				throw new InformationRequiredExceptionMachine(requiredElements);
		}
	}
}
