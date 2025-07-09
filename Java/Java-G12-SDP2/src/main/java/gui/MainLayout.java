package gui;

import java.util.function.Supplier;

import dto.MachineDTO;
import dto.MaintenanceDTO;
import dto.NotificationDTO;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import util.AuthenticationUtil;
import util.CurrentPage;
import util.I18n;
import util.Role;

public class MainLayout
{
	private final BorderPane rootLayout;
	private final Stage primaryStage;
	private final StackPane sceneWrapper;
	private final LoadingPane loadingPane = new LoadingPane();
	@Getter
	private final Scene mainScene;
	@Getter
	private final AppServices services;

	private static final Insets CONTENT_PADDING = new Insets(50, 80, 0, 80);
	private static final int CONTENT_SPACING = 20;

	public MainLayout(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
		this.services = AppServices.getInstance();

		this.rootLayout = new BorderPane();
		applyRootStyles();

		this.sceneWrapper = new StackPane(rootLayout);

		this.mainScene = new Scene(sceneWrapper);
		applySceneStyles();

		primaryStage.setScene(mainScene);
		primaryStage.setMaximized(true);

		showLoginScreen();
	}

	private void applyRootStyles()
	{
		BackgroundImage backgroundImage = new BackgroundImage(
				new Image(getClass().getResourceAsStream("/images/background.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(100, 100, true, true, true, true));
		rootLayout.setBackground(new Background(backgroundImage));
	}

	private void applySceneStyles()
	{
		mainScene.getStylesheets().add(getClass().getResource("/css/navbar.css").toExternalForm());
		mainScene.getStylesheets().add(getClass().getResource("/css/KPITile.css").toExternalForm());
	}

	public void showLoadingOverlay()
	{
		if (!sceneWrapper.getChildren().contains(loadingPane))
		{
			sceneWrapper.getChildren().add(loadingPane);
		}
	}

	public void hideLoadingOverlay()
	{
		sceneWrapper.getChildren().remove(loadingPane);
	}

	public void showLoginScreen()
	{
		LoginPane loginPane = new LoginPane(this);
		setContentAsync(() -> loginPane, false, false, CurrentPage.NONE);
	}

	public void showHomeScreen()
	{
		HomeScreen choicePane = new HomeScreen(this);
		setContentAsync(() -> choicePane, true, false, CurrentPage.HOME);
	}
	
	public void showPreferences()
	{
		PreferenceScreen preferenceScreen = new PreferenceScreen(this);
		setContentAsync(() -> preferenceScreen, true, false, CurrentPage.PREFERENCE);
	}

	public void showUserManagementScreen()
	{
		if (!AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			showNotAllowedAlert();
		} else
		{
			UserManagementPane userManagement = new UserManagementPane(this);
			setContentAsync(() -> userManagement, true, false, CurrentPage.USERS);
		}
	}

	public void showSitesList()
	{
		SitesListComponent sitesListComponent = new SitesListComponent(this);
		setContentAsync(() -> sitesListComponent, true, false, CurrentPage.SITES);
	}

	public void showSiteDetails(int siteId)
	{
		SiteDetailsComponent siteDetailsComponent = new SiteDetailsComponent(this, siteId);
		setContentAsync(() -> siteDetailsComponent, true, false, CurrentPage.NONE);
	}

	public void showMachineScreen()
	{
		MachinesListComponent machineList = new MachinesListComponent(this);
		setContentAsync(() -> machineList, true, false, CurrentPage.MACHINES);
	}

	public void showMaintenanceList()
	{
		MaintenanceListComponent maintenanceList = new MaintenanceListComponent(this);
		setContentAsync(() -> maintenanceList, true, false, CurrentPage.MAINTENANCE);
	}

	public void showMaintenanceList(MachineDTO machine)
	{
		MaintenanceListComponent maintenanceList = new MaintenanceListComponent(this, machine);
		setContentAsync(() -> maintenanceList, true, false, CurrentPage.MAINTENANCE);
	}

	public void showMaintenanceDetails(MaintenanceDTO maintenance)
	{
		if (!AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) && !AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			showNotAllowedAlert();
		} else
		{
			MaintenanceDetailView detailView = new MaintenanceDetailView(this, maintenance);
			setContentAsync(() -> detailView, true, false, CurrentPage.NONE);
		}
	}

	public void showNotificationDetails(NotificationDTO notification)
	{
		if (!AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) && !AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			showNotAllowedAlert();
		} else
		{
			NotificationDetailComponent detail = new NotificationDetailComponent(this, notification);
			setContentAsync(() -> detail, true, false, CurrentPage.NONE);
		}
	}

	public void showAddReport(MaintenanceDTO maintenance)
	{
		if (!AuthenticationUtil.hasRole(Role.TECHNIEKER) && !AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			showNotAllowedAlert();
		} else
		{
			AddReportForm addReport = new AddReportForm(this, maintenance);
			setContentAsync(() -> addReport, true, false, CurrentPage.NONE);
		}
	}

	public void showNotificationList()
	{
		NotificationListComponent notificationList = new NotificationListComponent(this);
		setContentAsync(() -> notificationList, true, false, CurrentPage.NONE);
	}

	public void showMaintenancePlanning(MachineDTO machineDTO)
	{
		if (!AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) && !AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			showNotAllowedAlert();
		} else
		{
			AddOrEditMaintenance maintenancePlanningForm = new AddOrEditMaintenance(this, machineDTO);
			setContentAsync(() -> maintenancePlanningForm, true, false, CurrentPage.NONE);
		}
	}

	public void showEditMaintenance(MaintenanceDTO maintenanceDTO, MachineDTO machineDTO)
	{
		if (!AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) && !AuthenticationUtil.hasRole(Role.ADMINISTRATOR)
				&& !AuthenticationUtil.hasRole(Role.TECHNIEKER))
		{
			showNotAllowedAlert();
		} else
		{
			AddOrEditMaintenance maintenancePlanningForm = new AddOrEditMaintenance(this, maintenanceDTO, machineDTO);
			setContentAsync(() -> maintenancePlanningForm, true, false, CurrentPage.NONE);
		}
	}

	public void setContentAsync(Supplier<Parent> contentSupplier, boolean showNavbar, boolean isHomeScreen,
			CurrentPage activePage)
	{
		showLoadingOverlay();

		Task<Parent> loadTask = new Task<>()
		{
			@Override
			protected Parent call()
			{
				return contentSupplier.get();
			}
		};

		loadTask.setOnSucceeded(e -> {
			Parent content = loadTask.getValue();

			VBox contentWrapper = new VBox();
			if (showNavbar)
			{
				Navbar navBar = new Navbar(this, isHomeScreen, activePage);
				contentWrapper.getChildren().add(navBar);
			}

			if (content != null)
			{
				VBox paddedContent = new VBox(content);
				paddedContent.setPadding(CONTENT_PADDING);
				paddedContent.setSpacing(CONTENT_SPACING);
				contentWrapper.getChildren().add(paddedContent);
			}

			rootLayout.setCenter(contentWrapper);
			hideLoadingOverlay();
		});

		loadTask.setOnFailed(e -> {
			hideLoadingOverlay();
			showErrorAlert(I18n.get("error.page-load"));
		});

		new Thread(loadTask).start();
	}

	public void showErrorAlert(String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(I18n.get("error-alert-title"));
		alert.setHeaderText(I18n.get("error"));
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void showNotAllowedAlert()
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(I18n.get("no-access"));
		alert.setHeaderText(I18n.get("no-access-header-message"));
		alert.setContentText(
				I18n.get("no-access-contact"));
		alert.showAndWait();
	}
}