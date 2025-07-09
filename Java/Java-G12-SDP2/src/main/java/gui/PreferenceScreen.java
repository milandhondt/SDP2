package gui;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import util.I18n;

import java.util.Locale;

public class PreferenceScreen extends VBox {

    private final Label languageLabel;
    private final ComboBox<String> languageComboBox;
    private final MainLayout mainLayout;

    public PreferenceScreen(MainLayout mainLayout) {
    	this.mainLayout = mainLayout;
        languageLabel = new Label(I18n.get("preferences.language"));
        languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll("English", "Nederlands");

        Locale currentLocale = Locale.getDefault();
        if (currentLocale.getLanguage().equals("nl")) {
            languageComboBox.setValue("Nederlands");
        } else {
            languageComboBox.setValue("English");
        }

        languageComboBox.setOnAction(e -> {
            String selected = languageComboBox.getValue();
            switch (selected) {
                case "English" -> I18n.setLocale(Locale.ENGLISH);
                case "Nederlands" -> I18n.setLocale(Locale.of("nl"));
            }
            refreshTexts();
        });

        setSpacing(10);
        setPadding(new Insets(20));
        getChildren().addAll(languageLabel, languageComboBox);
    }

    private void refreshTexts() {
        languageLabel.setText(I18n.get("preferences.language"));
    }
}
