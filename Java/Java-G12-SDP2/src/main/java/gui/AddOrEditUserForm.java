package gui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import dto.AddressDTO;
import dto.UserDTO;
import exceptions.InformationRequired;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import util.AuthenticationUtil;
import util.I18n;
import util.ItemI18n;
import util.Role;
import util.Status;

public class AddOrEditUserForm extends AddOrEditAbstract {
	private UserDTO userDTO;
	private TextField firstNameField, lastNameField, emailField, phoneField;
	private DatePicker birthdatePicker;
	private TextField streetField, houseNumberField, postalCodeField, cityField;
	private ComboBox<ItemI18n<Role>> roleBox;
	private ComboBox<ItemI18n<Status>> statusBox;
	private Label firstNameError, lastNameError, emailError, phoneError, birthdateError;
	private Label streetError, houseNumberError, postalCodeError, cityError;
	private Label roleError, statusError;

	public AddOrEditUserForm(MainLayout mainLayout, int userId) {
		super(mainLayout, false);

		this.userDTO = userController.getUserById(userId);
	}

	public AddOrEditUserForm(MainLayout mainLayout) {
		super(mainLayout, true);
	}

	@Override
	protected void initializeFields() {
		streetError = createErrorLabel();
		houseNumberError = createErrorLabel();
		postalCodeError = createErrorLabel();
		cityError = createErrorLabel();
		roleError = createErrorLabel();
		statusError = createErrorLabel();
		firstNameError = createErrorLabel();
		lastNameError = createErrorLabel();
		emailError = createErrorLabel();
		phoneError = createErrorLabel();
		birthdateError = createErrorLabel();
		errorLabel = createErrorLabel();

		statusBox = new ComboBox<>();
		statusBox.getItems().addAll(Stream.of(Status.values())
				.map((s) -> new ItemI18n<Status>(s, I18n.convertStatus(s.toString())))
				.collect(Collectors.toList()));
		statusBox.setPromptText(I18n.get("user-add.edit-status"));

		roleBox = new ComboBox<>();
		roleBox.getItems().addAll(Stream.of(Role.values())
				.map((s) -> new ItemI18n<Role>(s, I18n.convertRole(s.toString()))).collect(Collectors.toList()));
		roleBox.setPromptText(I18n.get("user-add.select-role"));

		firstNameField = new TextField();
		lastNameField = new TextField();
		emailField = new TextField();
		phoneField = new TextField();
		birthdatePicker = new DatePicker();
		birthdatePicker.setEditable(false);
		streetField = new TextField();
		houseNumberField = new TextField();
		postalCodeField = new TextField();
		cityField = new TextField();
	}

	@Override
	protected VBox createLeftBox() {
		VBox leftBox = new VBox(20);
		leftBox.setAlignment(Pos.TOP_LEFT);
		leftBox.setMinWidth(400);
		leftBox.setMaxWidth(400);

		leftBox.getChildren().addAll(createUserFieldsSection());

		return leftBox;
	}

	@Override
	protected VBox createRightBox() {
		VBox rightBox = new VBox(20);
		rightBox.setAlignment(Pos.TOP_LEFT);
		rightBox.setMinWidth(400);
		rightBox.setMaxWidth(400);

		rightBox.getChildren().addAll(createAddressFieldsSection(), createRoleStatusSection());

		return rightBox;
	}

	private GridPane createUserFieldsSection() {
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		Label sectionLabel = new Label(I18n.get("user-add.user-data"));
		sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;
		pane.add(new Label(I18n.get("user-add.name")), 0, row);
		pane.add(firstNameField, 1, row++);
		pane.add(firstNameError, 1, row++);

		pane.add(new Label(I18n.get("user-add.lastname")), 0, row);
		pane.add(lastNameField, 1, row++);
		pane.add(lastNameError, 1, row++);

		pane.add(new Label(I18n.get("user-add.email")), 0, row);
		pane.add(emailField, 1, row++);
		pane.add(emailError, 1, row++);

		pane.add(new Label(I18n.get("user-add.phone")), 0, row);
		pane.add(phoneField, 1, row++);
		pane.add(phoneError, 1, row++);

		pane.add(new Label(I18n.get("user-add.birthdate")), 0, row);
		pane.add(birthdatePicker, 1, row++);
		pane.add(birthdateError, 1, row++);

		return pane;
	}

	private GridPane createAddressFieldsSection() {
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		Label sectionLabel = new Label(I18n.get("user-add.address-data"));
		sectionLabel.getStyleClass().add("section-label");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;
		pane.add(new Label(I18n.get("user-add.address.street")), 0, row);
		pane.add(streetField, 1, row++);
		pane.add(streetError, 1, row++);

		pane.add(new Label(I18n.get("user-add.address.number")), 0, row);
		pane.add(houseNumberField, 1, row++);
		pane.add(houseNumberError, 1, row++);

		pane.add(new Label(I18n.get("user-add.address.zip")), 0, row);
		pane.add(postalCodeField, 1, row++);
		pane.add(postalCodeError, 1, row++);

		pane.add(new Label(I18n.get("user-add.address-city")), 0, row);
		pane.add(cityField, 1, row++);
		pane.add(cityError, 1, row++);

		return pane;
	}

