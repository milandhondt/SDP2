package gui;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.NotificationController;
import domain.User;
import dto.NotificationDTO;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.AuthenticationUtil;
import util.CurrentPage;
import util.I18n;
import util.Role;

public class Navbar extends HBox
{

	private Label userName;
	private Label userRole;
	private final NotificationController notificationController;
	private final MainLayout mainLayout;

	public Navbar(MainLayout mainLayout, boolean isHomeScreen, CurrentPage activePage)
	{
		this.mainLayout = mainLayout;
		this.notificationController = mainLayout.getServices().getNotificationController();

		this.getStyleClass().add("navbar");
		this.setAlignment(Pos.CENTER);

		ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/delaware_navbaricon.png")));
		logo.setFitHeight(40);
		logo.setPreserveRatio(true);
		Button logoBtn = new Button();
		logoBtn.setGraphic(logo);
		logoBtn.getStyleClass().add("logo-btn");
		logoBtn.setOnAction(e -> mainLayout.showHomeScreen());

		this.userName = new Label();
		this.userRole = new Label();
		userRole.getStyleClass().add("role-label");
		fillUserData();
		VBox userInfo = new VBox(2, userName, userRole);
		userInfo.getStyleClass().add("user-info");

		Button notificationBtn = new Button();
		FontIcon bellIcon = new FontIcon("fas-bell");
		bellIcon.setIconSize(20);
		notificationBtn.setGraphic(bellIcon);
		notificationBtn.getStyleClass().add("icon-btn");

		ContextMenu notificationMenu = new ContextMenu();
		notificationMenu.setMinSize(500, 500);

		notificationBtn.setOnAction(e -> {
			if (!notificationMenu.isShowing())
			{
				notificationMenu.getItems().clear();

				List<NotificationDTO> unread = notificationController.getAllUnread();

				if (unread.isEmpty())
				{
					MenuItem emptyItem = new MenuItem(I18n.get("notifications.none"));
					emptyItem.setDisable(true);
					notificationMenu.getItems().add(emptyItem);
				} else
				{
					for (NotificationDTO dto : unread)
					{
						MenuItem item = new MenuItem(dto.message());
						item.getStyleClass().add("menu-item");

						item.setOnAction(ev -> {
							notificationController.markAsRead(dto.id());
							mainLayout.showNotificationDetails(dto);
							notificationMenu.hide();
						});

						notificationMenu.getItems().add(item);
					}
				}

				MenuItem seeAll = new MenuItem(I18n.get("notifications.all"));
				seeAll.setStyle("-fx-font-weight: bold;");
				seeAll.setOnAction(ev -> mainLayout.showNotificationList());
				notificationMenu.getItems().add(new SeparatorMenuItem());
				notificationMenu.getItems().add(seeAll);

				notificationMenu.show(notificationBtn, Side.BOTTOM, 0, 0);
			} else
			{
				notificationMenu.hide();
			}
		});

		Button logoutBtn = new Button(I18n.get("logout.button"));
		logoutBtn.getStyleClass().add("logout-btn");
		logoutBtn.setOnAction(e -> mainLayout.showLoginScreen());
		
		FontIcon settingsIcon = new FontIcon("fas-cog");
		settingsIcon.setIconSize(20);
		Button preferencesBtn = new Button();
		preferencesBtn.setGraphic(settingsIcon);
		preferencesBtn.getStyleClass().add("icon-btn");
		preferencesBtn.setOnAction((e) -> mainLayout.showPreferences());

		HBox navLinks = new HBox(20);
		navLinks.getStyleClass().add("nav-links-container");
		navLinks.setAlignment(Pos.CENTER);

		if (!isHomeScreen)
		{
			Button maintenanceBtn = createNavButton(I18n.get("maintenance"), CurrentPage.MAINTENANCE, activePage,
					e -> mainLayout.showMaintenanceList());
			Button sitesBtn = createNavButton(I18n.get("sites"), CurrentPage.SITES, activePage, e -> mainLayout.showSitesList());
			Button machinesBtn = createNavButton(I18n.get("machines"), CurrentPage.MACHINES, activePage,
					e -> mainLayout.showMachineScreen());

			navLinks.getChildren().addAll(maintenanceBtn, sitesBtn, machinesBtn);

			if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
			{
				Button userBtn = createNavButton(I18n.get("users"), CurrentPage.USERS, activePage,
						e -> mainLayout.showUserManagementScreen());
				navLinks.getChildren().add(userBtn);
			}

		}

		Region leftSpacer = new Region();
		Region rightSpacer = new Region();
		HBox.setHgrow(leftSpacer, Priority.ALWAYS);
		HBox.setHgrow(rightSpacer, Priority.ALWAYS);

		HBox rightElements = new HBox(15, userInfo, notificationBtn, logoutBtn, preferencesBtn);
		rightElements.setAlignment(Pos.CENTER_RIGHT);

		this.getChildren().addAll(logoBtn, leftSpacer, navLinks, rightSpacer, rightElements);
	}

	private Button createNavButton(String text, CurrentPage page, CurrentPage activePage,
			EventHandler<ActionEvent> handler)
	{
		Button button = new Button(text);
		button.getStyleClass().add("nav-link");
		if (page == activePage)
		{
			button.getStyleClass().add("active");
		}
		button.setOnAction(handler);
		return button;
	}

	private void fillUserData()
	{
		User user = AuthenticationUtil.getAuthenticatedUser();
		userName.setText(user.getFullName());
		String roleFormatted = capitalize(user.getRole().toString().toLowerCase());
		userRole.setText(roleFormatted);
	}

	private String capitalize(String text)
	{
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

}
