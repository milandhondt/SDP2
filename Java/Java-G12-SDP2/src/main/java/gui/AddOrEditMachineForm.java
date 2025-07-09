package gui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import dto.MachineDTO;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;
import exceptions.InformationRequired;
import exceptions.InformationRequiredExceptionMachine;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import util.I18n;
import util.ItemI18n;
import util.MachineStatus;
import util.ProductionStatus;

public class AddOrEditMachineForm extends AddOrEditAbstract
{
    private MachineDTO machineDTO;

    private Label codeError, locationError, productInfoError;
    private Label errorSite, errorTechnician, errorMachineStatus, errorProductionStatus;
    private Label errorFutureMaintenance;

    private TextField codeField, locationField, productInfoField;

    private ComboBox<SiteDTOWithoutMachines> siteBox;
    private ComboBox<UserDTO> technicianBox;
    private ComboBox<ItemI18n<MachineStatus>> machineStatusBox;
    private ComboBox<ItemI18n<ProductionStatus>> productionStatusBox;

    private DatePicker futureMaintenance;

    public AddOrEditMachineForm(MainLayout mainLayout, int machineId)
    {
        super(mainLayout, false);
        this.machineDTO = machineController.getMachineById(machineId);
    }
    
    public AddOrEditMachineForm(MainLayout mainLayout)
    {
        super(mainLayout, true);
    }

    @Override
    protected void initializeFields()
    {
        codeField = new TextField();
        locationField = new TextField();
        productInfoField = new TextField();
        siteBox = new ComboBox<SiteDTOWithoutMachines>();
        technicianBox = new ComboBox<UserDTO>();
        machineStatusBox = new ComboBox<ItemI18n<MachineStatus>>();
        productionStatusBox = new ComboBox<ItemI18n<ProductionStatus>>();
        futureMaintenance = new DatePicker();
        futureMaintenance.setEditable(false);

        errorLabel = createErrorLabel();
        codeError = createErrorLabel();
        locationError = createErrorLabel();
        productInfoError = createErrorLabel();
        errorSite = createErrorLabel();
        errorTechnician = createErrorLabel();
        errorMachineStatus = createErrorLabel();
        errorProductionStatus = createErrorLabel();
        errorFutureMaintenance = createErrorLabel();
        
        // Initialiseren van comboboxen
        siteBox.getItems().addAll(siteController.getSitesWithoutMachines());
        siteBox.setPromptText(I18n.get("add-machine.select-site"));
        siteBox.setPrefWidth(200);

        siteBox.setCellFactory(param -> new ListCell<SiteDTOWithoutMachines>()
        {
            @Override
            protected void updateItem(SiteDTOWithoutMachines item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setText(null);
                } else
                {
                    setText(item.siteName());
                }
            }
        });

