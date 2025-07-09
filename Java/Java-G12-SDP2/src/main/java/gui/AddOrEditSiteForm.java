package gui;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import domain.SiteController;
import domain.UserController;
import dto.AddressDTO;
import dto.SiteDTOWithMachines;
import dto.UserDTO;
import exceptions.InformationRequired;
import exceptions.InformationRequiredExceptionSite;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import util.AuthenticationUtil;
import util.I18n;
import util.RequiredElementSite;
import util.Role;
import util.Status;

public class AddOrEditSiteForm extends AddOrEditAbstract
{
    private SiteDTOWithMachines site;

    private TextField siteNameField;
    private TextField streetField, houseNumberField, postalCodeField, cityField;
    private Map<String, UserDTO> employeeMap;
    private ComboBox<String> employeeBox;
    private ComboBox<Status> statusBox;

    private Label siteNameError, employeeError;
    private Label streetError, houseNumberError, postalCodeError, cityError;
    private Label statusError;

    public AddOrEditSiteForm(MainLayout mainLayout, int siteId)
    {
        super(mainLayout, false);
        this.site = siteController.getSite(siteId);
    }

    public AddOrEditSiteForm(MainLayout mainLayout)
    {
        super(mainLayout, true);
    }

    @Override
    protected void initializeFields()
    {
    	errorLabel = createErrorLabel();
        siteNameError = createErrorLabel();
        employeeError = createErrorLabel();
        streetError = createErrorLabel();
        houseNumberError = createErrorLabel();
        postalCodeError = createErrorLabel();
        cityError = createErrorLabel();
        statusError = createErrorLabel();
        
        siteNameField = new TextField();
        streetField = new TextField();
        houseNumberField = new TextField();
        postalCodeField = new TextField();
        cityField = new TextField();
        
        statusBox = new ComboBox<>();
        
        // Initialiseren van ComboBoxen
        List<UserDTO> verantwoordelijken = userController.getAllVerantwoordelijken();
        
        employeeMap = verantwoordelijken.stream()
            .collect(Collectors.toMap(
                t -> String.format("%d %s %s", t.id(), t.firstName(), t.lastName()),
                t -> t
            ));
        employeeBox = new ComboBox<>();
        employeeBox.getItems().addAll(employeeMap.keySet());
        employeeBox.setPromptText(I18n.get("site-add.select-site-manager"));
        employeeBox.setPrefWidth(200);
        
        statusBox.getItems().addAll(Status.values());
        statusBox.setPromptText(I18n.get("site-add.change-status"));
        statusBox.setPrefWidth(200);
    }

    @Override
    protected VBox createLeftBox()
    {
        VBox leftBox = new VBox(20);
        leftBox.setAlignment(Pos.TOP_LEFT);
        leftBox.setMinWidth(400);
        leftBox.setMaxWidth(400);

        leftBox.getChildren().addAll(createSiteNameField(), createComboBoxSection());

        return leftBox;
    }

    @Override
    protected VBox createRightBox()
    {
        VBox rightBox = new VBox(20);
        rightBox.setAlignment(Pos.TOP_LEFT);
        rightBox.setMinWidth(400);
        rightBox.setMaxWidth(400);

        rightBox.getChildren().addAll(createAddressFieldsSection());

        return rightBox;
    }

    private GridPane createSiteNameField()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        Label sectionLabel = new Label(I18n.get("site-add.site-name"));

        sectionLabel.getStyleClass().add("section-label");
        pane.add(sectionLabel, 0, 0);

        siteNameField.setPrefWidth(200);

        pane.add(new Label("Site:"), 0, 1);
        pane.add(siteNameField, 1, 1);
        pane.add(siteNameError, 1, 2);

