package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.MachineController;
import dto.MachineDTO;
import interfaces.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
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

public class MachinesListComponent extends GridPane implements Observer
{

	private TableView<MachineDTO> machineTable;
	private MachineController machineController;
	private final MainLayout mainLayout;

	private TextField searchField;
	private ComboBox<String> machStatFilter;
	private ComboBox<String> prodStatFilter;

	private List<MachineDTO> allMachines;
	private List<MachineDTO> filteredMachines;

	private int itemsPerPage = 10;
	private int currentPage = 0;
	private int totalPages = 0;
	private Pagination pagination;

	private Button addButton;

	public MachinesListComponent(MainLayout mainLayout)
	{
		this.mainLayout = mainLayout;
		this.machineController = mainLayout.getServices().getMachineController();
		initializeGUI();
		loadMachines();
	}

	private void initializeGUI()
	{
		this.getStylesheets().add(getClass().getResource("/css/tablePane.css").toExternalForm());

		this.getChildren().add(createTitleSection());

		allMachines = machineController.getMachineList();
		filteredMachines = allMachines;

		machineTable = new TableView<>();
		machineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

		buildColumns();

		addButton = new Button(I18n.get("machine.add"));
		addButton.getStyleClass().add("add-button");
		addButton.setOnAction(e -> {
			if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
			{
				openAddMachineForm();
			}
		});

		GridPane.setHalignment(addButton, HPos.RIGHT);
		GridPane.setMargin(addButton, new Insets(0, 0, 10, 0));

		if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
		{
			add(addButton, 0, 0);
		}

		VBox tableWithFilters = buildColumns();
		add(tableWithFilters, 0, 1);
		GridPane.setHgrow(tableWithFilters, Priority.ALWAYS);
		GridPane.setVgrow(tableWithFilters, Priority.ALWAYS);

	}

