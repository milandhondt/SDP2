package gui;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.MaintenanceController;
import dto.MachineDTO;
import dto.MaintenanceDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import util.I18n;
import util.MaintenanceStatus;
import util.Role;

public class MaintenanceListComponent extends VBox
{
	private final MainLayout mainLayout;
	private MaintenanceController mc;
	private TableView<MaintenanceDTO> table;

	private TextField searchField;
	private DatePicker executionDatePickerFilter;
	private ComboBox<LocalTime> startTimeFieldFilter, endTimeFieldFilter;
	private ComboBox<String> technicianFilter;
	private TextField reasonFilter;
	private TextField commentsFilter;
	private ComboBox<String> statusFilter;

	private List<MaintenanceDTO> allMaintenances;
	private List<MaintenanceDTO> filteredMaintenances;
	private MachineDTO machineDTO;

	private int itemsPerPage = 10;
	private int currentPage = 0;
	private int totalPages = 0;
	private Pagination pagination;

	public MaintenanceListComponent(MainLayout mainLayout)
	{
		this.mainLayout = mainLayout;
		this.mc = mainLayout.getServices().getMaintenanceController();
		this.table = new TableView<>();
		initializeGUI();
	}

	public MaintenanceListComponent(MainLayout mainLayout, MachineDTO machineDTO)
	{
		this.mc = mainLayout.getServices().getMaintenanceController();
		this.machineDTO = machineDTO;
		this.mainLayout = mainLayout;
		this.table = new TableView<>();
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.getStylesheets().add(getClass().getResource("/css/tablePane.css").toExternalForm());

		allMaintenances = machineDTO == null ? mc.getMaintenances()
				: mc.getMaintenances().stream().filter((m) -> m.machine().equals(machineDTO)).toList();
		filteredMaintenances = allMaintenances;

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

		VBox titleSection = createTitleSection();
		VBox tableSection = createTableSection();

		this.setSpacing(20);

		this.getChildren().addAll(titleSection, tableSection);
		updateTable(filteredMaintenances);
	}

	private VBox createTitleSection()
	{
		HBox header = createWindowHeader();
		HBox infoBox = new CustomInformationBox(I18n.get("maintenance.list.infobox"));
		return machineDTO == null ? new VBox(10, header, infoBox)
				: new VBox(10, header, new CustomInformationBox(
						MessageFormat.format(I18n.get("maintenance.list.infobox.machine"), machineDTO.id())));
	}

	private HBox createWindowHeader()
	{
		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setSpacing(10);

		FontIcon icon = new FontIcon("fas-arrow-left");
		icon.setIconSize(20);
		Button backButton = new Button();
		backButton.setGraphic(icon);
		backButton.getStyleClass().add("back-button");
		backButton.setOnAction(e -> mainLayout.showHomeScreen());

		Label title = new Label(I18n.get("maintenance.overview"));
		title.getStyleClass().add("title-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		hbox.getChildren().addAll(backButton, title);
		if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
		{
			CustomButton maintenancePlanBtn = new CustomButton(I18n.get("maintenance.plan"));
			maintenancePlanBtn.setOnAction((e) ->
			{
				if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR)
						|| AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
				{
					mainLayout.showMaintenancePlanning(machineDTO);
				} else
				{
					mainLayout.showNotAllowedAlert();
				}
			});
			maintenancePlanBtn.getStyleClass().add("add-button");

			hbox.getChildren().addAll(spacer, maintenancePlanBtn);
		}

		return hbox;
	}