        return pane;
    }

    private Node createComboBoxSection()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        String labelString = isNew ? I18n.get("site-add.site-manager") : I18n.get("site-add.site-manager-and-status");

        Label sectionLabel = new Label(labelString);

        sectionLabel.getStyleClass().add("section-label");
        pane.add(sectionLabel, 0, 0, 2, 1);

        int row = 1;
        pane.add(new Label(I18n.get("site-add.site-manager")+":"), 0, row);
        pane.add(employeeBox, 1, row++);
        pane.add(employeeError, 1, row++);

        if (!isNew)
        {
            pane.add(new Label(I18n.get("site-add.status")), 0, row);
            pane.add(statusBox, 1, row++);
            pane.add(statusError, 1, row++);
        }

        return pane;
    }

    private GridPane createAddressFieldsSection()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        Label sectionLabel = new Label(I18n.get("site-add.address-data"));
        sectionLabel.getStyleClass().add("section-label");
        pane.add(sectionLabel, 0, 0, 2, 1);

        streetField.setPrefWidth(200);
        houseNumberField.setPrefWidth(200);
        postalCodeField.setPrefWidth(200);
        cityField.setPrefWidth(200);

        int row = 1;
        pane.add(new Label(I18n.get("site-add.address.street")), 0, row);
        pane.add(streetField, 1, row++);
        pane.add(streetError, 1, row++);

        pane.add(new Label(I18n.get("site-add.address.number")), 0, row);
        pane.add(houseNumberField, 1, row++);
        pane.add(houseNumberError, 1, row++);

        pane.add(new Label(I18n.get("site-add.address.zip")), 0, row);
        pane.add(postalCodeField, 1, row++);
        pane.add(postalCodeError, 1, row++);

        pane.add(new Label(I18n.get("site-add.address-city")), 0, row);
        pane.add(cityField, 1, row++);
        pane.add(cityError, 1, row++);

        return pane;
    }

    @Override
    protected void fillData()
    {
    	Platform.runLater(() -> {
    		siteNameField.setText(site.siteName());

            AddressDTO address = site.address();
            if (address != null)
            {
                streetField.setText(address.street());
                houseNumberField.setText(String.valueOf(address.number()));
                postalCodeField.setText(String.valueOf(address.postalcode()));
                cityField.setText(address.city());
            }

            // Formatteer de waarde op dezelfde manier als in de map
            String key = String.format("%d %s %s", 
                site.verantwoordelijke().id(),
                site.verantwoordelijke().firstName(),
                site.verantwoordelijke().lastName());
                
            // Controleer of de key bestaat in de map
            if (employeeMap.containsKey(key)) {
                employeeBox.setValue(key);
            }
            
            statusBox.setValue(site.status());
    	});
    }

    @Override
    protected void save()
    {
        resetErrorLabels();

        if (AuthenticationUtil.hasRole(Role.VERANTWOORDELIJKE) || AuthenticationUtil.hasRole(Role.ADMINISTRATOR))
        {
            try
            {
                // Get the selected employee
                String selectedEmployee = employeeBox.getValue();
                int employeeId = (selectedEmployee != null && employeeMap.containsKey(selectedEmployee))
                    ? employeeMap.get(selectedEmployee).id()
                    : 0;
                
                if (isNew)
                {
                    siteController.createSite(siteNameField.getText(), streetField.getText(), houseNumberField.getText(),
                            postalCodeField.getText(), cityField.getText(), employeeId);
                } else
                {
                    Status status = statusBox.getValue();
                    
                    siteController.updateSite(site.id(), siteNameField.getText(), streetField.getText(), houseNumberField.getText(),
                            postalCodeField.getText(), cityField.getText(), employeeId,
                            status);
                }

                navigateBack();
            } catch (InformationRequiredExceptionSite e)
            {
                handleInformationRequiredException(e);
            } catch (NumberFormatException e)
            {
                showError(I18n.get("site-add.address.unique"));
            } catch (Exception e)
            {
                showError(I18n.get("error") + e.getMessage());
                e.printStackTrace();
            }
        } else
        {
            mainLayout.showNotAllowedAlert();
        }
    }

    @Override
    protected void navigateBack()
    {
        mainLayout.showSitesList();
    }

    @Override
    protected String getTitleText()
    {
        return isNew ? I18n.get("site-add.add-message") : I18n.get("site-add.edit-message");
    }

    @Override
    protected void handleInformationRequiredException(Exception e)
    {
        if (e instanceof InformationRequired) {
        	InformationRequired exception = (InformationRequired) e;
            exception.getRequiredElements().forEach((field, requiredElement) -> {
                String errorMessage = requiredElement.getMessage();
                showFieldError(field, errorMessage);
            });
        }
    }

    @Override
    protected void showFieldError(String fieldName, String message)
    {
        switch (fieldName)
        {
        case "siteName":
            siteNameError.setText(message);
            break;
        case "employee":
            employeeError.setText(message);
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
        case "status":
            statusError.setText(message);
            break;
        default:
            errorLabel.setText(message);
        }
    }

    @Override
    protected void resetErrorLabels()
    {
        super.resetErrorLabels();
        siteNameError.setText("");
        employeeError.setText("");
        streetError.setText("");
        houseNumberError.setText("");
        postalCodeError.setText("");
        cityError.setText("");
        statusError.setText("");
    }
}