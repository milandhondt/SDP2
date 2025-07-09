package domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import dto.MaintenanceDTO;
import dto.ReportDTO;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;
import exceptions.InformationRequiredExceptionReport;
import exceptions.InvalidReportException;
import repository.GenericDaoJpa;
import util.DTOMapper;
import util.Role;

/**
 * Controller responsible for managing {@link Report} entities including
 * creation, retrieval by technician or site, and validation of reports.
 */
public class ReportController
{

	private GenericDaoJpa<User> userDao;
	private GenericDaoJpa<Report> reportDao;

	/**
	 * Default constructor initializing DAOs for User, Report, and Site.
	 */
	public ReportController()
	{
		this.userDao = new GenericDaoJpa<>(User.class);
		this.reportDao = new GenericDaoJpa<>(Report.class);
	}

	/**
	 * Constructor used primarily for testing with mock dependencies.
	 *
	 * @param siteDao               the DAO for Site entities
	 * @param userDao               the DAO for User entities
	 * @param reportDao             the DAO for Report entities
	 * @param maintenanceController the MaintenanceController instance
	 */
	public ReportController(GenericDaoJpa<Site> siteDao, GenericDaoJpa<User> userDao, GenericDaoJpa<Report> reportDao,
			MaintenanceController maintenanceController)
	{
		this.userDao = userDao;
		this.reportDao = reportDao;
	}

	/**
	 * Retrieves a list of all users with the role of technician.
	 *
	 * @return a list of users with the {@link Role#TECHNIEKER}
	 */
	public List<User> getTechnicians()
	{
		return userDao.findAll().stream().filter(user -> user.getRole() == Role.TECHNIEKER).toList();
	}

	/**
	 * Creates a new maintenance report and persists it in the database.
	 *
	 * @param site        the site where the maintenance occurred
	 * @param maintenance the related maintenance activity
	 * @param technician  the technician who performed the maintenance
	 * @param startDate   the start date of the maintenance
	 * @param startTime   the start time of the maintenance
	 * @param endDate     the end date of the maintenance
	 * @param endTime     the end time of the maintenance
	 * @param reason      the reason for maintenance
	 * @param remarks     any additional remarks
	 * @return a {@link ReportDTO} representing the saved report
	 * @throws InvalidReportException if the report is invalid or saving fails
	 */
	public ReportDTO createReport(SiteDTOWithoutMachines site, MaintenanceDTO maintenance, UserDTO technician,
			LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, String reason,
			String remarks) throws InformationRequiredExceptionReport
	{
		try
		{
			Site siteObject = DTOMapper.toSite(site);
			Maintenance maintenanceObject = DTOMapper.toMaintenance(maintenance);
			User technicianObject = DTOMapper.toUser(technician);

			Report newReport = new Report.Builder().buildSite(siteObject).buildTechnician(technicianObject)
					.buildMaintenance(maintenanceObject).buildstartDate(startDate).buildStartTime(startTime)
					.buildEndDate(endDate).buildEndTime(endTime).buildReason(reason).buildRemarks(remarks).build();

			validateReport(newReport);

			reportDao.startTransaction();
			reportDao.insert(newReport);
			reportDao.commitTransaction();

			return DTOMapper.toReportDTO(newReport);
		} catch (InvalidReportException e)
		{
			reportDao.rollbackTransaction();
			throw e;
		} catch (Exception e)
		{
			reportDao.rollbackTransaction();
			throw new InvalidReportException("Failed to create report: " + e.getMessage());
		}
	}

	/**
	 * Validates a report by checking for required fields.
	 *
	 * @param report the report to validate
	 * @throws InvalidReportException if any critical fields are missing
	 */
	public void validateReport(Report report) throws InvalidReportException
	{
		if (report == null)
		{
			throw new InvalidReportException("Report cannot be null");
		}
		if (report.getTechnician() == null)
		{
			throw new InvalidReportException("Technician cannot be null");
		}
		if (report.getSite() == null)
		{
			throw new InvalidReportException("Site cannot be null");
		}
	}

	/**
	 * Retrieves all reports created by a specific technician.
	 *
	 * @param technician the technician whose reports are to be retrieved
	 * @return a list of {@link Report} instances created by the given technician
	 * @throws InvalidReportException if the technician is null
	 */
	public List<Report> getReportsByTechnician(User technician)
	{
		validateTechnician(technician);
		var query = GenericDaoJpa.em.createNamedQuery("Report.findByTechnieker", Report.class);
		query.setParameter("technieker", technician);
		return query.getResultList();
	}

	/**
	 * Retrieves all reports for a given site.
	 *
	 * @param site the site whose reports are to be retrieved
	 * @return a list of {@link Report} instances for the specified site
	 * @throws InvalidReportException if the site is null
	 */
	public List<Report> getReportsBySite(Site site)
	{
		validateSite(site);
		var query = GenericDaoJpa.em.createNamedQuery("Report.findBySite", Report.class);
		query.setParameter("site", site);
		return query.getResultList();
	}

	/**
	 * Validates that the given technician is not null.
	 *
	 * @param technician the technician to validate
	 * @throws InvalidReportException if the technician is null
	 */
	private void validateTechnician(User technician)
	{
		if (technician == null)
		{
			throw new InvalidReportException("Technician cannot be null");
		}
	}

	/**
	 * Validates that the given site is not null.
	 *
	 * @param site the site to validate
	 * @throws InvalidReportException if the site is null
	 */
	private void validateSite(Site site)
	{
		if (site == null)
		{
			throw new InvalidReportException("Site cannot be null");
		}
	}
}