	private VBox createTableSection()
	{
		HBox filterBox = createTableHeaders();

		TableColumn<MaintenanceDTO, Void> editColumn = new TableColumn<>(I18n.get("edit"));
		editColumn.setCellFactory(param -> new TableCell<MaintenanceDTO, Void>()
		{
			private final Button editButton = new Button();
			{
				FontIcon editIcon = new FontIcon("fas-pen");
				editIcon.setIconSize(12);
				editButton.setGraphic(editIcon);
				editButton.setBackground(Background.EMPTY);
				editButton.setOnAction(event ->
				{
					MaintenanceDTO maintenance = getTableRow().getItem();
					if (maintenance != null)
					{
						goToEditMaintenanceForm(maintenance);
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

		TableColumn<MaintenanceDTO, String> col1 = createColumn(I18n.get("maintenance.executiondate"), m -> m.executionDate().toString());
		TableColumn<MaintenanceDTO, String> col2 = createColumn(I18n.get("maintenance.starttime"),
				m -> m.startDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")).toString());
		TableColumn<MaintenanceDTO, String> col3 = createColumn(I18n.get("maintenance.endtime"),
				m -> m.endDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")).toString());
		TableColumn<MaintenanceDTO, String> col4 = createColumn(I18n.get("maintenance.technician-name"), m ->
		{
			if (m.technician() == null)
				return I18n.get("maintenance.technician.unknown");
			String first = m.technician().firstName();
			String last = m.technician().lastName();
			String formattedLast = last.substring(0, 1).toUpperCase() + last.substring(1).toLowerCase();
			return String.format("%s %s", first, formattedLast);
		});
		TableColumn<MaintenanceDTO, String> col5 = createColumn(I18n.get("maintenance.reason"), MaintenanceDTO::reason);
		TableColumn<MaintenanceDTO, String> col6 = createColumn(I18n.get("maintenance.comments"), MaintenanceDTO::comments);
		TableColumn<MaintenanceDTO, String> col7 = createColumn(I18n.get("maintenance.status"),
			    m -> I18n.convertStatus(m.status().toString()));
		TableColumn<MaintenanceDTO, String> col8 = createColumn(I18n.get("machine"),
				m -> String.format("%s %d", I18n.get("machine"), m.machine().id()));

		List<TableColumn<MaintenanceDTO, ?>> columns;
		if (machineDTO != null)
		{
			columns = new ArrayList<>(List.of(col1, col2, col3, col4, col5, col6, col7));
		} else
		{
			columns = new ArrayList<>(List.of(col1, col2, col3, col4, col5, col6, col7, col8));
		}

		if (AuthenticationUtil.hasRole(Role.TECHNIEKER) || AuthenticationUtil.hasRole(Role.ADMINISTRATOR)
				|| AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
		{
			columns.add(editColumn);
		}

		if (AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) || AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			TableColumn<MaintenanceDTO, Void> col9 = createDetailsButton();
			columns.add(col9);

		}

		if (AuthenticationUtil.hasRole(Role.TECHNIEKER) || AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
		{
			TableColumn<MaintenanceDTO, Void> col10 = createAddReportButton();
			columns.add(col10);
		}

		table.getColumns().addAll(columns);
		table.setPrefHeight(500);

		pagination = createPagination();

		VBox tableWithPagination = new VBox(10, table, pagination);
		VBox.setVgrow(table, Priority.ALWAYS);

		return new VBox(10, filterBox, tableWithPagination);
	}

	private HBox createTableHeaders()
	{
		searchField = new TextField();
		searchField.setPromptText(I18n.get("search"));
		searchField.setMaxWidth(300);
		searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

		executionDatePickerFilter = new DatePicker();
		executionDatePickerFilter.setPromptText(I18n.get("maintenance.execution-date"));
		executionDatePickerFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		startTimeFieldFilter = new ComboBox<LocalTime>();
		startTimeFieldFilter.setPromptText(I18n.get("maintenance.starttime"));
		populateTimePicker(startTimeFieldFilter);
		endTimeFieldFilter = new ComboBox<LocalTime>();
		endTimeFieldFilter.setPromptText(I18n.get("maintenance.endtime"));

		populateTimePicker(endTimeFieldFilter);
		startTimeFieldFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());
		endTimeFieldFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		technicianFilter = new ComboBox<String>();
		technicianFilter.setPromptText(I18n.get("maintenance.choose-technician"));
		technicianFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		reasonFilter = new TextField();
		reasonFilter.setPromptText(I18n.get("maintenance.reason"));
		reasonFilter.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

		commentsFilter = new TextField();
		commentsFilter.setPromptText(I18n.get("maintenance.comments"));
		commentsFilter.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

		statusFilter = new ComboBox<String>();
		statusFilter.setPromptText(I18n.get("maintenance.status"));
		Stream.of(MaintenanceStatus.values())
		      .map(status -> I18n.convertStatus(status.toString()))
		      .forEach(statusFilter.getItems()::add);
		statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());

		mainLayout.getServices().getUserController().getAllTechniekers().stream().map(u ->
		{
			String first = u.firstName();
			String last = u.lastName();
			String formattedLast = last.substring(0, 1).toUpperCase() + last.substring(1).toLowerCase();
			return String.format("%s %s", first, formattedLast);
		}).forEach((u) -> technicianFilter.getItems().add(u));

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox pageSelector = createPageSelector();

		CustomButton clearFiltersBtn = new CustomButton(I18n.get("clear-filters"));

		clearFiltersBtn.setOnAction(this::clearFilters);

		HBox filterBox = new HBox(10, searchField, executionDatePickerFilter, startTimeFieldFilter, endTimeFieldFilter,
				technicianFilter, reasonFilter, commentsFilter, statusFilter, clearFiltersBtn, spacer, pageSelector);
		filterBox.setAlignment(Pos.CENTER_LEFT);
		return filterBox;
	}

	private HBox createPageSelector()
	{
		Label lbl = new Label(I18n.get("maintenance.amount-per-page"));
		ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(10, 20, 50, 100));
		combo.setValue(itemsPerPage);
		combo.setOnAction(e -> updateItemsPerPage(combo.getValue()));

		HBox box = new HBox(10, lbl, combo);
		box.setAlignment(Pos.CENTER_RIGHT);
		return box;
	}

	private void updateItemsPerPage(int value)
	{
		this.itemsPerPage = value;
		this.currentPage = 0;
		updatePagination();
		updateTableItems();
	}

	private void clearFilters(Event event)
	{
		searchField.clear();
		executionDatePickerFilter.setValue(null);
		startTimeFieldFilter.setValue(null);
		endTimeFieldFilter.setValue(null);
		technicianFilter.setValue(null);
		reasonFilter.clear();
		commentsFilter.clear();
		statusFilter.setValue(null);
	}

	private void filterTable()
	{
		String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
		LocalDate selectedDate = executionDatePickerFilter.getValue();
		LocalTime selectedStartTime = startTimeFieldFilter.getValue();
		LocalTime selectedEndTime = endTimeFieldFilter.getValue();
		String technicianString = technicianFilter.getValue();
		String reasonQuery = reasonFilter.getText() != null ? reasonFilter.getText().toLowerCase().trim() : "";
		String commentsQuery = commentsFilter.getText() != null ? commentsFilter.getText().toLowerCase().trim() : "";
		String statusString = statusFilter.getValue() != null ? statusFilter.getValue() : "";

		filteredMaintenances = allMaintenances.stream().filter(m ->
		{
			boolean matchesQuery = query.isEmpty() || (m.reason() != null && m.reason().toLowerCase().contains(query))
					|| (m.comments() != null && m.comments().toLowerCase().contains(query))
					|| (m.technician() != null && m.technician().firstName().toLowerCase().contains(query))
					|| (m.technician() != null && m.technician().lastName().toLowerCase().contains(query));

			boolean matchesDate = selectedDate == null || selectedDate.equals(m.executionDate());

			boolean matchesStartTime = selectedStartTime == null
					|| m.startDate() == LocalDateTime.of(m.executionDate(), selectedStartTime);
			boolean matchesEndTime = selectedEndTime == null
					|| m.endDate() == LocalDateTime.of(m.executionDate(), selectedEndTime);

			boolean matchesTechnician = technicianString == null
					|| String.format("%s %s", m.technician().firstName(), m.technician().lastName()).toLowerCase()
							.equals(technicianString.toLowerCase());

			boolean matchesReason = reasonQuery.isBlank() || m.reason().toLowerCase().contains(reasonQuery);

			boolean matchesComment = commentsQuery.isBlank() || m.comments().toLowerCase().contains(commentsQuery);

			boolean matchesStatus = statusString.isBlank() || 
				    I18n.convertStatus(m.status().toString()).toLowerCase().equals(statusString.toLowerCase());

			return matchesQuery && matchesDate && matchesStartTime && matchesEndTime && matchesTechnician
					&& matchesReason && matchesComment && matchesStatus;
		}).collect(Collectors.toList());

		currentPage = 0;
		updatePagination();
		updateTableItems();
	}

	private TableColumn<MaintenanceDTO, String> createColumn(String title, Function<MaintenanceDTO, String> mapper)
	{
		TableColumn<MaintenanceDTO, String> col = new TableColumn<>(title);
		col.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
		return col;
	}

	private TableColumn<MaintenanceDTO, Void> createDetailsButton()
	{
		TableColumn<MaintenanceDTO, Void> col = new TableColumn<>(I18n.get("details"));

		col.setCellFactory(param -> new TableCell<>()
		{
			private final CustomButton btn = new CustomButton(I18n.get("details"), Pos.CENTER);
			{
				btn.setOnAction(e ->
				{
					if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR)
							|| AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
					{
						MaintenanceDTO selectedMaintenance = getTableView().getItems().get(getIndex());
						goToDetails(mainLayout, selectedMaintenance);
					} else
					{
						mainLayout.showNotAllowedAlert();
					}

				});
				btn.setMaxWidth(Double.MAX_VALUE);
				btn.setAlignment(Pos.CENTER);
			}

			@Override
			protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : btn);
				setAlignment(Pos.CENTER);
			}
		});
		return col;
	}

	private TableColumn<MaintenanceDTO, Void> createAddReportButton()
	{
		TableColumn<MaintenanceDTO, Void> col = new TableColumn<>(I18n.get("maintenance.add-report"));

		col.setCellFactory(param -> new TableCell<>()
		{
			private final CustomButton btn = new CustomButton(I18n.get("maintenance.add-report"), Pos.CENTER);
			{
				btn.setOnAction(e ->
				{
					if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.TECHNIEKER))
					{
						MaintenanceDTO selectedMaintenance = getTableView().getItems().get(getIndex());
						goToAddReport(mainLayout, selectedMaintenance);

					} else
					{
						mainLayout.showNotAllowedAlert();
					}

				});
				btn.setMaxWidth(Double.MAX_VALUE);
				btn.setAlignment(Pos.CENTER);
			}

			@Override
			protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : btn);
				setAlignment(Pos.CENTER);
			}
		});
		return col;
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
		totalPages = (int) Math.ceil((double) filteredMaintenances.size() / itemsPerPage);
	}

	private void updateTableItems()
	{
		int fromIndex = currentPage * itemsPerPage;
		int toIndex = Math.min(fromIndex + itemsPerPage, filteredMaintenances.size());

		if (filteredMaintenances.isEmpty())
		{
			table.getItems().clear();
		} else
		{
			List<MaintenanceDTO> currentPageItems = fromIndex < toIndex
					? filteredMaintenances.subList(fromIndex, toIndex)
					: List.of();
			table.getItems().setAll(currentPageItems);
		}
	}

	private void updateTable(List<MaintenanceDTO> list)
	{
		filteredMaintenances = list;
		updatePagination();
		updateTableItems();
	}

	private void goToEditMaintenanceForm(MaintenanceDTO maintenanceDTO)
	{
		mainLayout.showEditMaintenance(maintenanceDTO, machineDTO);
	}

	private void goToDetails(MainLayout mainLayout, MaintenanceDTO maintenance)
	{
		MaintenanceDetailView form = new MaintenanceDetailView(mainLayout, maintenance);
		form.getStylesheets().add(getClass().getResource("/css/maintenanceDetails.css").toExternalForm());
		mainLayout.showMaintenanceDetails(maintenance);
	}

	private void goToAddReport(MainLayout mainLayout, MaintenanceDTO maintenance)
	{
		AddReportForm form = new AddReportForm(mainLayout, maintenance);
		form.getStylesheets().add(getClass().getResource("/css/maintenanceDetails.css").toExternalForm());
		mainLayout.showAddReport(maintenance);
	}

	private void populateTimePicker(ComboBox<LocalTime> timePicker)
	{
		LocalTime time = LocalTime.of(0, 0);
		while (time.isBefore(LocalTime.of(23, 45)))
		{
			timePicker.getItems().add(time);
			time = time.plusMinutes(15);
		}

		timePicker.setConverter(new javafx.util.StringConverter<>()
		{
			private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

			@Override
			public String toString(LocalTime time)
			{
				return time != null ? formatter.format(time) : "";
			}

			@Override
			public LocalTime fromString(String string)
			{
				return (string != null && !string.isEmpty()) ? LocalTime.parse(string, formatter) : null;
			}
		});
	}

}
