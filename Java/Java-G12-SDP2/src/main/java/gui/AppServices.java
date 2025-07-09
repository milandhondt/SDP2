package gui;

import domain.FileInfoController;
import domain.KPIController;
import domain.KPIWaardeController;
import domain.MachineController;
import domain.MaintenanceController;
import domain.NotificationController;
import domain.ReportController;
import domain.SiteController;
import domain.UserController;
import lombok.Getter;

@Getter
public class AppServices
{
	private final UserController userController;
	private final SiteController siteController;
	private final MachineController machineController;
	private final MaintenanceController maintenanceController;
	private final FileInfoController fileInfoController;
	private final ReportController reportController;
	private final NotificationController notificationController;
	private final KPIController kpiController;
	private final KPIWaardeController kpiWaardeController;

	private AppServices()
	{
		this.siteController = new SiteController();
		this.machineController = new MachineController();
		this.maintenanceController = new MaintenanceController();
		this.fileInfoController = new FileInfoController();
		this.reportController = new ReportController();
		this.userController = new UserController();
		this.notificationController = new NotificationController();
		this.kpiController = new KPIController();
		this.kpiWaardeController = new KPIWaardeController();

	}

	public static class SingletonHolder
	{
		private static final AppServices instance = new AppServices();
	}

	public static AppServices getInstance()
	{
		return SingletonHolder.instance;
	}
}