package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.UserController;
import dto.UserDTO;
import interfaces.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.AuthenticationUtil;
import util.CurrentPage;
import util.I18n;
import util.Role;

public class UserManagementPane extends GridPane implements Observer
{

	private TableView<UserDTO> userTable;
	private Button addButton;

	private TextField searchField;
	private ComboBox<String> statusFilter;
	private ComboBox<String> roleFilter;
	private List<UserDTO> allUsers;
	private List<UserDTO> filteredUsers;

	private final MainLayout mainLayout;

	private UserController uc;

	public UserManagementPane(MainLayout mainLayout)
	{
		this.mainLayout = mainLayout;

		this.uc = AppServices.getInstance().getUserController();

		buildGUI();
		loadUsers();
	}

	private void buildGUI()
	{
		this.getStylesheets().add(getClass().getResource("/css/tablePane.css").toExternalForm());

		allUsers = uc.getAllUsers();
		filteredUsers = allUsers;

		this.getChildren().add(createTitleSection());

		userTable = new TableView<>();
		userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

		HBox filterBox = createFilters();
		GridPane.setMargin(filterBox, new Insets(0, 0, 10, 0));
		buildColumns();

		addButton = new Button(I18n.get("user.add"));
		addButton.getStyleClass().add("add-button");
		addButton.setOnAction(e -> {
			if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
			{
				openAddUserForm();
			} else
			{
				mainLayout.showNotAllowedAlert();
			}
		});

		GridPane.setHalignment(addButton, HPos.RIGHT);
		GridPane.setMargin(addButton, new Insets(0, 0, 10, 0));

		add(addButton, 0, 0);
		add(filterBox, 0, 1);
		add(userTable, 0, 2);

		GridPane.setHgrow(userTable, Priority.ALWAYS);
		GridPane.setVgrow(userTable, Priority.ALWAYS);

		updateFilterOptions();
		updateTable(filteredUsers);
	}

	private HBox createFilters()
	{
		searchField = new TextField();
		searchField.setPromptText(I18n.get("search"));
		searchField.setPrefWidth(300);
		searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

		statusFilter = new ComboBox<>();
		statusFilter.setPromptText(I18n.get("user.statuses"));
		statusFilter.setPrefWidth(150);
		statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		roleFilter = new ComboBox<>();
		roleFilter.setPromptText(I18n.get("user.roles"));
		roleFilter.setPrefWidth(150);
		roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		HBox filterBox = new HBox(10, searchField, statusFilter, roleFilter);
		filterBox.setAlignment(Pos.CENTER_LEFT);
		return filterBox;
	}

	private void filterTable()
	{
		String searchQuery = searchField.getText().toLowerCase();
		String selectedStatus = statusFilter.getValue();
		String selectedRole = roleFilter.getValue();

		filteredUsers = uc.getFilteredUsers(searchQuery, selectedStatus, selectedRole);

		updateTableItems();
	}

	private void updateTable(List<UserDTO> filteredUsers2)
	{
		String searchQuery = searchField.getText().toLowerCase();
		String selectedStatus = statusFilter.getValue();
		String selectedRole = roleFilter.getValue();

		filteredUsers = uc.getFilteredUsers(searchQuery, selectedStatus, selectedRole);

		updateTableItems();
	}

	private void updateTableItems()
	{
		if (filteredUsers.isEmpty())
		{
			userTable.getItems().clear();
		} else
		{
			userTable.getItems().setAll(filteredUsers);
		}
	}

	private void updateFilterOptions()
	{
		List<String> statusses = new ArrayList<>();
		statusses.add(null);
		statusses.addAll(uc.getAllStatusses());
		statusses = statusses.stream().map(I18n::convertStatus).collect(Collectors.toList());
		statusFilter.setItems(FXCollections.observableArrayList(statusses));

		List<String> roles = new ArrayList<>();
		roles.add(null);
		roles.addAll(uc.getAllRoles());
		roles = roles.stream().map(I18n::convertRole).collect(Collectors.toList());
		roleFilter.setItems(FXCollections.observableArrayList(roles));
	}

	private VBox createTitleSection()
	{
		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER_LEFT);

		FontIcon icon = new FontIcon("fas-arrow-left");
		icon.setIconSize(20);
		Button backButton = new Button();
		backButton.setGraphic(icon);
		backButton.getStyleClass().add("back-button");
		backButton.setOnAction(e -> mainLayout.showHomeScreen());
		this.add(backButton, 0, 0, 2, 1);

		Label title = new Label(I18n.get("user.list"));
		title.getStyleClass().add("title-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		hbox.getChildren().addAll(backButton, title, spacer);

		HBox infoBox = new CustomInformationBox(I18n.get("user.list.infobox"));
		VBox.setMargin(infoBox, new Insets(20, 0, 10, 0));

		return new VBox(10, hbox, infoBox);
	}

	private void buildColumns()
	{
		userTable.getColumns().clear();

		TableColumn<UserDTO, Integer> idColumn = new TableColumn<>(I18n.get("user.id"));
		idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().id()).asObject());

		TableColumn<UserDTO, String> firstnameColumn = new TableColumn<>(I18n.get("user.name"));
		firstnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().firstName()));

		TableColumn<UserDTO, String> lastnameColumn = new TableColumn<>(I18n.get("user.lastname"));
		lastnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().lastName()));

		TableColumn<UserDTO, String> emailColumn = new TableColumn<>(I18n.get("user.email"));
		emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().email()));

		TableColumn<UserDTO, String> roleColumn = new TableColumn<>(I18n.get("user.role"));
		roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(I18n.convertRole(cellData.getValue().role().toString())));

		TableColumn<UserDTO, String> statusColumn = new TableColumn<>(I18n.get("user.status"));
		statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(I18n.convertStatus(cellData.getValue().status().toString())));

		TableColumn<UserDTO, Void> editColumn = new TableColumn<>(I18n.get("edit"));
		editColumn.setCellFactory(param -> new TableCell<UserDTO, Void>()
		{
			private final Button editButton = new Button();

			{
				FontIcon editIcon = new FontIcon("fas-pen");
				editIcon.setIconSize(20);
				editButton.setGraphic(editIcon);
				editButton.setBackground(Background.EMPTY);
				editButton.setOnAction(event -> {
					if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
					{
						openEditUserForm(getTableRow().getItem().id());
					} else
					{
						mainLayout.showNotAllowedAlert();
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : editButton);
			}
		});

		userTable.setPlaceholder(new Label(I18n.get("no-users-found")));

		userTable.getColumns().add(idColumn);
		userTable.getColumns().add(firstnameColumn);
		userTable.getColumns().add(lastnameColumn);
		userTable.getColumns().add(emailColumn);
		userTable.getColumns().add(roleColumn);
		userTable.getColumns().add(statusColumn);

		userTable.getColumns().add(editColumn);

	}

	private void loadUsers()
	{
		userTable.getItems().setAll(uc.getAllUsers());
	}

	private void openAddUserForm()
	{
		Parent addUserForm = new AddOrEditUserForm(mainLayout);
		mainLayout.setContentAsync(() -> addUserForm, true, false, CurrentPage.NONE);
	}

	private void openEditUserForm(int userId)
	{
		Parent editUserForm = new AddOrEditUserForm(mainLayout, userId);
		mainLayout.setContentAsync(() -> editUserForm, true, false, CurrentPage.NONE);
	}

	@Override
	public void update(String message)
	{
		Platform.runLater(this::loadUsers);
	}

}