	private VBox buildColumns()
	{
		machineTable.getColumns().clear();

		HBox filterBox = createTableHeaders();

		TableColumn<MachineDTO, String> idCol = new TableColumn<>(I18n.get("machine.id"));
		idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().id())));

		TableColumn<MachineDTO, String> siteCol = new TableColumn<>(I18n.get("machine.site"));
		siteCol.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().site() != null ? data.getValue().site().siteName() : I18n.get("machine.unknown")));

		TableColumn<MachineDTO, String> technicianCol = new TableColumn<>(I18n.get("machine.technician"));
		technicianCol.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().technician() != null ? data.getValue().technician().firstName() : I18n.get("machine.unknown")));

		TableColumn<MachineDTO, String> productInfoCol = new TableColumn<>(I18n.get("machine.product-info"));
		productInfoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().productInfo()));

		TableColumn<MachineDTO, String> lastMaintenanceCol = new TableColumn<>(I18n.get("machine.last-maintenance"));
		lastMaintenanceCol.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().lastMaintenance() != null ? data.getValue().lastMaintenance().toString() : I18n.get("none")));

		TableColumn<MachineDTO, String> daysSinceMaintenanceCol = new TableColumn<>(I18n.get("machine.days-since-last-maintenance"));
		daysSinceMaintenanceCol.setCellValueFactory(
				data -> new SimpleStringProperty(String.valueOf(data.getValue().numberDaysSinceLastMaintenance())));

		TableColumn<MachineDTO, String> uptimeCol = new TableColumn<>(I18n.get("machine.uptime"));
		uptimeCol.setCellValueFactory(
				data -> new SimpleStringProperty(String.format("%.2f", data.getValue().upTimeInHours())));

		TableColumn<MachineDTO, String> codeCol = new TableColumn<>(I18n.get("machine.code"));
		codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().code()));

		TableColumn<MachineDTO, String> locationCol = new TableColumn<>(I18n.get("machine.location"));
		locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().location()));

		TableColumn<MachineDTO, String> statusCol = new TableColumn<>(I18n.get("machine.machinestatus"));
		statusCol.setCellValueFactory(data -> new SimpleStringProperty(I18n.convertStatus(data.getValue().machineStatus().toString())));

		TableColumn<MachineDTO, String> prodStatusCol = new TableColumn<>(I18n.get("machine.productionstatus"));
		prodStatusCol
				.setCellValueFactory(data -> new SimpleStringProperty(I18n.convertStatus(data.getValue().productionStatus().toString())));

		TableColumn<MachineDTO, String> maintenanceCol = new TableColumn<>(I18n.get("machine.maintenance-planned"));
		maintenanceCol
				.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().futureMaintenance().toString()));

		TableColumn<MachineDTO, Void> editCol = new TableColumn<>(I18n.get("edit"));
		editCol.setCellFactory(param -> {
			TableCell<MachineDTO, Void> cell = new TableCell<MachineDTO, Void>()
			{
				private final Button editButton = new Button();

				{
					FontIcon editIcon = new FontIcon("fas-pen");
					editIcon.setIconSize(20);
					editButton.setGraphic(editIcon);
					editButton.setBackground(Background.EMPTY);
					editButton.setOnAction(event -> {
						MachineDTO selectedMachine = getTableView().getItems().get(getIndex());
						openEditMachineForm(selectedMachine);
					});
				}

				@Override
				public void updateItem(Void item, boolean empty)
				{
					super.updateItem(item, empty);
					if (empty)
					{
						setGraphic(null);
					} else
					{
						setGraphic(editButton);
					}
				}
			};
			return cell;
		});

		TableColumn<MachineDTO, Void> onderhoudCol = new TableColumn<>(I18n.get("machine.maintenances"));
		onderhoudCol.setCellFactory(param -> new TableCell<>()
		{

			private final Button onderhoudButton = new Button();
			{
				FontIcon wrenchIcon = new FontIcon("fas-tools");

				wrenchIcon.setIconSize(20);
				onderhoudButton.setGraphic(wrenchIcon);
				onderhoudButton.setBackground(Background.EMPTY);
				onderhoudButton.setOnAction(event -> {
					MachineDTO selectedMachine = getTableView().getItems().get(getIndex());
					mainLayout.showMaintenanceList(selectedMachine);
				});
				onderhoudButton.setStyle("-fx-background-color: transparent;");
			}

			@Override
			protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
				{
					setGraphic(null);
				} else
				{
					setGraphic(onderhoudButton);
				}
			}
		});

		machineTable.getColumns().addAll(idCol, codeCol, locationCol, statusCol, prodStatusCol, maintenanceCol, siteCol,
				technicianCol, productInfoCol, lastMaintenanceCol, daysSinceMaintenanceCol, uptimeCol);

		machineTable.getColumns().addAll(onderhoudCol);

		if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
		{
			machineTable.getColumns().add(editCol);
		}

		List<MachineDTO> dtos = machineController.getMachineList();
		machineTable.getItems().setAll(dtos);

		HBox paginationControls = new HBox(20);
		pagination = createPagination();

		paginationControls.getChildren().addAll(pagination);
		paginationControls.setAlignment(Pos.CENTER);

		VBox tableWithPagination = new VBox(10, machineTable, paginationControls);
		VBox.setVgrow(machineTable, Priority.ALWAYS);

		return new VBox(10, filterBox, tableWithPagination);
	}

	private Pagination createPagination()
	{
		updateTotalPages();
		Pagination pagination = new Pagination(Math.max(1, totalPages), 0);
		pagination.setPageFactory(this::createPage);
		pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
			currentPage = newIndex.intValue();
			updateTableItems();
		});
		return pagination;
	}

	private HBox createPage(int pageIndex)
	{
		return new HBox();
	}

	private HBox createTableHeaders()
	{
		searchField = new TextField();
		searchField.setPromptText(I18n.get("search"));
		searchField.setPrefWidth(300);
		searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

		machStatFilter = new ComboBox<>();
		machStatFilter.setPromptText(I18n.get("machine.machinestatus"));
		machStatFilter.setPrefWidth(150);
		machStatFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		prodStatFilter = new ComboBox<>();
		prodStatFilter.setPromptText(I18n.get("machine.productionstatus"));
		prodStatFilter.setPrefWidth(150);
		prodStatFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox pageSelector = createPageSelector();

		HBox filterBox = new HBox(10, searchField, machStatFilter, prodStatFilter, spacer, pageSelector);
		filterBox.setAlignment(Pos.CENTER_LEFT);
		return filterBox;
	}

	private HBox createPageSelector()
	{
		Label lblItemsPerPage = new Label(I18n.get("amount-per-page"));

		ComboBox<Integer> comboItemsPerPage = new ComboBox<>(FXCollections.observableArrayList(10, 20, 50, 100));
		comboItemsPerPage.setValue(itemsPerPage);
		comboItemsPerPage.setOnAction(e -> {
			int selectedValue = comboItemsPerPage.getValue();
			updateItemsPerPage(selectedValue);
		});

		HBox pageSelector = new HBox(10, lblItemsPerPage, comboItemsPerPage);
		pageSelector.setAlignment(Pos.CENTER_RIGHT);
		return pageSelector;
	}

	private void updateItemsPerPage(int itemsPerPage)
	{
		this.itemsPerPage = itemsPerPage;
		this.currentPage = 0;
		updatePagination();
		updateTableItems();
	}

	private void filterTable()
	{
		String searchQuery = searchField.getText().toLowerCase();
		String selectedProdStat = prodStatFilter.getValue();
		String selectedMachStat = machStatFilter.getValue();

		filteredMachines = machineController.getFilteredMachines(searchQuery, selectedProdStat, selectedMachStat);

		currentPage = 0;
		updatePagination();
		updateTableItems();
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

		Label title = new Label("Machines");
		title.getStyleClass().add("title-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		hbox.getChildren().addAll(backButton, title, spacer);

		HBox infoBox = new CustomInformationBox(I18n.get("machines.list.infobox"));
		VBox.setMargin(infoBox, new Insets(20, 0, 10, 0));

		return new VBox(10, hbox, infoBox);
	}

	private void openAddMachineForm()
	{
		Parent addMachineForm = new AddOrEditMachineForm(mainLayout);
		mainLayout.setContentAsync(() -> addMachineForm, true, false, CurrentPage.NONE);
	}

	private void openEditMachineForm(MachineDTO machine)
	{
		Parent editMachineForm = new AddOrEditMachineForm(mainLayout, machine.id());
		mainLayout.setContentAsync(() -> editMachineForm, true, false, CurrentPage.NONE);
	}

	private void loadMachines()
	{
		allMachines = machineController.getMachineList();
		filteredMachines = allMachines;
		updateFilterOptions();
		updateTable(allMachines);
	}

	private void updateTable(List<MachineDTO> machines)
	{
		filteredMachines = machines;
		currentPage = 0;
		updatePagination();
		updateTableItems();
	}

	private void updateTableItems()
	{
		int fromIndex = currentPage * itemsPerPage;
		int toIndex = Math.min(fromIndex + itemsPerPage, filteredMachines.size());

		if (filteredMachines.isEmpty())
		{
			machineTable.getItems().clear();
		} else
		{
			List<MachineDTO> currentPageItems = fromIndex < toIndex ? filteredMachines.subList(fromIndex, toIndex)
					: List.of();
			machineTable.getItems().setAll(currentPageItems);
		}
	}

	private void updatePagination()
	{
		updateTotalPages();
		pagination.setPageCount(Math.max(1, totalPages));
		pagination.setCurrentPageIndex(Math.min(currentPage, Math.max(0, totalPages - 1)));
	}

	private void updateTotalPages()
	{
		totalPages = (int) Math.ceil((double) filteredMachines.size() / itemsPerPage);
	}

	private void updateFilterOptions()
	{
		List<String> prodStats = new ArrayList<>();
		prodStats.add(null);
		prodStats.addAll(machineController.getAllProductionStatusses());
		prodStats = prodStats.stream().map((s) -> I18n.convertStatus(s)).collect(Collectors.toList());
		prodStatFilter.setItems(FXCollections.observableArrayList(prodStats));

		List<String> machStats = new ArrayList<>();
		machStats.add(null);
		machStats.addAll(machineController.getAllMachineStatusses());
		machStats = machStats.stream().map((s) -> I18n.convertStatus(s)).collect(Collectors.toList());
		machStatFilter.setItems(FXCollections.observableArrayList(machStats));
	}

	@Override
	public void update(String message)
	{
		Platform.runLater(this::loadMachines);

	}
}
