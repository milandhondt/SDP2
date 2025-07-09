package gui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.kordamp.ikonli.javafx.FontIcon;

import domain.FileInfo;
import domain.FileInfoController;
import domain.Maintenance;
import domain.MaintenanceController;
import dto.MaintenanceDTO;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.I18n;

public class MaintenanceDetailView extends BorderPane
{
	private MaintenanceController maintenanceController;
	private FileInfoController fileInfoController;
	private MaintenanceDTO currentMaintenance;
	private Stage primaryStage;

	private Label titleLabel;
	private Label machineInfoLabel;
	private VBox filesSection;
	private FlowPane filesContainer;
	private List<FileInfo> currentFiles;
	private ComboBox<String> fileTypeFilter;
	private ComboBox<String> sortOrder;
	private String currentFilter = I18n.get("files-filter-all");
	private String currentSort = I18n.get("maintenance-details-file-filter-date-new");
	private Label messageLabel;

	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	private final MainLayout mainLayout;

	public MaintenanceDetailView(MainLayout mainLayout, MaintenanceDTO maintenance)
	{
		this.mainLayout = mainLayout;
		this.maintenanceController = mainLayout.getServices().getMaintenanceController();
		this.fileInfoController = mainLayout.getServices().getFileInfoController();
		this.primaryStage = (Stage) mainLayout.getMainScene().getWindow();

		if (maintenance != null)
		{
			this.currentMaintenance = maintenance;
		}

		this.currentFiles = getFilesFromDatabase();

		getStylesheets().add(getClass().getResource("/css/maintenanceDetails.css").toExternalForm());
		getStyleClass().add("maintenance-details");

		initialize();
	}

	private List<FileInfo> getFilesFromDatabase()
	{
		if (currentMaintenance == null)
		{
			return new ArrayList<>();
		}

		return fileInfoController.getFilesForMaintenance(currentMaintenance.id());
	}

	private void initialize()
	{
		this.getStylesheets().add(getClass().getResource("/css/tablePane.css").toExternalForm());

		setStyle("-fx-background-color: #f5f5f5;");
		setPadding(new Insets(10, 30, 10, 30));

		createHeaderSection();

		createMaintenanceInfoSection();

		// Create files section if files exist
		if (currentFiles != null && !currentFiles.isEmpty())
		{
			createFilesSection();
		}
	}

	private void createHeaderSection()
	{

		Button backButton = new Button();
		FontIcon backIcon = new FontIcon("fas-arrow-left");
		backIcon.setIconSize(20);
		backButton.setGraphic(backIcon);
		backButton.getStyleClass().add("back-button");
		backButton.setOnAction(e ->
		{
			mainLayout.showMaintenanceList(currentMaintenance.machine());
		});

		titleLabel = new Label(I18n.get("maintenance-detail-maintenance"));
		titleLabel.getStyleClass().add("title-label");
		if (currentMaintenance != null)
		{
			titleLabel.setText(I18n.get("maintenance-detail-maintenance-title") + currentMaintenance.id());

			if (currentMaintenance.machine() != null)
			{
				machineInfoLabel = new Label(I18n.get("machine") + ": " + currentMaintenance.machine().code());
				machineInfoLabel.getStyleClass().add("machine-info-label");
			}
		}

		VBox titleVBox = new VBox(5);
		titleVBox.getChildren().add(titleLabel);
		if (machineInfoLabel != null)
		{
			titleVBox.getChildren().add(machineInfoLabel);
		}

		HBox titleBox = new HBox(10);
		titleBox.setAlignment(Pos.CENTER_LEFT);
		titleBox.getChildren().addAll(backButton, titleVBox);

		Button uploadButton = new Button(I18n.get("maintenance-detail-add-file"));
		FontIcon uploadIcon = new FontIcon("fas-upload");
		uploadIcon.setIconSize(16);
		uploadIcon.setIconColor(Color.WHITE);
		uploadButton.setGraphic(uploadIcon);
		uploadButton.getStyleClass().add("action-button");
		uploadButton.setOnAction(e -> uploadFiles());

		HBox actionsBox = new HBox(10);
		actionsBox.setAlignment(Pos.CENTER_RIGHT);
		actionsBox.getChildren().add(uploadButton);

		// Combine title and actions in a header bar
		BorderPane headerPane = new BorderPane();
		headerPane.setLeft(titleBox);
		headerPane.setRight(actionsBox);
		headerPane.setPadding(new Insets(0, 0, 20, 0));

		setTop(headerPane);
	}