	private GridPane createRoleStatusSection() {
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setHgap(10);

		String labelString = isNew ? I18n.get("user-add.role") : I18n.get("user-add.role-and-status");

		Label sectionLabel = new Label(labelString);

		sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		pane.add(sectionLabel, 0, 0, 2, 1);

		int row = 1;
		pane.add(new Label(I18n.get("user-add.role") + ":"), 0, row);
		pane.add(roleBox, 1, row++);
		pane.add(roleError, 1, row++);

		if (!isNew) {
			pane.add(new Label(I18n.get("user-add.status")), 0, row);
			pane.add(statusBox, 1, row++);
			pane.add(statusError, 1, row++);
		}

		return pane;
	}

	@Override
	protected void fillData() {
		Platform.runLater(() -> {
			firstNameField.setText(userDTO.firstName());
			lastNameField.setText(userDTO.lastName());
			emailField.setText(userDTO.email());
			phoneField.setText(userDTO.phoneNumber());
			birthdatePicker.setValue(userDTO.birDate());

			AddressDTO address = userDTO.address();
			if (address != null) {
				streetField.setText(address.street());
				houseNumberField.setText(String.valueOf(address.number()));
				postalCodeField.setText(String.valueOf(address.postalcode()));
				cityField.setText(address.city());
			}

			roleBox.setValue(new ItemI18n<Role>(userDTO.role(), I18n.convertRole(userDTO.role().toString())));
			statusBox.setValue(new ItemI18n<Status>(userDTO.status(), I18n.convertStatus(userDTO.status().toString())));
		});
	}

	@Override
	protected void save() {
		resetErrorLabels();

		if (!AuthenticationUtil.hasRole(Role.ADMINISTRATOR)) {
			mainLayout.showNotAllowedAlert();
			return;
		}

		try {
			Status selectedStatus = 
					statusBox.getValue() != null ? statusBox.getValue().getValue() : null; 
			Role selectedRole =
					roleBox.getValue() != null ? roleBox.getValue().getValue() : null;
			if (isNew) {
				userController.createUser(firstNameField.getText(), lastNameField.getText(), emailField.getText(),
						phoneField.getText(), birthdatePicker.getValue(), streetField.getText(),
						houseNumberField.getText(), postalCodeField.getText(), cityField.getText(), 
						selectedRole);
			} else {
				userController.updateUser(userDTO.id(), firstNameField.getText(), lastNameField.getText(),
						emailField.getText(), phoneField.getText(), birthdatePicker.getValue(), streetField.getText(),
						houseNumberField.getText(), postalCodeField.getText(), cityField.getText(), 
						roleBox.getValue().getValue(),
						selectedStatus);
			}

			navigateBack();
		} catch (NumberFormatException e) {
			showError(I18n.get("user-add.number-zip.numerique"));
		} catch (IllegalArgumentException e) {
			handleInformationRequiredException(e);
		} catch (Exception e) {
			// hier ipv dit teruggaan naar beginscherm
			showError(I18n.get("error") + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void navigateBack() {
		mainLayout.showUserManagementScreen();
	}

	@Override
	protected String getTitleText() {
		return isNew ? I18n.get("user-add.add-message") : I18n.get("user-add.edit-message");
	}

	@Override
	protected void handleInformationRequiredException(Exception e) {
		if (e instanceof InformationRequired) {
			InformationRequired exception = (InformationRequired) e;
			exception.getRequiredElements().forEach((field, requiredElement) -> {
				String errorMessage = requiredElement.getMessage();
				showFieldError(field, errorMessage);
			});
		}
		if (e instanceof IllegalArgumentException) {
			String errorMessage = e.getMessage();
			showFieldError("email", errorMessage);
		}
	}

	@Override
	protected void showFieldError(String fieldName, String message) {
		switch (fieldName) {
		case "firstName":
			firstNameError.setText(message);
			break;
		case "lastName":
			lastNameError.setText(message);
			break;
		case "email":
			emailError.setText(message);
			break;
		case "phone":
			phoneError.setText(message);
			break;
		case "birthDate":
			birthdateError.setText(message);
			break;
		case "street":
			streetError.setText(message);
			break;
		case "number":
			houseNumberError.setText(message);
			break;
		case "postalCode":
			postalCodeError.setText(message);
			break;
		case "city":
			cityError.setText(message);
			break;
		case "role":
			roleError.setText(message);
			break;
		case "status":
			statusError.setText(message);
			break;
		default:
			errorLabel.setText(message);
		}
	}

	@Override
	protected void resetErrorLabels() {
		super.resetErrorLabels();
		firstNameError.setText("");
		lastNameError.setText("");
		emailError.setText("");
		phoneError.setText("");
		birthdateError.setText("");
		streetError.setText("");
		houseNumberError.setText("");
		postalCodeError.setText("");
		cityError.setText("");
		roleError.setText("");
		statusError.setText("");
	}
}