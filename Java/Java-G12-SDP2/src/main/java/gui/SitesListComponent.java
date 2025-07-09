package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.SiteController;
import dto.SiteDTOWithMachines;
import interfaces.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.AuthenticationUtil;
import util.CurrentPage;
import util.I18n;
import util.Role;

public class SitesListComponent extends VBox implements Observer
{
	private final MainLayout mainLayout;
	private SiteController sc;

	private TableView<SiteDTOWithMachines> table;
	private TextField searchField;

	private ComboBox<String> statusFilter;
	private ComboBox<String> nameFilter;
	private ComboBox<String> verantwoordelijkeFilter;
	private TextField minMachinesField;
	private TextField maxMachinesField;
	private List<SiteDTOWithMachines> allSites;
	private List<SiteDTOWithMachines> filteredSites;

	private int itemsPerPage = 10;
	private int currentPage = 0;
	private int totalPages = 0;
	private Pagination pagination;

	public SitesListComponent(MainLayout mainLayout)
	{
		this.mainLayout = mainLayout;
		this.sc = mainLayout.getServices().getSiteController();
		this.table = new TableView<>();
		initializeGUI();
		loadSites();
	}

	private void loadSites()
	{
		allSites = sc.getSites();
		filteredSites = allSites;
		updateFilterOptions();
		updateTable(allSites);
	}

	private void initializeGUI()
	{
		this.getStylesheets().add(getClass().getResource("/css/tablePane.css").toExternalForm());

		allSites = sc.getSites();
		filteredSites = allSites;

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

		VBox titleSection = createTitleSection();
		VBox tableSection = createTableSection();

		this.setSpacing(20);
		this.getChildren().addAll(titleSection, tableSection);

		updateFilterOptions();
		updateTable(filteredSites);
		configureTableLayout();
	}

	private void configureTableLayout()
	{
		table.setMinHeight(300);
		table.setPrefHeight(500);
		table.setMaxHeight(Double.MAX_VALUE);

		VBox.setVgrow(table, Priority.ALWAYS);
	}

	private VBox createTitleSection()
	{
		HBox windowHeader = createWindowHeader();
		HBox informationBox = new CustomInformationBox(
				I18n.get("site.list.infobox"));
		return new VBox(10, windowHeader, informationBox);
	}

	private HBox createWindowHeader()
	{
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setSpacing(10);

		Button backButton = new Button();
		FontIcon icon = new FontIcon("fas-arrow-left");
		icon.setIconSize(20);
		backButton.setGraphic(icon);
		backButton.getStyleClass().add("back-button");
		backButton.setOnAction(e -> mainLayout.showHomeScreen());

		Label title = new Label(I18n.get("sites"));
		title.getStyleClass().add("title-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		hbox.getChildren().addAll(backButton, title);

		if (AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) || AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			Button addButton = new Button(I18n.get("site.add"));
			addButton.setOnAction(e -> openAddSiteForm());
			addButton.getStyleClass().add("add-button");

			hbox.getChildren().addAll(spacer, addButton);
		}

		return hbox;
	}