	private void createMaintenanceInfoSection()
	{

		HBox infoBox = new CustomInformationBox(I18n.get("maintenance-detail.infobox"));
		VBox.setMargin(infoBox, new Insets(20, 0, 5, 0));

		messageLabel = new Label();
		messageLabel.getStyleClass().add("message-label");
		messageLabel.setWrapText(true);
		messageLabel.setVisible(false);
		messageLabel.setMaxWidth(Double.MAX_VALUE);
		VBox.setMargin(messageLabel, new Insets(0, 0, 5, 0));

		GridPane table = createMaintenanceTable();

		VBox contentBox = new VBox(5);
		contentBox.getChildren().addAll(infoBox, messageLabel, table);

		setCenter(contentBox);
	}

	private GridPane createMaintenanceTable()
	{
		GridPane table = new GridPane();
		table.getStyleClass().add("maintenance-table");

		String[] headers =
		{ 
				I18n.get("maintenance-detail-header-maintenance-number"),
				I18n.get("maintenance-detail-header-execution-date"),
				I18n.get("maintenance-detail-header-start-time"), 
				I18n.get("maintenance-detail-header-end-time"), 
				I18n.get("maintenance-detail-header-technician"), 
				I18n.get("maintenance-detail-header-reason"), 
				I18n.get("maintenance-detail-header-remarks"),
				I18n.get("maintenance-detail-header-status")
		};

		for (int i = 0; i < headers.length; i++)
		{
			Label headerLabel = new Label(headers[i]);
			headerLabel.getStyleClass().add("table-header");
			headerLabel.setMaxWidth(Double.MAX_VALUE);
			headerLabel.setAlignment(Pos.CENTER_LEFT);

			table.add(headerLabel, i, 0);
			GridPane.setHgrow(headerLabel, Priority.SOMETIMES);
		}

		Region separator = new Region();
		separator.setStyle("-fx-background-color: #e0e0e0; -fx-min-height: 1px; -fx-max-height: 1px;");
		separator.setMaxWidth(Double.MAX_VALUE);
		table.add(separator, 0, 1, headers.length, 1);

		if (currentMaintenance != null)
		{

			Label idLabel = new Label(String.valueOf(currentMaintenance.id()));
			idLabel.getStyleClass().add("table-cell");
			table.add(idLabel, 0, 2);

			Label executionDateLabel = new Label(currentMaintenance.executionDate() != null
					? currentMaintenance.executionDate().format(dateFormatter)
					: "");
			executionDateLabel.getStyleClass().add("table-cell");
			table.add(executionDateLabel, 1, 2);

			Label startTimeLabel = new Label(
					currentMaintenance.startDate() != null ? currentMaintenance.startDate().format(timeFormatter) : "");
			startTimeLabel.getStyleClass().add("table-cell");
			table.add(startTimeLabel, 2, 2);

			Label endDateLabel = new Label(
					currentMaintenance.endDate() != null ? currentMaintenance.endDate().format(timeFormatter) : "");
			endDateLabel.getStyleClass().add("table-cell");
			table.add(endDateLabel, 3, 2);

			Label technicianLabel = new Label(currentMaintenance.technician() != null
					? currentMaintenance.technician().firstName() + " " + currentMaintenance.technician().lastName()
					: "");
			technicianLabel.getStyleClass().add("table-cell");
			table.add(technicianLabel, 4, 2);

			Label reasonLabel = new Label(currentMaintenance.reason() != null ? currentMaintenance.reason() : "");
			reasonLabel.getStyleClass().add("table-cell");
			table.add(reasonLabel, 5, 2);

			Label commentsLabel = new Label(currentMaintenance.comments() != null ? currentMaintenance.comments() : "");
			commentsLabel.getStyleClass().add("table-cell");
			table.add(commentsLabel, 6, 2);

			Label statusLabel = new Label(
					currentMaintenance.status() != null ? I18n.convertStatus(currentMaintenance.status().toString()) : "");
			statusLabel.getStyleClass().add("table-cell");
			table.add(statusLabel, 7, 2);
		}

		return table;
	}

