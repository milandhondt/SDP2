package domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import exceptions.InformationRequired;
import exceptions.InformationRequiredExceptionMaintenance;
import interfaces.RequiredElement;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import util.MaintenanceStatus;
import util.RequiredElementMaintenance;

/**
 * Represents a maintenance operation on a machine. Includes details such as
 * execution date, technician, reason, and status.
 */
@Entity
@Table(name = "maintenances")
@NoArgsConstructor
@Getter
@Setter
public class Maintenance implements Serializable
{

	private static final long serialVersionUID = 1L;

	/** Unique identifier for the maintenance record. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private int id;

	/** The date the maintenance was executed. */
	private LocalDate executionDate;

	/** The start date and time of the maintenance. */
	private LocalDateTime startDate;

	/** The end date and time of the maintenance. */
	private LocalDateTime endDate;

	/** The technician responsible for performing the maintenance. */
	@ManyToOne(cascade =
	{ CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "technician_id")
	private User technician;

	/** The reason why the maintenance was required. */
	private String reason;

	/** Any additional comments about the maintenance. */
	private String comments;

	/** The status of the maintenance (e.g., planned, in progress, completed). */
	@Enumerated(EnumType.STRING)
	private MaintenanceStatus status;

	/** The machine on which the maintenance is being performed. */
	@ManyToOne(cascade =
	{ CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "machine_id")
	private Machine machine;

	/**
	 * Validates that the end date is not before the start date. This method is
	 * automatically called before persisting or updating.
	 */
	@PrePersist
	@PreUpdate
	void validateDates()
	{
		if (startDate != null && endDate != null && endDate.isBefore(startDate))
		{
			throw new IllegalStateException("End date cannot be before start date.");
		}
	}

	/**
	 * Constructs a new Maintenance object with the specified properties.
	 *
	 * @param executionDate the date of execution
	 * @param startDate     the start date and time
	 * @param endDate       the end date and time
	 * @param technician    the technician performing the maintenance
	 * @param reason        the reason for maintenance
	 * @param comments      any additional comments
	 * @param status        the maintenance status
	 * @param machine       the machine being maintained
	 */
	Maintenance(Builder builder)
	{
		this.executionDate = builder.executionDate;
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.technician = builder.technician;
		this.reason = builder.reason;
		this.comments = builder.comments;
		this.status = builder.status;
		this.machine = builder.machine;
	}

	/**
	 * Builder class for constructing a {@link Maintenance} object step-by-step.
	 */
	public static class Builder
	{

		private LocalDate executionDate;
		private LocalDateTime startDate;
		private LocalDateTime endDate;
		private User technician;
		private String reason;
		private String comments;
		private MaintenanceStatus status;
		private Machine machine;
		protected Maintenance maintenance;

		/**
		 * Sets the execution date for the maintenance.
		 * 
		 * @param executionDate the execution date
		 * @return the builder instance
		 */
		public Builder buildExecutionDate(LocalDate executionDate)
		{
			this.executionDate = executionDate;
			return this;
		}

		/**
		 * Sets the start date and time for the maintenance.
		 * 
		 * @param startDate the start date and time
		 * @return the builder instance
		 */
		public Builder buildStartDate(LocalDateTime startDate)
		{
			this.startDate = startDate;
			return this;
		}

		/**
		 * Sets the end date and time for the maintenance.
		 * 
		 * @param endDate the end date and time
		 * @return the builder instance
		 */
		public Builder buildEndDate(LocalDateTime endDate)
		{
			this.endDate = endDate;
			return this;
		}

		/**
		 * Sets the technician responsible for the maintenance.
		 * 
		 * @param technician the technician
		 * @return the builder instance
		 */
		public Builder buildTechnician(User technician)
		{
			this.technician = technician;
			return this;
		}

		/**
		 * Sets the reason for the maintenance.
		 * 
		 * @param reason the reason
		 * @return the builder instance
		 */
		public Builder buildReason(String reason)
		{
			this.reason = reason;
			return this;
		}

		/**
		 * Sets any additional comments for the maintenance.
		 * 
		 * @param comments the comments
		 * @return the builder instance
		 */
		public Builder buildComments(String comments)
		{
			this.comments = comments;
			return this;
		}

		/**
		 * Sets the status of the maintenance.
		 * 
		 * @param status the status
		 * @return the builder instance
		 */
		public Builder buildMaintenanceStatus(MaintenanceStatus status)
		{
			this.status = status;
			return this;
		}

		/**
		 * Sets the machine on which the maintenance is being performed.
		 * 
		 * @param machine the machine
		 * @return the builder instance
		 */
		public Builder buildMachine(Machine machine)
		{
			this.machine = machine;
			return this;
		}

		/**
		 * Builds and returns the {@link Maintenance} object after validation.
		 *
		 * @return the constructed Maintenance object
		 * @throws InformationRequiredExceptionMaintenance if any required fields are
		 *                                                 missing or invalid
		 */
		public Maintenance build() throws InformationRequiredExceptionMaintenance
		{
			validateRequiredFields();
			return new Maintenance(this);
		}

		/**
		 * Validates that all required fields are present and consistent.
		 *
		 * @throws InformationRequiredExceptionMaintenance if required fields are
		 *                                                 missing or invalid
		 */
		private void validateRequiredFields() throws InformationRequired
		{
			Map<String, RequiredElement> requiredElements = new HashMap<>();

			if (executionDate == null)
				requiredElements.put("executionDate", RequiredElementMaintenance.EXECUTION_DATE_REQUIRED);

			if (startDate == null)
				requiredElements.put("startDate", RequiredElementMaintenance.START_DATE_REQUIRED);

			if (endDate == null)
				requiredElements.put("endDate", RequiredElementMaintenance.END_DATE_REQUIRED);

			if (technician == null)
				requiredElements.put("technician", RequiredElementMaintenance.TECHNICIAN_REQUIRED);

			if (reason == null || reason.isBlank())
				requiredElements.put("reason", RequiredElementMaintenance.REASON_REQUIRED);

			if (status == null)
				requiredElements.put("status", RequiredElementMaintenance.MAINTENANCESTATUS_REQUIRED);

			if (machine == null)
				requiredElements.put("machine", RequiredElementMaintenance.MACHINE_REQUIRED);

			if (startDate != null && endDate != null && endDate.isBefore(startDate))
				requiredElements.put("endDate", RequiredElementMaintenance.END_DATE_BEFORE_START);

			if (!requiredElements.isEmpty())
				throw new InformationRequiredExceptionMaintenance(requiredElements);
		}
	}
}
