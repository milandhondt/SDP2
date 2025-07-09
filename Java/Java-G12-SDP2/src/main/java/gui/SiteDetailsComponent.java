package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import domain.SiteController;
import dto.MachineDTO;
import dto.SiteDTOWithMachines;
import dto.UserDTO;
import interfaces.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import util.*;

public class SiteDetailsComponent extends VBox implements Observer {
    private final MainLayout mainLayout;
    private final SiteController sc;
    private final int siteId;
    private final SiteDTOWithMachines site;

    private final TableView<MachineDTO> table;
    private final TextField searchField;
    private final ComboBox<String> locationFilter;
    private final ComboBox<ItemI18n<MachineStatus>> statusFilter;
    private final ComboBox<ItemI18n<ProductionStatus>> productionStatusFilter;
    private final ComboBox<String> technicianFilter;

    private List<MachineDTO> allMachines = new ArrayList<>();
    private List<MachineDTO> filteredMachines = new ArrayList<>();

    private int itemsPerPage = 10;
    private int currentPage = 0;
    private int totalPages = 1;
    private Pagination pagination;

    public SiteDetailsComponent(MainLayout mainLayout, int siteId) {
        this.mainLayout = mainLayout;
        this.sc = mainLayout.getServices().getSiteController();
        this.siteId = siteId;
        this.site = sc.getSite(siteId);
        this.sc.addObserver(this);

        this.table = new TableView<>();
        this.searchField = new TextField();
        this.locationFilter = new ComboBox<>();
        this.statusFilter = new ComboBox<>();
        this.productionStatusFilter = new ComboBox<>();
        this.technicianFilter = new ComboBox<>();

        initializeGUI();
        loadMachines();
    }

    private void initializeGUI() {
        this.getStylesheets().add(getClass().getResource("/css/tablePane.css").toExternalForm());
        this.setSpacing(20);

        VBox titleSection = createTitleSection();
        VBox tableSection = createTableSection();

        this.getChildren().addAll(titleSection, tableSection);
    }

    private VBox createTitleSection() {
        HBox windowHeader = createWindowHeader();
        UserDTO verantwoordelijke = site.verantwoordelijke();

        HBox infoBox1 = new CustomInformationBox(I18n.get("site-details-infobox"));
        HBox infoBox2 = new CustomInformationBox(
            "%s %s %s".formatted(I18n.get("report.responsible"), verantwoordelijke.firstName(), verantwoordelijke.lastName()));

        return new VBox(10, windowHeader, infoBox1, infoBox2);
    }

    private HBox createWindowHeader() {
        Button backButton = new Button();
        backButton.setGraphic(new FontIcon("fas-arrow-left"));
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> mainLayout.showSitesList());

