package gui;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import dto.MaintenanceDTO;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;
import exceptions.InformationRequired;
import exceptions.InformationRequiredExceptionReport;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import util.I18n;

public class AddReportForm extends AddOrEditAbstract
{
	private Label siteNameLabel, responsiblePersonLabel, maintenanceNumberLabel;
	private ComboBox<String> technicianComboBox;
	private DatePicker startDatePicker, endDatePicker;
	private ComboBox<LocalTime> startTimeField;
	private ComboBox<LocalTime> endTimeField;
	private TextField reasonField;
	private TextArea commentsArea;
	private Label technicianErrorLabel, startDateErrorLabel, startTimeErrorLabel, endDateErrorLabel, endTimeErrorLabel,
			reasonErrorLabel;

	private MaintenanceDTO maintenanceDTO;

	public AddReportForm(MainLayout mainLayout, MaintenanceDTO maintenanceDTO)
	{
		super(mainLayout, false);
		this.maintenanceDTO = maintenanceDTO;

		if (maintenanceDTO == null)
		{
			mainLayout.showHomeScreen();
			throw new IllegalArgumentException(I18n.get("report-invalid"));
		}
	}

	@Override
	protected void initializeFields()
	{
		siteNameLabel = new Label(I18n.get("report-loading"));
		siteNameLabel.getStyleClass().add("info-value");

		responsiblePersonLabel = new Label(I18n.get("report-loading"));
		responsiblePersonLabel.getStyleClass().add("info-value");

		maintenanceNumberLabel = new Label(I18n.get("report-loading"));
		maintenanceNumberLabel.getStyleClass().add("info-value");

		technicianComboBox = new ComboBox<String>();

		startDatePicker = new DatePicker();
		startDatePicker.setPromptText(I18n.get("report.choose-startdate"));

		startTimeField = new ComboBox<>();
		startTimeField.setPromptText(I18n.get("report.choose-starttime"));
		populateTimePicker(startTimeField);

		endDatePicker = new DatePicker();
		endDatePicker.setPromptText(I18n.get("report.choose-enddate"));

		endTimeField = new ComboBox<>();
		endTimeField.setPromptText(I18n.get("report.choose-endtime"));
		populateTimePicker(endTimeField);

		reasonField = new TextField();
		reasonField.setPromptText(I18n.get("report.enter-reason"));

		commentsArea = new TextArea();
		commentsArea.setPrefRowCount(5);
		commentsArea.setWrapText(true);
		commentsArea.setPromptText(I18n.get("report.add-comments"));

		errorLabel = createErrorLabel();
		technicianErrorLabel = createErrorLabel();
		startDateErrorLabel = createErrorLabel();
		startTimeErrorLabel = createErrorLabel();
		endDateErrorLabel = createErrorLabel();
		endTimeErrorLabel = createErrorLabel();
		reasonErrorLabel = createErrorLabel();

		String errorStyle = "-fx-text-fill: red; -fx-font-weight: bold;";
		technicianErrorLabel.setStyle(errorStyle);
		startDateErrorLabel.setStyle(errorStyle);
		startTimeErrorLabel.setStyle(errorStyle);
		endTimeErrorLabel.setStyle(errorStyle);
		reasonErrorLabel.setStyle(errorStyle);
		errorLabel.setStyle(errorStyle);
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
				if (time != null)
				{
					return formatter.format(time);
				}
				return "";
			}