	private void createFilesSection()
	{
		filesSection = new VBox(10);
		filesSection.getStyleClass().add("files-section");

		Label filesHeader = new Label("Bestanden");
		filesHeader.getStyleClass().add("files-header");

		HBox controlsBox = createFilterControls();
		controlsBox.getStyleClass().add("filter-controls");

		filesContainer = new FlowPane();
		filesContainer.getStyleClass().add("files-container");

		refreshFilesDisplay();

		ScrollPane scrollPane = new ScrollPane(filesContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.getStyleClass().add("files-scroll-pane");

		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		scrollPane.setPrefViewportHeight(Integer.MAX_VALUE);
		scrollPane.setMaxHeight(Double.MAX_VALUE);
		scrollPane.setMinHeight(400);

		filesSection.getChildren().addAll(filesHeader, controlsBox, scrollPane);

		VBox mainContent = new VBox(20);
		mainContent.getChildren().addAll((VBox) getCenter(), filesSection);

		setCenter(mainContent);
	}

	private HBox createFilterControls()
	{
		HBox controlsBox = new HBox(10);
		controlsBox.setAlignment(Pos.CENTER_LEFT);

		Label filterLabel = new Label(I18n.get("maintenance-detail-filter-file"));
		fileTypeFilter = new ComboBox<>();
		fileTypeFilter.getItems().addAll("Alle", "PDF", "Foto");
		fileTypeFilter.setValue(currentFilter);
		fileTypeFilter.setOnAction(e ->
		{
			currentFilter = fileTypeFilter.getValue();
			refreshFilesDisplay();
		});

		Label sortLabel = new Label(I18n.get("maintenance-detail-sort-by"));
		sortOrder = new ComboBox<>();
		sortOrder.getItems().addAll(
				I18n.get("maintenance-details-file-filter-date-new"),
				I18n.get("maintenance-details-file-filter-date-old"),
				I18n.get("maintenance-details-file-filter-date-size-biggest"),
				I18n.get("maintenance-details-file-filter-date-size-smallest"),
				I18n.get("maintenance-details-file-filter-date-name-az"),
				I18n.get("maintenance-details-file-filter-date-name-za"));
		sortOrder.setValue(currentSort);
		sortOrder.setOnAction(e ->
		{
			currentSort = sortOrder.getValue();
			refreshFilesDisplay();
		});

		controlsBox.getChildren().addAll(filterLabel, fileTypeFilter, sortLabel, sortOrder);
		return controlsBox;
	}

	private void refreshFilesDisplay()
	{
		filesContainer.getChildren().clear();

		List<FileInfo> filteredFiles = currentFiles.stream().filter(file ->
		{
			if (currentFilter.equals(I18n.get("files-filter-all")))
				return true;
			String fileType = file.getType().toLowerCase();
			switch (currentFilter)
			{
			case "PDF":
				return fileType.equals("pdf");
			case "Foto":
				return fileType.equals("image");
			case "Video":
				return fileType.equals("video");
			default:
				return true;
			}
		}).collect(Collectors.toList());

		filteredFiles.sort((f1, f2) ->
		{
			if(currentSort.equals(I18n.get("maintenance-details-file-filter-date-new"))){
				return f2.getUploadDate().compareTo(f1.getUploadDate());
			}
			if(currentSort.equals(I18n.get("maintenance-details-file-filter-date-old"))) {
				return f1.getUploadDate().compareTo(f2.getUploadDate());
			}
			if(currentSort.equals(I18n.get("maintenance-details-file-filter-date-size-biggest"))) {
				return Long.compare(f2.getSize(), f1.getSize());
			}
			if(currentSort.equals(I18n.get("maintenance-details-file-filter-date-size-smallest"))) {
				return Long.compare(f1.getSize(), f2.getSize());
			}
			if(currentSort.equals(I18n.get("maintenance-details-file-filter-date-name-az"))) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
			if(currentSort.equals(I18n.get("maintenance-details-file-filter-date-name-za"))) {
				return f2.getName().compareToIgnoreCase(f1.getName());
			}
			return 0;
		});

		for (FileInfo file : filteredFiles)
		{
			filesContainer.getChildren().add(createFileBox(file));
		}
	}

	private void refreshFilesSection()
	{
		refreshFilesDisplay();

		if (currentFiles.isEmpty())
		{
			VBox mainContent = (VBox) getCenter();
			mainContent.getChildren().remove(filesSection);
			filesSection = null;
		}
	}

	private VBox createFileBox(FileInfo fileInfo)
	{
		VBox fileBox = new VBox();
		fileBox.getStyleClass().add("file-box");

		StackPane previewContainer = new StackPane();
		previewContainer.getStyleClass().add("image-container");

		String fileType = fileInfo.getType();

		if (fileType.equals("pdf"))
		{
			try
			{
				byte[] pdfContent = fileInfoController.getFileContent(fileInfo);
				if (pdfContent != null)
				{
					// Load the first page of the PDF
					PDDocument document = Loader.loadPDF(pdfContent);
					PDFRenderer pdfRenderer = new PDFRenderer(document);

					BufferedImage image = pdfRenderer.renderImageWithDPI(0, 150); // Increased DPI for better quality

					// Convert to JavaFX Image
					Image pdfImage = SwingFXUtils.toFXImage(image, null);
					ImageView pdfImageView = new ImageView(pdfImage);

					// Create a container to center the image
					StackPane imageContainer = new StackPane();
					imageContainer.setStyle("-fx-background-color: white;");
					imageContainer.setPrefSize(217, 160);

					// Set dimensions while preserving aspect ratio
					pdfImageView.setFitWidth(200); // Slightly smaller than container to allow for padding
					pdfImageView.setFitHeight(140);
					pdfImageView.setPreserveRatio(true);
					pdfImageView.setSmooth(true);

					StackPane.setAlignment(pdfImageView, Pos.CENTER);

					imageContainer.getChildren().add(pdfImageView);

					previewContainer.getChildren().add(imageContainer);

					document.close();
				} else
				{
					throw new IOException("No PDF content available");
				}
			} catch (Exception e)
			{
				// Fallback to icon if preview fails
				VBox pdfPreview = new VBox(10);
				pdfPreview.setAlignment(Pos.CENTER);

				FontIcon pdfIcon = new FontIcon("fas-file-pdf");
				pdfIcon.setIconSize(60);
				pdfIcon.setIconColor(Color.web("#333333"));

				Label pdfName = new Label(fileInfo.getName());
				pdfName.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
				pdfName.setMaxWidth(200);
				pdfName.setWrapText(true);

				pdfPreview.getChildren().addAll(pdfIcon, pdfName);
				previewContainer.getChildren().add(pdfPreview);
			}
		} else if (fileType.equals("image"))
		{
			try
			{
				byte[] imageContent = fileInfoController.getFileContent(fileInfo);
				if (imageContent != null)
				{
					Image image = new Image(new ByteArrayInputStream(imageContent));
					ImageView imageView = new ImageView(image);

					imageView.setFitWidth(217);
					imageView.setFitHeight(160);
					imageView.setPreserveRatio(true);
					imageView.setSmooth(true);

					StackPane.setAlignment(imageView, Pos.CENTER);

					previewContainer.getChildren().add(imageView);
				} else
				{
					throw new IOException("No image content available");
				}
			} catch (Exception e)
			{

				VBox imagePreview = new VBox(10);
				imagePreview.setAlignment(Pos.CENTER);

				FontIcon imageIcon = new FontIcon("fas-image");
				imageIcon.setIconSize(60);
				imageIcon.setIconColor(Color.web("#333333"));

				Label imageName = new Label(fileInfo.getName());
				imageName.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
				imageName.setMaxWidth(200);
				imageName.setWrapText(true);

				imagePreview.getChildren().addAll(imageIcon, imageName);
				previewContainer.getChildren().add(imagePreview);
			}
		} else
		{

			VBox genericPreview = new VBox(10);
			genericPreview.setAlignment(Pos.CENTER);

			FontIcon fileIcon = new FontIcon("fas-file");
			fileIcon.setIconSize(60);
			fileIcon.setIconColor(Color.web("#333333"));

			Label fileName = new Label(fileInfo.getName());
			fileName.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
			fileName.setMaxWidth(200);
			fileName.setWrapText(true);

			genericPreview.getChildren().addAll(fileIcon, fileName);
			previewContainer.getChildren().add(genericPreview);
		}

		HBox fileActions = new HBox();
		fileActions.getStyleClass().add("file-actions");
		fileActions.setAlignment(Pos.CENTER_LEFT);

		Label fileName = new Label(fileInfo.getName());
		fileName.getStyleClass().add("file-name");
		fileName.setMaxWidth(140);
		fileName.setWrapText(true);
		fileName.setTextOverrun(OverrunStyle.ELLIPSIS); // Add ellipsis for long names
		HBox.setHgrow(fileName, Priority.ALWAYS);

		Button downloadBtn = new Button();
		FontIcon downloadIcon = new FontIcon("fas-download");
		downloadIcon.setIconSize(16);
		downloadIcon.setIconColor(Color.WHITE);
		downloadBtn.setGraphic(downloadIcon);
		downloadBtn.getStyleClass().add("action-button-small");
		downloadBtn.setOnAction(e -> downloadFile(fileInfo));

		Button deleteBtn = new Button();
		FontIcon deleteIcon = new FontIcon("fas-trash");
		deleteIcon.setIconSize(16);
		deleteIcon.setIconColor(Color.WHITE);
		deleteBtn.setGraphic(deleteIcon);
		deleteBtn.getStyleClass().add("action-button-small");
		deleteBtn.setOnAction(e -> deleteFile(fileInfo));

		fileActions.getChildren().addAll(fileName, downloadBtn, deleteBtn);

		fileBox.getChildren().addAll(previewContainer, fileActions);

		return fileBox;
	}

	private Stage getStage()
	{
		return (Stage) getScene().getWindow();
	}

	private void uploadFiles()
	{
		if (currentMaintenance == null)
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(I18n.get("maintenance-detail-upload-error"));
			alert.setHeaderText(I18n.get("maintenance-detail-no-maintenance-selected"));
			alert.setContentText(I18n.get("maintenance-detail-no-maintenance-selected-text"));
			alert.initOwner(getStage());
			alert.showAndWait();
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(I18n.get("maintenance-detail-upload-files"));

		// Set supported file types
		FileChooser.ExtensionFilter allSupportedFilter = new FileChooser.ExtensionFilter(I18n.get("all-supported-files"),
				"*.pdf", "*.jpg", "*.jpeg", "*.png", "*.gif");
		FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter(I18n.get("pdf-files"), "*.pdf");
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(I18n.get("photo-files"), "*.jpg", "*.jpeg",
				"*.png", "*.gif");

		fileChooser.getExtensionFilters().addAll(allSupportedFilter, pdfFilter, imageFilter);
		fileChooser.setSelectedExtensionFilter(allSupportedFilter);

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getStage());

		if (selectedFiles != null && !selectedFiles.isEmpty())
		{

			Maintenance maintenance = maintenanceController.getMaintenance(currentMaintenance.id());
			if (maintenance == null)
			{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(I18n.get("maintenance-detail-upload-error"));
				alert.setHeaderText(I18n.get("database-error"));
				alert.setContentText(I18n.get("no-maintenances-in-db"));
				alert.initOwner(getStage());
				alert.showAndWait();
				return;
			}

			List<String> failedUploads = new ArrayList<>();
			List<FileInfo> successfulUploads = new ArrayList<>();

			for (File file : selectedFiles)
			{
				try
				{

					String fileType = getFileType(file.getName());
					if (!isValidFileType(fileType))
					{
						failedUploads.add(String.format(
								"- %s: %s",
								file.getName(), I18n.get("file-type-unsupported")));
						continue;
					}

					FileInfo newFile = new FileInfo(file.getName(), fileType, null, maintenance);

					fileInfoController.saveFileContent(file, newFile);

					successfulUploads.add(newFile);

				} catch (Exception e)
				{
					String errorMessage = e.getMessage();
					if (errorMessage.contains("Packet for query is too large"))
					{
						errorMessage = I18n.get("file-too-large");
					}
					failedUploads.add(String.format("- %s: %s", file.getName(), errorMessage));
				}
			}

			if (!successfulUploads.isEmpty())
			{

				if (filesSection == null)
				{

					if (currentFiles == null)
					{
						currentFiles = new ArrayList<>();
					}

					createFilesSection();
				}

				for (FileInfo file : successfulUploads)
				{
					currentFiles.add(file);
					filesContainer.getChildren().add(createFileBox(file));
				}
			}

			if (!failedUploads.isEmpty())
			{
				StringBuilder errorMessage = new StringBuilder(
						I18n.get("could-not-upload-following-files")+":\n\n");
				errorMessage.append(String.join("\n", failedUploads));
				errorMessage.append("\n\n"+I18n.get("check-file-size-and-extension"));

				Platform.runLater(() ->
				{
					try
					{
						Alert errorAlert = new Alert(Alert.AlertType.ERROR);
						errorAlert.setTitle(I18n.get("maintenance-detail-upload-error"));
						errorAlert.setHeaderText(I18n.get("file-upload-error-header-text"));
						errorAlert.setContentText(errorMessage.toString());
						errorAlert.initOwner(getStage());
						errorAlert.showAndWait();
					} catch (Exception e)
					{
						System.err.println(I18n.get("error") + e.getMessage());
					}
				});
			}
		}
	}

