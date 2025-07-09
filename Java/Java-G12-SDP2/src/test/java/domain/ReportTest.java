package domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.Maintenance;
import domain.Report;
import domain.ReportController;
import domain.Site;
import domain.User;
import exceptions.InformationRequiredExceptionReport;

class ReportTest
{

	private ReportController reportController;
	private Site mockSite;
	private Maintenance mockMaintenance;
	private User mockTechnician;

	@BeforeEach
	void setUp()
	{
		reportController = new ReportController();
		mockSite = mock(Site.class);
		mockMaintenance = mock(Maintenance.class);
		mockTechnician = mock(User.class);
	}

	@Test
	void buildReport_WithAllValidFields_ShouldCreateReportSuccessfully() throws InformationRequiredExceptionReport
	{
		Report report = new Report.Builder().buildSite(mockSite).buildMaintenance(mockMaintenance)
				.buildTechnician(mockTechnician).buildstartDate(LocalDate.of(2024, 5, 1))
				.buildStartTime(LocalTime.of(10, 0)).buildEndDate(LocalDate.of(2024, 5, 1))
				.buildEndTime(LocalTime.of(12, 0)).buildReason("Routine check").buildRemarks("Everything was okay.")
				.build();

		assertNotNull(report);
		assertEquals(mockTechnician, report.getTechnician());
	}

	@Test
	void buildReport_WithNullTechnician_ShouldThrowInformationRequiredExceptionReport()
	{
		InformationRequiredExceptionReport exception = assertThrows(InformationRequiredExceptionReport.class, () -> {
			new Report.Builder().buildSite(mockSite).buildMaintenance(mockMaintenance).buildTechnician(null)
					.buildstartDate(LocalDate.of(2024, 5, 1)).buildStartTime(LocalTime.of(10, 0))
					.buildEndDate(LocalDate.of(2024, 5, 1)).buildEndTime(LocalTime.of(12, 0))
					.buildReason("Routine check").buildRemarks("Everything was okay.").build();
		});

	}

	@Test
	void validateReport_WithValidReport_ShouldPassValidation() throws InformationRequiredExceptionReport
	{
		Report report = new Report.Builder().buildSite(mockSite).buildMaintenance(mockMaintenance)
				.buildTechnician(mockTechnician).buildstartDate(LocalDate.of(2024, 5, 1))
				.buildStartTime(LocalTime.of(10, 0)).buildEndDate(LocalDate.of(2024, 5, 1))
				.buildEndTime(LocalTime.of(12, 0)).buildReason("Routine check").buildRemarks("All good.").build();

		assertDoesNotThrow(() -> reportController.validateReport(report));
	}
}
