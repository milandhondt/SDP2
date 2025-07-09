package gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.MachineController;
import domain.MaintenanceController;
import domain.UserController;
import dto.MachineDTO;
import dto.MaintenanceDTO;
import dto.UserDTO;
import exceptions.InformationRequired;
import exceptions.InformationRequiredExceptionMaintenance;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.AuthenticationUtil;
import util.MaintenanceStatus;
import util.Role;

public class MaintenancePlanningForm extends GridPane
{

	private final MainLayout mainLayout;
	private final MachineDTO machineDTO;
	private final MaintenanceDTO maintenanceDTO;
	private final MachineController mc;
	private final MaintenanceController mntcc;
	private final UserController uc;

	private Label errorLabel;
	private Label startDateErrorLabel, endDateErrorLabel, machineErrorLabel, statusErrorLabel, reasonErrorLabel,
			technicianErrorLabel, executionDateErrorLabel;

	private TextField reasonField;

	private TextArea commentsField;

	private ComboBox<String> technicianComboBox;
	private ComboBox<LocalTime> startTimeField, endTimeField;
	private ComboBox<String> statusComboBox;
	private ComboBox<String> machineComboBox;
	private Map<String, UserDTO> technicianMap;
	private Map<String, MachineDTO> machineMap;

	private DatePicker executionDatePicker;

	public MaintenancePlanningForm(MainLayout mainLayout, MachineDTO machineDTO)
	{
		this.mainLayout = mainLayout;
		this.machineDTO = machineDTO;
		this.maintenanceDTO = null;
		this.mc = mainLayout.getServices().getMachineController();
		this.mntcc = mainLayout.getServices().getMaintenanceController();
		this.uc = mainLayout.getServices().getUserController();

		initializeFields();
		buildGUI();
	}

	public MaintenancePlanningForm(MainLayout mainLayout, MaintenanceDTO maintenanceDTO, MachineDTO machineDTO)
	{
		this.mainLayout = mainLayout;
		this.machineDTO = machineDTO;
		this.mc = mainLayout.getServices().getMachineController();
		this.mntcc = mainLayout.getServices().getMaintenanceController();
		this.uc = mainLayout.getServices().getUserController();
		this.maintenanceDTO = maintenanceDTO;
		initializeFields();
		buildGUI();
	}

	private void buildGUI()
	{
		this.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());
		this.setAlignment(Pos.CENTER);
		this.setHgap(10);
		this.setVgap(15);
		this.setPadding(new Insets(20));

		VBox formAndSaveButton = new VBox(10);
		formAndSaveButton.getChildren().addAll(createFormContent(), createSaveButton());

		VBox mainContainer = new VBox(10);
		mainContainer.setAlignment(Pos.TOP_CENTER);
		mainContainer.setPadding(new Insets(10));
		mainContainer.getChildren().addAll(createTitleSection(), errorLabel, formAndSaveButton);

		this.add(mainContainer, 0, 0);
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
		backButton.setOnAction(e -> mainLayout.showMaintenanceList(machineDTO));
		this.add(backButton, 0, 0, 2, 1);

		Label title = new Label("Onderhoud inplannen");
		title.getStyleClass().add("title-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		hbox.getChildren().addAll(backButton, title, spacer);

		return new VBox(10, hbox);
	}

	private HBox createSaveButton()
	{
		Button saveButton = new Button("Opslaan");
		saveButton.getStyleClass().add("save-button");
		saveButton.setOnAction(e -> {

			if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
			{
				savePlanning();
			} else
			{
				mainLayout.showNotAllowedAlert();
			}
		});

		saveButton.setPrefSize(300, 40);
		saveButton.setMaxWidth(Double.MAX_VALUE);

		HBox buttonBox = new HBox(saveButton);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		buttonBox.setPadding(new Insets(20, 0, 0, 0));

		buttonBox.setMinWidth(800);
		buttonBox.setMaxWidth(800);

		return buttonBox;
	}