        siteBox.setButtonCell(new ListCell<SiteDTOWithoutMachines>()
        {
            @Override
            protected void updateItem(SiteDTOWithoutMachines item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setText(null);
                } else
                {
                    setText(item.siteName());
                }
            }
        });

        technicianBox.getItems().addAll(userController.getAllTechniekers());
        technicianBox.setPromptText(I18n.get("add-machine.select-technician"));
        technicianBox.setPrefWidth(200);

        technicianBox.setCellFactory(param -> new ListCell<UserDTO>()
        {
            @Override
            protected void updateItem(UserDTO technician, boolean empty)
            {
                super.updateItem(technician, empty);
                if (empty || technician == null)
                {
                    setText(null);
                } else
                {
                    setText(technician.firstName() + " " + technician.lastName());
                }
            }
        });

        technicianBox.setButtonCell(new ListCell<UserDTO>()
        {
            @Override
            protected void updateItem(UserDTO technician, boolean empty)
            {
                super.updateItem(technician, empty);
                if (empty || technician == null)
                {
                    setText(null);
                } else
                {
                    setText(technician.firstName() + " " + technician.lastName());
                }
            }
        });
        
        machineStatusBox.getItems().addAll(
        		Stream.of(MachineStatus.values())
        		.map((s) -> new ItemI18n<MachineStatus>(s, I18n.convertStatus(s.toString())))
        		.collect(Collectors.toList()));
        machineStatusBox.setPromptText(I18n.get("add-machine.select-machinestatus"));
        machineStatusBox.setPrefWidth(200);

        productionStatusBox.getItems().addAll(
        		Stream.of(ProductionStatus.values())
        		.map((s) -> new ItemI18n<ProductionStatus>(s, I18n.convertStatus(s.toString())))
        		.collect(Collectors.toList()));
        productionStatusBox.setPromptText(I18n.get("add-machine.select-productionstatus"));
        productionStatusBox.setPrefWidth(200);
    }

    @Override
    protected VBox createLeftBox()
    {
        VBox leftBox = new VBox(20);
        leftBox.setAlignment(Pos.TOP_LEFT);
        leftBox.setMinWidth(400);
        leftBox.setMaxWidth(400);

        leftBox.getChildren().addAll(createTextFields(), createDatePicker());
        
        return leftBox;
    }

    @Override
    protected VBox createRightBox()
    {
        VBox rightBox = new VBox(20);
        rightBox.setAlignment(Pos.TOP_LEFT);
        rightBox.setMinWidth(400);
        rightBox.setMaxWidth(400);

        rightBox.getChildren().addAll(createInfoBox(), createComboBoxSection());
        
        return rightBox;
    }

    private Node createDatePicker()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        Label sectionLabel = new Label(I18n.get("maintenance"));

        sectionLabel.getStyleClass().add("section-label");
        pane.add(sectionLabel, 0, 0);

        codeField.setPrefWidth(200);

        int row = 1;

        pane.add(new Label(I18n.get("add-machine.next-maintenance")), 0, row);
        pane.add(futureMaintenance, 1, row++);
        pane.add(errorFutureMaintenance, 1, row++);

        return pane;
    }

    private Node createTextFields()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        Label sectionLabel = new Label(I18n.get("add-machine.site-info"));

        sectionLabel.getStyleClass().add("section-label");
        pane.add(sectionLabel, 0, 0);

        codeField.setPrefWidth(200);
        locationField.setPrefWidth(200);
        productInfoField.setPrefWidth(200);

        int row = 1;

        pane.add(new Label(I18n.get("add-machine.code")), 0, row);
        pane.add(codeField, 1, row++);
        pane.add(codeError, 1, row++);

        pane.add(new Label(I18n.get("add-machine.location")), 0, row);
        pane.add(locationField, 1, row++);
        pane.add(locationError, 1, row++);

        pane.add(new Label(I18n.get("add-machine.product-info")), 0, row);
        pane.add(productInfoField, 1, row++);
        pane.add(productInfoError, 1, row++);

        return pane;
    }

    private Node createInfoBox()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        Label sectionLabel1 = new Label(I18n.get("add-machine.site-technician"));
        sectionLabel1.getStyleClass().add("section-label");
        pane.add(sectionLabel1, 0, 0, 2, 1);

        int row = 1;
        pane.add(new Label(I18n.get("add-machine.site")), 0, row);
        pane.add(siteBox, 1, row++);
        pane.add(errorSite, 1, row++);

        pane.add(new Label(I18n.get("add-machine.technician")), 0, row);
        pane.add(technicianBox, 1, row++);
        pane.add(errorTechnician, 1, row++);

        return pane;
    }

    private Node createComboBoxSection()
    {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(10);

        Label sectionLabel = new Label(I18n.get("add-machine.statuses"));
        sectionLabel.getStyleClass().add("section-label");
        pane.add(sectionLabel, 0, 0, 2, 1);

        int row = 1;

        pane.add(new Label(I18n.get("add-machine.machinestatus")), 0, row);
        pane.add(machineStatusBox, 1, row++);
        pane.add(errorMachineStatus, 1, row++);

        pane.add(new Label(I18n.get("add-machine.productionstatus")), 0, row);
        pane.add(productionStatusBox, 1, row++);
        pane.add(errorProductionStatus, 1, row++);

        return pane;
    }

    @Override
    protected void fillData()
    {
    	Platform.runLater(() -> {
    		 codeField.setText(machineDTO.code());
             locationField.setText(machineDTO.location());
             productInfoField.setText(machineDTO.productInfo());
             technicianBox.setValue(machineDTO.technician());
             machineStatusBox.setValue(new ItemI18n<MachineStatus>(machineDTO.machineStatus(), I18n.convertStatus(machineDTO.machineStatus().toString())));
             productionStatusBox
             .setValue(new ItemI18n<ProductionStatus>(machineDTO.productionStatus(), I18n.convertStatus(machineDTO.productionStatus().toString())));
             futureMaintenance.setValue(machineDTO.futureMaintenance());
             siteBox.setValue(machineDTO.site());
    	});
    }

    @Override
    protected void save()
    {
        resetErrorLabels();

        try
        {
        	MachineStatus selectedMachineStatus = machineStatusBox.getValue() != null
        		    ? machineStatusBox.getValue().getValue()
        		    : null;

        	ProductionStatus selectedProductionStatus = productionStatusBox.getValue() != null
        		    ? productionStatusBox.getValue().getValue()
        		    : null;
            if (isNew)
            {
                machineController.createMachine(siteBox.getValue(), technicianBox.getValue(), codeField.getText(),
                        selectedMachineStatus,
                        selectedProductionStatus,
                        locationField.getText(),
                        productInfoField.getText(), futureMaintenance.getValue());
                mainLayout.getServices().getSiteController().notifyObservers(I18n.get("add-machine.added"));
            } else
            {
                machineController.updateMachine(machineDTO.id(), siteBox.getValue(), technicianBox.getValue(),
                        codeField.getText(), 
                        selectedMachineStatus,
                        selectedProductionStatus,
                        locationField.getText(), productInfoField.getText(), futureMaintenance.getValue());
                mainLayout.getServices().getSiteController().notifyObservers(I18n.get("add-machine.edited"));
            }

            navigateBack();
        } catch (InformationRequiredExceptionMachine e)
        {
            handleInformationRequiredException(e);
        } catch (Exception e)
        {
            showError(I18n.get("error") + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void navigateBack()
    {
        mainLayout.showMachineScreen();
    }

    @Override
    protected String getTitleText()
    {
        return isNew ? I18n.get("add-machine-add") : I18n.get("add-machine-edit");
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
        case "code":
            codeError.setText(message);
            break;
        case "machineStatus":
            errorMachineStatus.setText(message);
            break;
        case "productionStatus":
            errorProductionStatus.setText(message);
            break;
        case "location":
            locationError.setText(message);
            break;
        case "productInfo":
            productInfoError.setText(message);
            break;
        case "site":
            errorSite.setText(message);
            break;
        case "technician":
            errorTechnician.setText(message);
            break;
        case "futureMaintenance":
            errorFutureMaintenance.setText(message);
            break;
        default:
            errorLabel.setText(message);
        }
    }

    @Override
    protected void resetErrorLabels()
    {
        super.resetErrorLabels();
        codeError.setText("");
        locationError.setText("");
        productInfoError.setText("");
        errorSite.setText("");
        errorTechnician.setText("");
        errorMachineStatus.setText("");
        errorProductionStatus.setText("");
        errorFutureMaintenance.setText("");
    }
}