	private String getFileType(String fileName)
	{
		String lowerCaseName = fileName.toLowerCase();
		if (lowerCaseName.endsWith(".pdf"))
		{
			return "pdf";
		} else if (lowerCaseName.matches(".*\\.(jpg|jpeg|png|gif)$"))
		{
			return "image";
		}
		return "other";
	}

	private boolean isValidFileType(String fileType)
	{
		return fileType.equals("pdf") || fileType.equals("image") || fileType.equals("video");
	}

	private void downloadFile(FileInfo fileInfo)
	{

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(I18n.get("file-select-location"));
		File selectedDirectory = directoryChooser.showDialog(getStage());

		if (selectedDirectory != null)
		{
			try
			{

				byte[] content = fileInfoController.getFileContent(fileInfo);
				if (content == null)
				{
					Alert errorAlert = new Alert(Alert.AlertType.WARNING);
					errorAlert.setTitle(I18n.get("download-error"));
					errorAlert.setHeaderText(I18n.get("file-unavailable"));
					errorAlert.setContentText(I18n.get("download-error-full-text"));
					errorAlert.initOwner(getStage());
					errorAlert.showAndWait();
					return;
				}

				File targetFile = new File(selectedDirectory.getAbsolutePath() + File.separator + fileInfo.getName());

				try (FileOutputStream fos = new FileOutputStream(targetFile))
				{
					fos.write(content);
				}

				Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
				successAlert.setTitle(I18n.get("download-successfull"));
				successAlert.setHeaderText(I18n.get("file-downloaded"));
				successAlert.setContentText(I18n.get("file-downloaded-to") + ": " + targetFile.getAbsolutePath());
				successAlert.initOwner(getStage());
				successAlert.showAndWait();

			} catch (IOException e)
			{
				Alert errorAlert = new Alert(Alert.AlertType.ERROR);
				errorAlert.setTitle(I18n.get("download-error"));
				errorAlert.setHeaderText(I18n.get("download-error-failed"));
				errorAlert.setContentText(I18n.get("error-while-downloading") + " " + e.getMessage());
				errorAlert.initOwner(getStage());
				errorAlert.showAndWait();
			}
		}
	}

	private void deleteFile(FileInfo fileInfo)
	{

		Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
		confirmAlert.setTitle(I18n.get("confirm-delete-file"));
		confirmAlert.setHeaderText(I18n.get("confirm-delete-file-confirmation"));
		confirmAlert.setContentText(I18n.get("file") + ": " + fileInfo.getName());
		confirmAlert.initOwner(getStage());

		Optional<ButtonType> result = confirmAlert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK)
		{
			try
			{

				fileInfoController.deleteFile(fileInfo);

				currentFiles.remove(fileInfo);
				refreshFilesSection();

			} catch (Exception e)
			{
				Alert errorAlert = new Alert(Alert.AlertType.ERROR);
				errorAlert.setTitle(I18n.get("file-delete-error="));
				errorAlert.setHeaderText(I18n.get("file-delete-error-message"));
				errorAlert.setContentText(I18n.get("file-error-while-delete") + " " + e.getMessage());
				errorAlert.initOwner(getStage());
				errorAlert.showAndWait();
			}
		}
	}
}