        Label title = new Label(I18n.get("site-details-site-details"));
        title.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button(I18n.get("site-details.add-machine"));
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> openAddMachineForm());

        return new HBox(10, backButton, title, spacer, addButton);
    }

    private VBox createTableSection() {
        HBox filterBox = createTableFilters();
        createTableColumns();

        table.setPlaceholder(new Label(I18n.get("site-details-no-machines")));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(index -> new HBox());
        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            currentPage = newIdx.intValue();
            updateTableItems();
        });

        VBox tableWithPagination = new VBox(10, table, pagination);
        VBox.setVgrow(table, Priority.ALWAYS);

        return new VBox(10, filterBox, tableWithPagination);
    }

    private HBox createTableFilters() {
        searchField.setPromptText(I18n.get("search"));
        searchField.setMaxWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

        setupComboBox(locationFilter, I18n.get("machine.location"), 150);
        setupComboBox(statusFilter, I18n.get("machine.status"), 150);
        setupComboBox(productionStatusFilter, I18n.get("machine.productionstatus"), 150);
        setupComboBox(technicianFilter, I18n.get("user.role.technician"), 200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox pageSelector = new HBox(10, new Label(I18n.get("amount-per-page")), createItemsPerPageComboBox());
        pageSelector.setAlignment(Pos.CENTER_RIGHT);

        return new HBox(10, searchField, locationFilter, statusFilter, productionStatusFilter, technicianFilter, spacer, pageSelector);
    }

    private <T> void setupComboBox(ComboBox<T> comboBox, String promptText, int width) {
        comboBox.setPromptText(promptText);
        comboBox.setPrefWidth(width);
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTable());
    }

    private ComboBox<Integer> createItemsPerPageComboBox() {
        ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(10, 20, 50, 100));
        combo.setValue(itemsPerPage);
        combo.setOnAction(e -> updateItemsPerPage(combo.getValue()));
        return combo;
    }

    private void createTableColumns() {
        table.getColumns().setAll(
            createColumn(I18n.get("number"), m -> new SimpleIntegerProperty(m.id())),
            createColumn(I18n.get("machine.location"), m -> new SimpleStringProperty(m.location())),
            createColumn(I18n.get("machine.status"), m -> new SimpleStringProperty(I18n.convertStatus(m.machineStatus().toString()))),
            createColumn(I18n.get("machine.productionstatus"), m -> new SimpleStringProperty(I18n.convertStatus(m.productionStatus().toString()))),
            createColumn(I18n.get("user.role.technician"), m -> new SimpleStringProperty(m.technician() != null ? m.technician().firstName() : "")),
            createButtonColumn("", "fas-pen", this::openEditMachineForm),
            createTextButtonColumn(I18n.get("site-details-view"), this::showMachineDetails)
        );
    }

    private <T> TableColumn<MachineDTO, T> createColumn(String title, Function<MachineDTO, ObservableValue<T>> mapper) {
        TableColumn<MachineDTO, T> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> mapper.apply(cellData.getValue()));
        return col;
    }

    private TableColumn<MachineDTO, Void> createButtonColumn(String title, String iconCode, java.util.function.Consumer<MachineDTO> action) {
        TableColumn<MachineDTO, Void> col = new TableColumn<>(title);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setGraphic(new FontIcon(iconCode));
                btn.setBackground(Background.EMPTY);
                btn.setOnAction(e -> {
                    MachineDTO machine = getTableRow().getItem();
                    if (machine != null) action.accept(machine);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow().getItem() == null ? null : btn);
            }
        });
        return col;
    }

    private TableColumn<MachineDTO, String> createTextButtonColumn(String buttonText, java.util.function.Consumer<MachineDTO> action) {
        TableColumn<MachineDTO, String> col = new TableColumn<>("");
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button(buttonText);
            {
                btn.setOnAction(e -> {
                    MachineDTO machine = getTableRow().getItem();
                    if (machine != null) action.accept(machine);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow().getItem() == null ? null : btn);
            }
        });
        return col;
    }

    private void updateItemsPerPage(int count) {
        itemsPerPage = count;
        currentPage = 0;
        updatePagination();
        updateTableItems();
    }

    private void updatePagination() {
        totalPages = Math.max(1, (int) Math.ceil((double) filteredMachines.size() / itemsPerPage));
        pagination.setPageCount(totalPages);
        pagination.setCurrentPageIndex(Math.min(currentPage, totalPages - 1));
    }

    private void updateTableItems() {
        int fromIndex = currentPage * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredMachines.size());
        table.setItems(FXCollections.observableArrayList(filteredMachines.subList(fromIndex, toIndex)));
    }

    private void loadMachines() {
        try {
            SiteDTOWithMachines currentSite = sc.getSite(siteId);
            allMachines = currentSite.machines().stream().collect(Collectors.toList());
            filteredMachines = new ArrayList<>(allMachines);
            updateFilterOptions();
            updateTable(filteredMachines);
        } catch (Exception e) {
            e.printStackTrace();
            allMachines.clear();
            filteredMachines.clear();
            updateFilterOptions();
            updateTable(filteredMachines);
        }
    }

    private void filterTable() {
        String query = searchField.getText().toLowerCase().trim();
        String location = locationFilter.getValue();
        String status = statusFilter.getValue() != null ? statusFilter.getValue().getValue().name() : null;
        String prodStatus = productionStatusFilter.getValue() != null ? productionStatusFilter.getValue().getValue().toString() : null;
        String technician = technicianFilter.getValue();

        filteredMachines = sc.getFilteredMachines(siteId, query, location, status, prodStatus, technician);
        currentPage = 0;
        updatePagination();
        updateTableItems();
    }

    private void updateTable(List<MachineDTO> machines) {
        filteredMachines = new ArrayList<>(machines);
        currentPage = 0;
        updatePagination();
        updateTableItems();
    }

    private void updateFilterOptions() {
        locationFilter.setItems(FXCollections.observableArrayList(
            allMachines.stream().map(MachineDTO::location).distinct().sorted().collect(Collectors.toCollection(() -> {
                List<String> l = new ArrayList<>(); l.add(null); return l;
            }))));

        statusFilter.setItems(FXCollections.observableArrayList(
            allMachines.stream().map(m -> new ItemI18n<>(m.machineStatus(), I18n.convertStatus(m.machineStatus().toString())))
                .distinct().collect(Collectors.toList())));

        productionStatusFilter.setItems(FXCollections.observableArrayList(
            allMachines.stream().map(m -> new ItemI18n<>(m.productionStatus(), I18n.convertStatus(m.productionStatus().toString())))
                .distinct().sorted().collect(Collectors.toCollection(() -> {
                    List<ItemI18n<ProductionStatus>> l = new ArrayList<>(); l.add(null); return l;
                }))));

        technicianFilter.setItems(FXCollections.observableArrayList(
            allMachines.stream().map(m -> m.technician() != null ? m.technician().firstName() : null)
                .filter(n -> n != null && !n.isEmpty()).distinct().sorted().collect(Collectors.toCollection(() -> {
                    List<String> l = new ArrayList<>(); l.add(null); return l;
                }))));
    }

    private void openAddMachineForm() {
        mainLayout.setContentAsync(() -> new AddOrEditMachineForm(mainLayout), true, false, CurrentPage.NONE);
    }

    private void openEditMachineForm(MachineDTO machine) {
        mainLayout.setContentAsync(() -> new AddOrEditMachineForm(mainLayout, machine.id()), true, false, CurrentPage.NONE);
    }

    private void showMachineDetails(MachineDTO machine) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("machine-details-alertbox"));
        alert.setHeaderText(I18n.get("machine-details-alertbox-headertext") + " " + machine.id());

        alert.setContentText(String.format("""
                %s: %d
                %s: %s
                %s: %s
                %s: %s
                %s: %s %s
                """,
                I18n.get("site-details-machine-details-alertbox-id"), machine.id(),
                I18n.get("site-details-machine-details-alertbox-location"), machine.location(),
                I18n.get("site-details-machine-details-alertbox-status"), I18n.convertStatus(machine.machineStatus().toString()).toLowerCase(),
                I18n.get("site-details-machine-details-alertbox-production-status"), I18n.convertStatus(machine.productionStatus().toString()).toLowerCase(),
                I18n.get("site-details-machine-details-alertbox-technician"),
                machine.technician() != null ? machine.technician().firstName() : "N/A",
                machine.technician() != null ? machine.technician().lastName() : ""
        ));
        alert.showAndWait();
    }

    @Override
    public void update(String message) {
        Platform.runLater(this::loadMachines);
    }
}