	private void savePlanning()
	{
		if (AuthenticationUtil.hasRole(Role.ADMINISTRATOR) || AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE))
		{
			try
			{
				resetErrorLabels();

				LocalDate execDate = executionDatePicker.getValue();
				LocalTime startTime = startTimeField.getValue();
				LocalTime endTime = endTimeField.getValue();

				// Convert to LocalDateTime
				LocalDateTime startDateTime = (execDate != null && startTime != null)
						? LocalDateTime.of(execDate, startTime)
						: null;
				LocalDateTime endDateTime = (execDate != null && endTime != null) ? LocalDateTime.of(execDate, endTime)
						: null;

				// Get IDs for technician and machine
				String selectedTechnician = technicianComboBox.getValue();
				int technicianId = selectedTechnician != null && technicianMap.containsKey(selectedTechnician)
						? technicianMap.get(selectedTechnician).id()
						: 0;
				String selectedMachine = machineComboBox.getValue();
				int machineId = selectedMachine != null && machineMap.containsKey(selectedMachine)
						? machineMap.get(selectedMachine).id()
						: 0;

				// Get status
				MaintenanceStatus status = statusComboBox.getValue() != null
						? MaintenanceStatus.valueOf(statusComboBox.getValue())
						: null;

				// Use the controller to create the maintenance
				if (maintenanceDTO == null)
				{
					mntcc.createMaintenance(execDate, startDateTime, endDateTime, technicianId, reasonField.getText(),
							commentsField.getText(), status, machineId);
				} else
				{
					mntcc.updateMaintenance(maintenanceDTO.id(), execDate, startDateTime, endDateTime, technicianId,
							reasonField.getText(), commentsField.getText(), status, machineId);
				}

				mainLayout.showMaintenanceList(machineDTO);

			} catch (InformationRequiredExceptionMaintenance ex)
			{
				handleInformationRequiredException(ex);
			} catch (Exception ex)
			{
				errorLabel.setText("Er is een fout opgetreden: " + ex.getMessage());
				ex.printStackTrace();
			}
		} else
		{
			mainLayout.showNotAllowedAlert();
		}
	}

	private HBox createFormContent()
	{
		HBox formContent = new HBox(30);
		formContent.setAlignment(Pos.TOP_CENTER);
		formContent.getStyleClass().add("form-box");
		HBox.setHgrow(formContent, Priority.ALWAYS);

		VBox dataBox = new VBox(15, createDateSection());
		VBox technicianReasonBox = new VBox(15, createTechnicianReasonSection());
		VBox otherBox = new VBox(15, createOtherSection());

		VBox leftBox = new VBox(20);
		leftBox.setAlignment(Pos.TOP_LEFT);
		leftBox.setMinWidth(600);
		leftBox.setMaxWidth(800);

		leftBox.getChildren().addAll(dataBox, technicianReasonBox);

		VBox rightBox = new VBox(20);
		rightBox.setAlignment(Pos.TOP_LEFT);
		rightBox.setMinWidth(600);
		rightBox.setMaxWidth(800);

		rightBox.getChildren().addAll(otherBox);

		formContent.getChildren().addAll(leftBox, rightBox);

		return formContent;
	}

	private void populateFormFieldsForEdit()
	{
		if (maintenanceDTO.executionDate() != null)
		{
			executionDatePicker.setValue(maintenanceDTO.executionDate());
		}

		if (maintenanceDTO.startDate() != null)
		{
			startTimeField.setValue(maintenanceDTO.startDate().toLocalTime());
		}

		if (maintenanceDTO.endDate() != null)
		{
			endTimeField.setValue(maintenanceDTO.endDate().toLocalTime());
		}

		if (maintenanceDTO.technician() != null)
		{
			String key = String.format("%d %s %s", maintenanceDTO.technician().id(),
					maintenanceDTO.technician().firstName(), maintenanceDTO.technician().lastName());
			if (technicianMap.containsKey(key))
			{
				technicianComboBox.setValue(key);
			}
		}

		reasonField.setText(maintenanceDTO.reason() != null ? maintenanceDTO.reason() : "");
		commentsField.setText(maintenanceDTO.comments() != null ? maintenanceDTO.comments() : "");

		if (maintenanceDTO.status() != null)
		{
			statusComboBox.setValue(maintenanceDTO.status().name());
		}

		if (maintenanceDTO.machine() != null)
		{
			String key = String.format("%d Machine %s", maintenanceDTO.machine().id(), maintenanceDTO.machine().code());
			if (machineMap.containsKey(key))
			{
				machineComboBox.setValue(key);
			}
		}

	}

	private void initializeFields()
	{
		startDateErrorLabel = createErrorLabel();
		endDateErrorLabel = createErrorLabel();
		machineErrorLabel = createErrorLabel();
		statusErrorLabel = createErrorLabel();
		technicianErrorLabel = createErrorLabel();
		reasonErrorLabel = createErrorLabel();
		executionDateErrorLabel = createErrorLabel();
		errorLabel = createErrorLabel();

		reasonField = new TextField();
		reasonField.setPrefWidth(200);

		commentsField = new TextArea();

		List<UserDTO> technicians = uc.getAllTechniekers();

		technicianMap = technicians.stream()
				.collect(Collectors.toMap(t -> String.format("%d %s %s", t.id(), t.firstName(), t.lastName()), t -> t));

		technicianComboBox = new ComboBox<>();
		technicianComboBox.getItems().addAll(technicianMap.keySet());
		technicianComboBox.setPromptText("Selecteer technieker");
		technicianComboBox.setPrefWidth(200);

		technicianComboBox.setPromptText("Selecteer technieker");
		technicianComboBox.setPrefWidth(200);

		startTimeField = new ComboBox<>();
		startTimeField.setPrefWidth(200);
		endTimeField = new ComboBox<>();
		endTimeField.setPrefWidth(200);

		startTimeField.setPromptText("Starttijd");
		endTimeField.setPromptText("Eindtijd");

		populateTimePicker(endTimeField);
		populateTimePicker(startTimeField);

		executionDatePicker = new DatePicker();
		executionDatePicker.setEditable(false);

		statusComboBox = new ComboBox<>();
		statusComboBox.setPrefWidth(200);
		machineComboBox = new ComboBox<>();
		machineComboBox.setPrefWidth(200);

		List<String> statusses = Arrays.stream(MaintenanceStatus.values()).map(Enum::toString).toList();
		if (AuthenticationUtil.hasRole(Role.TECHNIEKER))
		{
			statusses = Arrays.stream(MaintenanceStatus.values()).filter(s -> !s.equals(MaintenanceStatus.INGEPLAND))
					.map(Enum::toString).toList();
		}
		statusComboBox.getItems().addAll(statusses);
		statusComboBox.setPromptText("Selecteer status");

		List<MachineDTO> machines = mc.getMachineList();
		machineMap = machines.stream()
				.collect(Collectors.toMap(m -> String.format("%d Machine %s", m.id(), m.code()), m -> m));

		machineComboBox.getItems().addAll(machineMap.keySet());
		machineComboBox.setPromptText("Selecteer machine");
		machineComboBox.setPrefWidth(200);

		if (machineDTO != null)
		{
			String key = String.format("%d Machine %s", machineDTO.id(), machineDTO.code());
			machineComboBox.setValue(key);
			machineComboBox.setDisable(true);
		}

		if (maintenanceDTO != null)
		{
			populateFormFieldsForEdit();
		}
	}

	private GridPane createOtherSection()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		int row = 1;

		pane.add(new Label("Opmerkingen:"), 0, row);
		pane.add(commentsField, 1, row++);

		pane.add(new Label("Status:"), 0, row);
		pane.add(statusComboBox, 1, row++);
		pane.add(statusErrorLabel, 1, row++);

		pane.add(new Label("Machine:"), 0, row);
		pane.add(machineComboBox, 1, row++);
		pane.add(machineErrorLabel, 1, row++);

		return pane;
	}

	private GridPane createTechnicianReasonSection()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		int row = 1;

		pane.add(new Label("Technieker:"), 0, row);
		pane.add(technicianComboBox, 1, row++);
		pane.add(technicianErrorLabel, 1, row++);

		pane.add(new Label("Reden:"), 0, row);
		pane.add(reasonField, 1, row++);
		pane.add(reasonErrorLabel, 1, row++);

		return pane;
	}

	private GridPane createDateSection()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		Label sectionLabel = new Label("Datum en tijd");
		sectionLabel.getStyleClass().add("section-label");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;
		pane.add(new Label("Datum uitgevoerd:"), 0, row);
		pane.add(executionDatePicker, 1, row++);
		pane.add(executionDateErrorLabel, 1, row++);

		pane.add(new Label("Starttijdstip:"), 0, row);
		pane.add(startTimeField, 1, row++);
		pane.add(startDateErrorLabel, 1, row++);

		pane.add(new Label("Eindtijdstip:"), 0, row);
		pane.add(endTimeField, 1, row++);
		pane.add(endDateErrorLabel, 1, row++);

		return pane;
	}

	private void handleInformationRequiredException(Exception e)
	{
		if (e instanceof InformationRequired)
		{
			InformationRequired exception = (InformationRequired) e;
			exception.getRequiredElements().forEach((field, requiredElement) -> {
				String errorMessage = requiredElement.getMessage();
				showFieldError(field, errorMessage);
			});
		}
	}

	private void showFieldError(String fieldName, String message)
	{
		switch (fieldName)
		{
		case "startDate":
			startDateErrorLabel.setText(message);
			break;
		case "endDate":
			endDateErrorLabel.setText(message);
			break;
		case "technician":
			technicianErrorLabel.setText(message);
			break;
		case "reason":
			reasonErrorLabel.setText(message);
			break;
		case "machine":
			machineErrorLabel.setText(message);
			break;
		case "status":
			statusErrorLabel.setText(message);
			break;
		case "executionDate":
			executionDateErrorLabel.setText(message);
			break;
		default:
			errorLabel.setText(message);
		}
	}

	private void resetErrorLabels()
	{
		errorLabel.setText("");
		startDateErrorLabel.setText("");
		endDateErrorLabel.setText("");
		technicianErrorLabel.setText("");
		reasonErrorLabel.setText("");
		machineErrorLabel.setText("");
		statusErrorLabel.setText("");
		executionDateErrorLabel.setText("");
	}

	private Label createErrorLabel()
	{
		Label error = new Label();
		error.getStyleClass().add("error-label");
		return error;
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
