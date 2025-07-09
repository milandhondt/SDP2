package gui;

import domain.UserController;
import exceptions.InvalidInputException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import util.I18n;

public class LoginPane extends VBox
{

	private static final String CSS_PATH = "/css/login.css";
	private static final String LOGO_PATH = "/images/delaware_logo.png";

	private final UserController userController = new UserController();
	private final MainLayout mainLayout;

	private final Label errorLabel = new Label();
	private final TextField emailField = new TextField();
	private final PasswordField passwordField = new PasswordField();
	private final TextField passwordTextField = new TextField();

	private final Label emailValidationLabel = new Label();
	private final Label passwordValidationLabel = new Label();

	public LoginPane(MainLayout mainLayout)
	{
		this.mainLayout = mainLayout;
		this.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
		this.getStyleClass().add("login-pane");
		setupLayout();
	}

	private void setupLayout()
	{
		this.setAlignment(Pos.CENTER);
		this.setPadding(new Insets(40));

		HBox logoContainer = createLogoContainer();
		HBox mainLayout = createMainLayout();

		this.getChildren().addAll(logoContainer, mainLayout);
	}

	private HBox createLogoContainer()
	{
		Image logoImage = new Image(getClass().getResourceAsStream(LOGO_PATH));
		ImageView logoView = new ImageView(logoImage);
		logoView.setFitHeight(120);
		logoView.setPreserveRatio(true);

		HBox logoContainer = new HBox();
		logoContainer.setAlignment(Pos.CENTER);
		logoContainer.setPadding(new Insets(0, 0, 20, 0));
		logoContainer.getChildren().add(logoView);

		return logoContainer;
	}

	private HBox createMainLayout()
	{
		HBox mainLayout = new HBox(40);
		mainLayout.setAlignment(Pos.CENTER);
		mainLayout.setPadding(new Insets(20));

		VBox loginForm = createLoginForm();
		StackPane loginImage = createLoginImage(loginForm);

		mainLayout.getChildren().addAll(loginForm, loginImage);
		return mainLayout;
	}

	private VBox createLoginForm()
	{
		VBox form = new VBox(15);
		form.setAlignment(Pos.CENTER_LEFT);
		form.getStyleClass().add("login-form");
		form.setFillWidth(true);

		Label welcomeLabel = new Label(I18n.get("login.welcome"));
		welcomeLabel.getStyleClass().add("welcome-label");
		welcomeLabel.setMaxWidth(Double.MAX_VALUE);
		welcomeLabel.setAlignment(Pos.CENTER);
		
		VBox infoBox = createInfoBox();

		errorLabel.getStyleClass().add("error-label");

		Label emailLabel = new Label(I18n.get("login.email"));
		emailLabel.getStyleClass().add("login-label");
		emailField.setPromptText(I18n.get("login.email.placeholder"));
		emailField.getStyleClass().add("login-field");

		emailValidationLabel.getStyleClass().add("validation-label");
		emailValidationLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
		emailValidationLabel.setVisible(false);

		Label passwordLabel = new Label(I18n.get("login.password"));
		passwordLabel.getStyleClass().add("login-label");
		passwordField.setPromptText("●●●●●●●●●●●");
		passwordField.getStyleClass().add("login-field");
		
		ImageView eyeImage = new ImageView(new Image(getClass().getResourceAsStream("/images/showpassword.png")));
		eyeImage.setFitHeight(16);
		eyeImage.setPreserveRatio(true);
		
        Button togglePasswordButton = new Button();
        togglePasswordButton.setGraphic(eyeImage);
        togglePasswordButton.setStyle("-fx-background-color: transparent;");
        togglePasswordButton.setFocusTraversable(false);
        togglePasswordButton.setPadding(new Insets(0));
        togglePasswordButton.setMinSize(24, 24);
        togglePasswordButton.setPrefSize(24, 24);
        
        passwordField.setStyle("-fx-padding: 0 30 0 5;");
        passwordTextField.setStyle("-fx-padding: 0 30 0 5;");
        
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
		passwordTextField.getStyleClass().add("login-field");
		passwordTextField.setVisible(false);
		passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());		
        togglePasswordButton.setOnAction(e -> {
            boolean isVisible = passwordTextField.isVisible();
            passwordTextField.setVisible(!isVisible);
            passwordField.setVisible(isVisible);
            String imagePath = isVisible ? "/images/showpassword.png" : "/images/hidepassword.png";
            eyeImage.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        });
        