	private VBox createTableSection()
	{
		HBox filterBox = createTableHeaders();

		TableColumn<SiteDTOWithMachines, Void> editColumn = new TableColumn<>(I18n.get("edit"));
		editColumn.setCellFactory(param -> new TableCell<SiteDTOWithMachines, Void>()
		{
			private final Button editButton = new Button();
			{
				FontIcon editIcon = new FontIcon("fas-pen");
				editIcon.setIconSize(12);
				editButton.setGraphic(editIcon);
				editButton.setBackground(Background.EMPTY);
				editButton.setOnAction(event ->
				{
					SiteDTOWithMachines site = getTableRow().getItem();
					if (site != null)
					{
						openEditSiteForm(site.id());
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

		TableColumn<SiteDTOWithMachines, Number> col1 = new TableColumn<>(I18n.get("number"));
		col1.setMaxWidth(70);
		col1.setMinWidth(70);
		col1.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));

		TableColumn<SiteDTOWithMachines, String> col2 = new TableColumn<>(I18n.get("site.name"));
		col2.setPrefWidth(200);
		col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));

		TableColumn<SiteDTOWithMachines, String> col3 = new TableColumn<>(I18n.get("site.manager"));
		col3.setPrefWidth(200);
		col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().verantwoordelijke().firstName()));

		TableColumn<SiteDTOWithMachines, String> col4 = new TableColumn<>(I18n.get("site.status"));
		col4.setPrefWidth(100);
		col4.setCellValueFactory(data -> new SimpleStringProperty(I18n.convertStatus(data.getValue().status().toString())));

		TableColumn<SiteDTOWithMachines, Number> col5 = new TableColumn<>(I18n.get("site.machine-count"));
		col5.setPrefWidth(150);
		col5.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().machines().size()));

		TableColumn<SiteDTOWithMachines, String> showColumn = new TableColumn<>(I18n.get("details"));
		showColumn.setMaxWidth(100);
		showColumn.setMinWidth(100);
		showColumn.setCellFactory(param -> new TableCell<SiteDTOWithMachines, String>()
		{
			private final Button viewButton = new Button(I18n.get("details"));
			{
				viewButton.setOnAction(event ->
				{
					SiteDTOWithMachines site = getTableRow().getItem();
					if (site != null)
					{
						openSiteDetails(site.id());
					}
				});
			}

			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : viewButton);
			}
		});

		table.getColumns().addAll(col1, col2, col3, col4, col5);
		if (AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) || AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			table.getColumns().add(editColumn);
		}
		table.getColumns().add(showColumn);
		table.setPrefHeight(300);

		HBox paginationControls = new HBox(20);
		pagination = createPagination();

		paginationControls.getChildren().addAll(pagination);
		paginationControls.setAlignment(Pos.CENTER);

		VBox tableWithPagination = new VBox(10, table, paginationControls);
		VBox.setVgrow(table, Priority.ALWAYS);

		return new VBox(10, filterBox, tableWithPagination);
	}

	private HBox createTableHeaders()
	{
		searchField = new TextField();
		searchField.setPromptText(I18n.get("search"));
		searchField.setPrefWidth(300);
		searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

		statusFilter = new ComboBox<>();
		statusFilter.setPromptText(I18n.get("site.statuses"));
		statusFilter.setPrefWidth(150);
		statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		nameFilter = new ComboBox<>();
		nameFilter.setPromptText(I18n.get("site-name"));
		nameFilter.setPrefWidth(150);
		nameFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		verantwoordelijkeFilter = new ComboBox<>();
		verantwoordelijkeFilter.setPromptText(I18n.get("site.manager"));
		verantwoordelijkeFilter.setPrefWidth(200);
		verantwoordelijkeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		minMachinesField = new TextField();
		minMachinesField.setPromptText(I18n.get("site.min-machines"));
		minMachinesField.setMaxWidth(100);
		minMachinesField.textProperty().addListener((obs, oldVal, newVal) ->
		{
			if (!newVal.matches("\\d*"))
			{
				minMachinesField.setText(newVal.replaceAll("[^\\d]", ""));
			}
			filterTable();
		});

		maxMachinesField = new TextField();
		maxMachinesField.setPromptText(I18n.get("site.max-machines"));
		maxMachinesField.setMaxWidth(100);
		maxMachinesField.textProperty().addListener((obs, oldVal, newVal) ->
		{
			if (!newVal.matches("\\d*"))
			{
				maxMachinesField.setText(newVal.replaceAll("[^\\d]", ""));
			}
			filterTable();
		});

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox pageSelector = createPageSelector();

		HBox filterBox = new HBox(10, searchField, statusFilter, nameFilter, verantwoordelijkeFilter, minMachinesField,
				maxMachinesField, spacer, pageSelector);
		filterBox.setAlignment(Pos.CENTER_LEFT);
		return filterBox;
	}

	private HBox createPageSelector()
	{
		Label lblItemsPerPage = new Label(I18n.get("amount-per-page"));

		ComboBox<Integer> comboItemsPerPage = new ComboBox<>(FXCollections.observableArrayList(10, 20, 50, 100));
		comboItemsPerPage.setValue(itemsPerPage);
		comboItemsPerPage.setOnAction(e ->
		{
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

	private void updateFilterOptions()
	{
		List<String> statussen = new ArrayList<>();
		statussen.addAll(sc.getAllStatusses());
		statussen = statussen.stream().map((s) -> I18n.convertStatus(s)).collect(Collectors.toList());
		statusFilter.setItems(FXCollections.observableArrayList(statussen));

		List<String> siteNames = new ArrayList<>();
		siteNames.add(null);
		siteNames.addAll(sc.getAllSiteNames());
		nameFilter.setItems(FXCollections.observableArrayList(siteNames));

		List<String> verantwoordelijken = new ArrayList<>();
		verantwoordelijken.add(null);
		verantwoordelijken.addAll(sc.getAllVerantwoordelijken());
		verantwoordelijkeFilter.setItems(FXCollections.observableArrayList(verantwoordelijken));
	}

	private void filterTable()
	{
		String searchQuery = searchField.getText().toLowerCase();
		String selectedStatus = statusFilter.getValue();
		String selectedName = nameFilter.getValue();
		String selectedVerantwoordelijke = verantwoordelijkeFilter.getValue();

		int minMachines = parseIntSafely(minMachinesField.getText(), Integer.MIN_VALUE);
		int maxMachines = parseIntSafely(maxMachinesField.getText(), Integer.MAX_VALUE);
		filteredSites = sc.getFilteredSites(searchQuery, selectedStatus, selectedName, selectedVerantwoordelijke,
				minMachines, maxMachines);

		currentPage = 0;
		updatePagination();
		updateTableItems();
	}

	private int parseIntSafely(String value, int defaultValue)
	{
		if (value == null || value.trim().isEmpty())
		{
			return defaultValue;
		}
		try
		{
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	private Pagination createPagination()
	{
		updateTotalPages();
		Pagination pagination = new Pagination(Math.max(1, totalPages), 0);
		pagination.setPageFactory(this::createPage);
		pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
		{
			currentPage = newIndex.intValue();
			updateTableItems();
		});
		return pagination;
	}

	private HBox createPage(int pageIndex)
	{
		return new HBox();
	}

	private void updatePagination()
	{
		updateTotalPages();
		pagination.setPageCount(Math.max(1, totalPages));
		pagination.setCurrentPageIndex(Math.min(currentPage, Math.max(0, totalPages - 1)));
	}

	private void updateTotalPages()
	{
		totalPages = (int) Math.ceil((double) filteredSites.size() / itemsPerPage);
	}

	private void updateTableItems()
	{
		int fromIndex = currentPage * itemsPerPage;
		int toIndex = Math.min(fromIndex + itemsPerPage, filteredSites.size());

		if (filteredSites.isEmpty())
		{
			table.getItems().clear();
		} else
		{
			List<SiteDTOWithMachines> currentPageItems = fromIndex < toIndex ? filteredSites.subList(fromIndex, toIndex)
					: List.of();
			table.getItems().setAll(currentPageItems);
		}
	}

	private void updateTable(List<SiteDTOWithMachines> sites)
	{
		filteredSites = sites;
		currentPage = 0;
		updatePagination();
		updateTableItems();
	}

	private void openAddSiteForm()
	{
		Parent addSiteForm = new AddOrEditSiteForm(mainLayout);
		mainLayout.setContentAsync(() -> addSiteForm, true, false, CurrentPage.NONE);
	}

	private void openEditSiteForm(int siteId)
	{
		Parent editSiteForm = new AddOrEditSiteForm(mainLayout, siteId);
		mainLayout.setContentAsync(() -> editSiteForm, true, false, CurrentPage.NONE);
	}

	private void openSiteDetails(int siteId)
	{
		Parent siteDetails = new SiteDetailsComponent(mainLayout, siteId);
		mainLayout.setContentAsync(() -> siteDetails, true, false, CurrentPage.NONE);
	}

	@Override
	public void update(String message)
	{
		Platform.runLater(() ->
		{
			allSites = sc.getSites();
			filteredSites = new ArrayList<>(allSites);
			updateFilterOptions();
			updateTable(filteredSites);
		});
	}
}