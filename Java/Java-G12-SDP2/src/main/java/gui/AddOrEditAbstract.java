package gui;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.MachineController;
import domain.MaintenanceController;
import domain.ReportController;
import domain.SiteController;
import domain.UserController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.I18n;

public abstract class AddOrEditAbstract extends GridPane
{
	protected MachineController machineController;
	protected SiteController siteController;
	protected UserController userController;
	protected MaintenanceController maintenanceController;
	protected ReportController reportController;

	// Gemeenschappelijke velden
	protected final MainLayout mainLayout;
	protected boolean isNew;
	protected Label errorLabel;

	// Constructors
	public AddOrEditAbstract(MainLayout mainLayout, boolean isNew)
	{
		this.mainLayout = mainLayout;
		this.isNew = isNew;

		this.machineController = mainLayout.getServices().getMachineController();
		this.siteController = mainLayout.getServices().getSiteController();
		this.userController = mainLayout.getServices().getUserController();
		this.maintenanceController = mainLayout.getServices().getMaintenanceController();
		this.reportController = mainLayout.getServices().getReportController();

		initializeFields();
		buildGUI();

		if (!isNew)
		{
			fillData();
		}
	}

	// Gemeenschappelijke methoden
	protected void buildGUI()
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

	protected HBox createFormContent()
	{
		HBox formContent = new HBox(30);
		formContent.setAlignment(Pos.TOP_CENTER);
		formContent.getStyleClass().add("form-box");
		formContent.setMaxWidth(800);

		VBox leftBox = createLeftBox();
		VBox rightBox = createRightBox();

		formContent.getChildren().addAll(leftBox, rightBox);

		return formContent;
	}

	protected VBox createTitleSection()
	{
		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER_LEFT);

		FontIcon icon = new FontIcon("fas-arrow-left");
		icon.setIconSize(20);
		Button backButton = new Button();
		backButton.setGraphic(icon);
		backButton.getStyleClass().add("back-button");
		backButton.setOnAction(e -> navigateBack());

		Label title = new Label(getTitleText());
		title.getStyleClass().add("title-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		hbox.getChildren().addAll(backButton, title, spacer);

		return new VBox(10, hbox);
	}

	protected HBox createSaveButton()
	{
		Button saveButton = new Button(I18n.get("save"));
		saveButton.getStyleClass().add("save-button");
		saveButton.setOnAction(e -> save());

		saveButton.setPrefSize(300, 40);
		saveButton.setMaxWidth(Double.MAX_VALUE);

		HBox buttonBox = new HBox(saveButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(20, 0, 0, 0));

		buttonBox.setMinWidth(800);
		buttonBox.setMaxWidth(800);

		return buttonBox;
	}

	protected Label createErrorLabel()
	{
		Label errorLabel = new Label();
		errorLabel.getStyleClass().add("error-label");
		return errorLabel;
	}

	protected void showError(String message)
	{
		errorLabel.setText(message);
	}

	protected void resetErrorLabels()
	{
		errorLabel.setText("");
	}

	// Abstracte methoden die geïmplementeerd moeten worden in subklassen
	protected abstract void initializeFields();

	protected abstract VBox createLeftBox();

	protected abstract VBox createRightBox();

	protected abstract void fillData();

	protected abstract void save();

	protected abstract void navigateBack();

	protected abstract String getTitleText();

	protected abstract void handleInformationRequiredException(Exception e);

	protected abstract void showFieldError(String fieldName, String message);
}