        StackPane passwordStack = new StackPane();
        passwordField.minHeightProperty().bind(emailField.heightProperty());
        passwordTextField.minHeightProperty().bind(emailField.heightProperty());
        passwordStack.getChildren().addAll(passwordField, passwordTextField, togglePasswordButton);
        StackPane.setAlignment(togglePasswordButton, Pos.CENTER_RIGHT);

        passwordStack.setMaxWidth(Double.MAX_VALUE);

		passwordValidationLabel.getStyleClass().add("validation-label");
		passwordValidationLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
		passwordValidationLabel.setVisible(false);
		passwordValidationLabel.setText(I18n.get("login.password.required"));

		Button loginButton = new Button(I18n.get("login.button").toUpperCase());
		loginButton.getStyleClass().add("login-button");
		loginButton.setOnAction(e -> handleLogin());
		loginButton.setMaxWidth(Double.MAX_VALUE);

		VBox.setVgrow(loginButton, Priority.ALWAYS);
		VBox.setMargin(welcomeLabel, new Insets(0));

		VBox emailContainer = new VBox(2);
		emailContainer.getChildren().addAll(emailField, emailValidationLabel);

		form.getChildren().addAll(welcomeLabel, infoBox, errorLabel, emailLabel, emailContainer, passwordLabel,
				passwordStack, loginButton);

		return form;
	}

	private VBox createInfoBox()
	{
		VBox infoBox = new VBox(5);

		Label infoTitle = new Label(I18n.get("login.no-account"));
		infoTitle.setStyle("-fx-font-weight: bold;");

		Label infoDescription = new Label(I18n.get("login.no-account.contact"));
		infoDescription.getStyleClass().add("info-description");

		infoBox.getChildren().addAll(infoTitle, infoDescription);
		return infoBox;
	}

	private StackPane createLoginImage(VBox loginForm)
	{
		StackPane imageContainer = new StackPane();
		imageContainer.setPrefWidth(800);
		imageContainer.prefHeightProperty().bind(loginForm.heightProperty());
		imageContainer.getStyleClass().add("login-image");

		Rectangle clip = new Rectangle();
		clip.setArcWidth(24);
		clip.setArcHeight(24);
		clip.widthProperty().bind(imageContainer.widthProperty());
		clip.heightProperty().bind(imageContainer.heightProperty());

		imageContainer.setClip(clip);

		return imageContainer;
	}

	private void handleLogin()
	{
		String email = emailField.getText().trim();
		String password = passwordField.getText();

		errorLabel.setVisible(false);
		emailValidationLabel.setVisible(false);
		passwordValidationLabel.setVisible(false);

		boolean isValid = true;

		if (email.isEmpty())
		{
			emailValidationLabel.setText(I18n.get("login.email.required"));
			emailValidationLabel.setVisible(true);
			isValid = false;
		} else if (!isValidEmailFormat(email))
		{
			emailValidationLabel.setText(I18n.get("login.email.valid"));
			emailValidationLabel.setVisible(true);
			isValid = false;
		}

		if (password.isEmpty())
		{
			passwordValidationLabel.setVisible(true);
			isValid = false;
		}

		if (isValid)
		{
			try
			{
				userController.authenticate(email, password);
				errorLabel.setText("");
				mainLayout.showHomeScreen();
			} catch (InvalidInputException e)
			{
				errorLabel.setVisible(true);
				errorLabel.setText(e.getMessage());
			}
		}
	}

	private boolean isValidEmailFormat(String email)
	{
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0]+\\.[A-Za-z]{2,}$";
		return email.matches(emailRegex);
	}

}