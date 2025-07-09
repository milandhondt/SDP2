package domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import exceptions.InformationRequired;
import exceptions.InformationRequiredExceptionReport;
import interfaces.RequiredElement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import util.RequiredElementReport;

/**
 * Entity representing a maintenance report. Contains details about the
 * maintenance activity, technician, timing, and related site.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "reportId")
@Getter
@Setter
public class Report implements Serializable
{

	private static final long serialVersionUID = 1L;

	/**
	 * Unique identifier for the report, auto-generated.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "REPORTID", columnDefinition = "INT UNSIGNED")
	private int reportId;

	/**
	 * The site where the maintenance took place.
	 */
	@ManyToOne
	@JoinColumn(name = "site_id", columnDefinition = "INT UNSIGNED")
	private Site site;

	/**
	 * The maintenance activity associated with this report. 
	 */
	@ManyToOne
	@JoinColumn(name = "maintenance_id", columnDefinition = "INT UNSIGNED")
	private Maintenance maintenance;

	/**
	 * The technician responsible for the maintenance.
	 */
	@ManyToOne
	private User technician;

	/**
	 * The start date of the maintenance.
	 */
	private LocalDate startDate;

	/**
	 * The start time of the maintenance.
	 */
	private LocalTime startTime;

	/**
	 * The end date of the maintenance.
	 */
	private LocalDate endDate;

	/**
	 * The end time of the maintenance.
	 */
	private LocalTime endTime;

	/**
	 * Reason for performing the maintenance.
	 */
	private String reason;

	/**
	 * Additional remarks or comments related to the maintenance.
	 */
	private String remarks;

	/**
	 * Constructs a new Report instance with the specified details.
	 * 
	 * @param selectedMaintenance the maintenance activity
	 * @param selectedTechnician  the technician performing the maintenance
	 * @param startDate           start date of maintenance
	 * @param startTime           start time of maintenance
	 * @param endDate             end date of maintenance
	 * @param endTime             end time of maintenance
	 * @param reason              reason for maintenance
	 * @param remarks             additional remarks
	 * @param site                site where maintenance took place
	 */
	private Report(Builder builder)
	{
		this.site = builder.site;
		this.technician = builder.technician;
		this.startDate = builder.startDate;
		this.startTime = builder.startTime;
		this.maintenance = builder.maintenance;
		this.endDate = builder.endDate;
		this.endTime = builder.endTime;
		this.reason = builder.reason;
		this.remarks = builder.remarks;
	}

	/**
	 * Builder for creating {@link Report} instances in a flexible and readable
	 * manner.
	 */
	public static class Builder
	{
		private Site site;
		private User technician;
		private LocalDate startDate;
		private LocalTime startTime;
		private LocalDate endDate;
		private LocalTime endTime;
		private String reason;
		private String remarks;
		private Maintenance maintenance;

		/**
		 * Sets the site of the report.
		 * 
		 * @param site the site
		 * @return the builder instance
		 */
		public Builder buildSite(Site site)
		{
			this.site = site;
			return this;
		}

		/**
		 * Sets the technician responsible for the report.
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
		 * Sets the start date of the maintenance.
		 * 
		 * @param startDate the start date
		 * @return the builder instance
		 */
		public Builder buildstartDate(LocalDate startDate)
		{
			this.startDate = startDate;
			return this;
		}

		/**
		 * Sets the start time of the maintenance.
		 * 
		 * @param startTime the start time
		 * @return the builder instance
		 */
		public Builder buildStartTime(LocalTime startTime)
		{
			this.startTime = startTime;
			return this;
		}

		/**
		 * Sets the end date of the maintenance.
		 * 
		 * @param endDate the end date
		 * @return the builder instance
		 */
		public Builder buildEndDate(LocalDate endDate)
		{
			this.endDate = endDate;
			return this;
		}

		/**
		 * Sets the end time of the maintenance.
		 * 
		 * @param endTime the end time
		 * @return the builder instance
		 */
		public Builder buildEndTime(LocalTime endTime)
		{
			this.endTime = endTime;
			return this;
		}

		/**
		 * Sets the reason for maintenance.
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
		 * Sets additional remarks.
		 * 
		 * @param remarks the remarks
		 * @return the builder instance
		 */
		public Builder buildRemarks(String remarks)
		{
			this.remarks = remarks;
			return this;
		}

		/**
		 * Sets the maintenance activity for the report.
		 * 
		 * @param maintenance the maintenance
		 * @return the builder instance
		 */
		public Builder buildMaintenance(Maintenance maintenance)
		{
			this.maintenance = maintenance;
			return this;
		}

		public Report build() throws InformationRequiredExceptionReport
		{
			validateRequiredFields();
			return new Report(this);
		}

		private void validateRequiredFields() throws InformationRequiredExceptionReport
		{
			Map<String, RequiredElement> requiredElements = new HashMap<>();

			if (maintenance == null)
				requiredElements.put("maintenance", RequiredElementReport.MAINTENANCE_REQUIRED);

			if (site == null)
				requiredElements.put("site", RequiredElementReport.SITE_REQUIRED);

			if (technician == null)
				requiredElements.put("technician", RequiredElementReport.TECHNICIAN_REQUIRED);

			if (startDate == null)
				requiredElements.put("startDate", RequiredElementReport.STARTDATE_REQUIRED);

			if (startTime == null)
				requiredElements.put("startTime", RequiredElementReport.STARTTIME_REQUIRED);

			if (endDate == null)
				requiredElements.put("endDate", RequiredElementReport.ENDDATE_REQUIRED);

			if (endTime == null)
				requiredElements.put("endTime", RequiredElementReport.ENDTIME_REQUIRED);

			if (reason == null || reason.isEmpty())
				requiredElements.put("reason", RequiredElementReport.REASON_REQUIRED);

			if (startDate != null && endDate != null)
			{
				if (endDate.isBefore(startDate))
				{
					requiredElements.put("endDate", RequiredElementReport.END_DATE_BEFORE_START);
				} else if (endDate.isEqual(startDate) && startTime != null
						&& endTime != null && endTime.isBefore(startTime))
				{
					requiredElements.put("endTime", RequiredElementReport.END_TIME_BEFORE_START);
				}
			}

			if (!requiredElements.isEmpty())
				throw new InformationRequiredExceptionReport(requiredElements);
		}
	}
}