package gui;

import java.util.List;

import domain.KPI;
import domain.KPIController;
import domain.KPIWaarde;
import domain.KPIWaardeController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.Grafiek;
import util.I18n;

public class HomeScreen extends GridPane
{
	private final MainLayout mainLayout;
	private final KPIWaardeController kpiWaardeController;
	private final KPIController kpiController;

	public HomeScreen(MainLayout mainLayout)
	{
		this.mainLayout = mainLayout;
		this.kpiWaardeController = mainLayout.getServices().getKpiWaardeController();
		this.kpiController = mainLayout.getServices().getKpiController();

		setHgap(20);
		setVgap(20);
		setPadding(new Insets(20));

		this.setAlignment(Pos.CENTER);

		setupLayout();
	}
	
	private String convertToI18nSubject(String subject) {
		return switch(subject.toLowerCase()) {
		case "algemene gezondheid alle sites" -> I18n.get("kpis.general-health-all");
		case "algemene gezondheid site x" -> I18n.get("kpis.general-health-specific");
		case "productiegraad alle sites gesorteerd (hoog naar laag)" -> I18n.get("kpis.productiongrade-high-to-low");
		case "productiegraad alle sites gesorteerd (laag naar hoog)" -> I18n.get("kpis.productiongrade-low-to-high");
		case "top 5 gezonde machines" -> I18n.get("kpis.top-5-healthy");
		case "top 5 falende machines" -> I18n.get("kpis.top-5-failing");
		case "top 5 machines met nood aan onderhoud" -> I18n.get("kpis.top-5-needs-maintenance");
		case "aankomende onderhoudsbeurten" -> I18n.get("kpis.future-maintenances");
		case "laatste 5 onderhoudsbeurten" -> I18n.get("kpis.last-5-maintenances");
		case "draaiende machines" -> I18n.get("kpis.running-machines");
		case "manueel gestopte machines" -> I18n.get("kpis.manually-stopped-machines");
		case "automatisch gestopte machines" -> I18n.get("kpis.automatically-stopped-machines");
		case "machines in onderhoud" -> I18n.get("kpis.machines-under-maintenance");
		case "startbare machines" -> I18n.get("kpis.startable-machines");
		case "mijn machines" -> I18n.get("kpis.my-machines");
 		default -> subject;
		};
	}

	private void setupLayout()
	{
		List<KPI> kpis = kpiController.getAllKPIs();

		int row = 0;
		int col = 0;
		for (KPI kpi : kpis)
		{
			List<KPIWaarde> waarden = kpiWaardeController.getWaardenByKPI(kpi.getId());

			VBox kpiBox = new VBox(10);
			kpiBox.getStyleClass().add("kpi-tile");

			Label titleLabel = new Label(convertToI18nSubject(kpi.getOnderwerp()));
			titleLabel.getStyleClass().add("kpi-title");

			if (kpi.getGrafiek() == Grafiek.SINGLE || kpi.getGrafiek() == Grafiek.GEZONDHEID)
			{
				titleLabel.getStyleClass().add("kpi-title-large");
			} else
			{
				titleLabel.getStyleClass().add("kpi-title-small");
			}

			kpiBox.getChildren().add(titleLabel);
			kpiBox.getChildren().add(generateGraph(kpi.getGrafiek(), waarden));
			kpiBox.setPrefHeight(500);
			kpiBox.setFillWidth(true);

			add(kpiBox, col, row);

			col++;
			if (col == 3)
			{
				col = 0;
				row++;
			}
		}
	}

	private Node generateGraph(Grafiek graphType, List<KPIWaarde> waarden)
	{
		return switch (graphType)
		{
		case Grafiek.BARHOOGLAAG -> createBarChart(waarden, true);
		case Grafiek.BARLAAGHOOG -> createBarChart(waarden, false);
		case Grafiek.SINGLE -> createSingleValueDisplay(waarden, false);
		case Grafiek.GEZONDHEID -> createSingleValueDisplay(waarden, true);
		default -> null;
		};
	}

	private Node createSingleValueDisplay(List<KPIWaarde> waarden, boolean isPercentage)
	{
		if (waarden.isEmpty())
		{
			Label label = new Label(I18n.get("no-data"));
			label.getStyleClass().add("kpi-no-data");
			return new HBox(label);
		}

		String rawWaarde = waarden.getFirst().getWaarde();
		if (rawWaarde == null || rawWaarde.isBlank())
		{
			return new HBox(new Label("Onbekend"));
		}

		rawWaarde = rawWaarde.replaceAll("^\"|\"$", "");

		String displayWaarde = rawWaarde;

		try
		{
			double value = Double.parseDouble(rawWaarde);
			displayWaarde = String.format("%.0f", value);
			if (isPercentage)
			{
				displayWaarde += "%";
			}
		} catch (NumberFormatException e)
		{

		}

		Label valueLabel = new Label(displayWaarde);
		valueLabel.getStyleClass().add("kpi-single-value");

		VBox vbox = new VBox(valueLabel);
		vbox.setStyle("-fx-alignment: center;");
		vbox.setPrefHeight(400);
		vbox.setMaxHeight(Double.MAX_VALUE);

		return vbox;

	}

	private Node createBarChart(List<KPIWaarde> waarden, boolean hoogLaag)
	{
		if (waarden.isEmpty())
		{
			return new HBox(new Label(I18n.get("no-data")));
		}

		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Site");
		xAxis.setTickLabelsVisible(false);
		xAxis.setTickMarkVisible(false);

		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Productiegraad");

		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setLegendVisible(false);
		barChart.setCategoryGap(10);
		barChart.setBarGap(5);
		barChart.setPrefHeight(300);
		barChart.setPrefWidth(400);

		XYChart.Series<String, Number> series = new XYChart.Series<>();

		final int[] index = { 1 };

		waarden.stream().sorted((w1, w2) -> {
			try
			{
				double v1 = Double.parseDouble(w1.getWaarde().replaceAll("^\"|\"$", ""));
				double v2 = Double.parseDouble(w2.getWaarde().replaceAll("^\"|\"$", ""));
				return hoogLaag ? Double.compare(v2, v1) : Double.compare(v1, v2);
			} catch (NumberFormatException e)
			{
				return 0;
			}
		}).forEach(w -> {
			try
			{
				String label = "Waarde " + index[0]++;
				double value = Double.parseDouble(w.getWaarde().replaceAll("^\"|\"$", ""));
				series.getData().add(new XYChart.Data<>(label, value));
			} catch (NumberFormatException e)
			{

			}
		});

		barChart.getData().add(series);

		VBox container = new VBox(5);
		container.getChildren().addAll(barChart);

		return container;
	}

}