			@Override
			public LocalTime fromString(String string)
			{
				if (string != null && !string.isEmpty())
				{
					return LocalTime.parse(string, formatter);
				}
				return null;
			}
		});
	}

	@Override
	protected VBox createLeftBox()
	{
		VBox leftBox = new VBox(20);
		leftBox.setAlignment(Pos.TOP_LEFT);
		leftBox.setMinWidth(400);
		leftBox.setMaxWidth(600);

		leftBox.getChildren().addAll(createInformationBox(), createTechnicianBox());

		return leftBox;
	}

	@Override
	protected VBox createRightBox()
	{
		VBox rightBox = new VBox(20);
		rightBox.setAlignment(Pos.TOP_LEFT);
		rightBox.setMinWidth(400);
		rightBox.setMaxWidth(600);

		rightBox.getChildren().addAll(createDatesBox(), createLowBox());

		return rightBox;
	}

	private Node createInformationBox()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		String labelString = I18n.get("report.maintenance-data");
		Label sectionLabel = new Label(labelString);
		sectionLabel.getStyleClass().add("section-label");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;

		pane.add(new Label(I18n.get("add-machine.site")), 0, row);
		pane.add(siteNameLabel, 1, row++);

		pane.add(new Label(I18n.get("report.responsible")), 0, row);
		pane.add(responsiblePersonLabel, 1, row++);

		pane.add(new Label(I18n.get("report.maintenance-nr")), 0, row);
		pane.add(maintenanceNumberLabel, 1, row++);

		return pane;
	}

	private Node createTechnicianBox()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		String labelString = I18n.get("report.info");
		Label sectionLabel = new Label(labelString);
		sectionLabel.getStyleClass().add("section-label");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;
		pane.add(new Label(I18n.get("report.technician")), 0, row);
		pane.add(technicianComboBox, 1, row++);
		pane.add(technicianErrorLabel, 1, row++);

		return pane;
	}

	private Node createDatesBox()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		String labelString = I18n.get("report-dates");
		Label sectionLabel = new Label(labelString);
		sectionLabel.getStyleClass().add("section-label");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;
		pane.add(new Label(I18n.get("report.startdate")), 0, row);
		pane.add(startDatePicker, 1, row++);
		pane.add(startDateErrorLabel, 1, row++);

		pane.add(new Label(I18n.get("report.starttime")), 0, row);
		pane.add(startTimeField, 1, row++);
		pane.add(startTimeErrorLabel, 1, row++);

		pane.add(new Label(I18n.get("report.enddate")), 0, row);
		pane.add(endDatePicker, 1, row++);
		pane.add(endDateErrorLabel, 1, row++);

		pane.add(new Label(I18n.get("report.endtime")), 0, row);
		pane.add(endTimeField, 1, row++);
		pane.add(endTimeErrorLabel, 1, row++);

		return pane;
	}

	private Node createLowBox()
	{
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		int row = 1;

		pane.add(new Label(I18n.get("maintenance-add.reason")), 0, row);
		pane.add(reasonField, 1, row++);
		pane.add(reasonErrorLabel, 1, row++);

		pane.add(new Label(I18n.get("maintenance-add.comments")), 0, row);
		pane.add(commentsArea, 1, row++);

		return pane;
	}

	@Override
	protected void fillData()
	{
		Platform.runLater(() -> {
			siteNameLabel.setText(maintenanceDTO.machine().site().siteName());
			siteNameLabel.getStyleClass().add("info-value");

			responsiblePersonLabel
					.setText(maintenanceDTO.technician().firstName() + " " + maintenanceDTO.technician().lastName());

			responsiblePersonLabel.getStyleClass().add("info-value");

			maintenanceNumberLabel.setText("" + maintenanceDTO.id());
			maintenanceNumberLabel.getStyleClass().add("info-value");

			technicianComboBox.setPromptText(I18n.get("add-machine.select-technician"));
			technicianComboBox.getItems().addAll(userController.getAllTechniekers().stream()
					.map(user -> user.firstName() + " " + user.lastName()).collect(Collectors.toList()));
		});

	}

	@Override
	protected void resetErrorLabels()
	{
		errorLabel.setText("");
		technicianErrorLabel.setText("");
		startDateErrorLabel.setText("");
		startTimeErrorLabel.setText("");
		endDateErrorLabel.setText("");
		endTimeErrorLabel.setText("");
		reasonErrorLabel.setText("");
	}

	@Override
	protected void save()
	{
		resetErrorLabels();
		try
		{

			boolean hasErrors = false;

			if (technicianComboBox.getValue() == null)
			{
				showFieldError("technician", I18n.get("add-machine.select-technician"));
				hasErrors = true;
			}

			if (startDatePicker.getValue() == null)
			{
				showFieldError("startDate", I18n.get("report-dates"));
				hasErrors = true;
			}

			if (startTimeField.getValue() == null)
			{
				showFieldError("startTime", I18n.get("report.select-starttime"));
				hasErrors = true;
			}

			if (endDatePicker.getValue() == null)
			{
				showFieldError("endDate", I18n.get("report.select-enddate"));
				hasErrors = true;
			}

			if (endTimeField.getValue() == null)
			{
				showFieldError("endTime", I18n.get("report.select-endtime"));
				hasErrors = true;
			}

			if (reasonField.getText() == null || reasonField.getText().trim().isEmpty())
			{
				showFieldError("reason", I18n.get("report.enter-reason"));
				hasErrors = true;
			}

			if (hasErrors)
			{
				return;
			}

			UserDTO selectedTechnician = null;
			if (technicianComboBox.getValue() != null)
			{
				String selectedName = technicianComboBox.getValue();
				selectedTechnician = userController.getAllTechniekers().stream()
						.filter(user -> (user.firstName() + " " + user.lastName()).equals(selectedName)).findFirst()
						.orElse(null);
			}

			SiteDTOWithoutMachines siteWoMachines = maintenanceDTO.machine().site();
			MaintenanceDTO maintenance = maintenanceController.getMaintenanceDTO(maintenanceDTO.id());
			UserDTO technician = selectedTechnician != null ? userController.getUserById(selectedTechnician.id())
					: null;

			reportController.createReport(siteWoMachines, maintenance, technician, startDatePicker.getValue(),
					startTimeField.getValue(), endDatePicker.getValue(), endTimeField.getValue(),
					reasonField.getText().trim(), commentsArea.getText().trim());

			navigateBack();
		} catch (InformationRequiredExceptionReport e)
		{
			handleInformationRequiredException(e);
		} catch (Exception e)
		{
			e.printStackTrace();
			showError(I18n.get("error") + e.getMessage());
		}
	}

	@Override
	protected void navigateBack()
	{
		mainLayout.showMaintenanceList();
	}

	@Override
	protected String getTitleText()
	{
		return I18n.get("report.title");
	}

	@Override
	protected void handleInformationRequiredException(Exception e)
	{

		if (e instanceof InformationRequired)
		{
			InformationRequired exception = (InformationRequired) e;
			exception.getRequiredElements().forEach((field, requiredElement) -> {
				String errorMessage = requiredElement.getMessage();
				showFieldError(field, errorMessage);
			});
		} else
		{
			showError(I18n.get("unexpected-error") + e.getMessage());
		}
	}

	@Override
	protected void showFieldError(String fieldName, String message)
	{
		switch (fieldName)
		{
		case "technician":
			technicianErrorLabel.setText(message);
			break;
		case "startDate":
			startDateErrorLabel.setText(message);
			break;
		case "startTime":
			startTimeErrorLabel.setText(message);
			break;
		case "endDate":
			endDateErrorLabel.setText(message);
			break;
		case "endTime":
			endTimeErrorLabel.setText(message);
			break;
		case "reason":
			reasonErrorLabel.setText(message);
			break;
		default:
			errorLabel.setText(message);
		}
	}
}