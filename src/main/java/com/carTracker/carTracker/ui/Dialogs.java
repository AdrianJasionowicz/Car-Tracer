package com.carTracker.carTracker.ui;

import com.carTracker.carTracker.car.CarView;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;
import com.carTracker.carTracker.mileage.MileageStats;
import com.carTracker.carTracker.reminder.ReminderPreset;
import com.carTracker.carTracker.reminder.ServiceReminder;
import com.carTracker.carTracker.util.Formats;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class Dialogs {

    private static final ButtonType SAVE = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType CANCEL = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Dialogs() {
    }

    public static Optional<CarView> carDialog(CarView existing) {
        boolean editing = existing != null;
        TextField brand = new TextField(editing ? existing.brand() : "");
        TextField model = new TextField(editing ? existing.model() : "");
        TextField owner = new TextField(editing ? nullToEmpty(existing.owner()) : "");
        TextField mileage = new TextField(editing && existing.mileage() != null ? existing.mileage().toString() : "");
        mileage.setDisable(editing);
        TextArea description = textArea(editing ? existing.description() : null);

        FormDialog<CarView> dialog = new FormDialog<>(editing ? "Edytuj auto" : "Dodaj auto");
        dialog.addRow("Marka *", brand);
        dialog.addRow("Model *", model);
        dialog.addRow("Osoba odpowiedzialna", owner);
        dialog.addRow("Aktualny przebieg (km) *", mileage);
        dialog.addRow("Opis", description);
        if (editing) {
            dialog.addHint("Przebieg zmieniasz przez „Dodaj wydarzenie” albo „Aktualizuj przebieg”.");
        }
        Platform.runLater(brand::requestFocus);
        return dialog.show(() -> {
            Integer km = editing ? existing.mileage() : parseInt(mileage.getText(), "Przebieg");
            if (km == null) {
                throw new IllegalArgumentException("Podaj aktualny przebieg.");
            }
            return new CarView(editing ? existing.id() : null, required(brand.getText(), "Marka"),
                    required(model.getText(), "Model"), km, blankToNull(owner.getText()), blankToNull(description.getText()));
        });
    }

    public static Optional<MaintenanceRecord> recordDialog(String presetType, MileageStats stats, List<String> typeSuggestions) {
        LocalDate today = LocalDate.now();
        DatePicker date = new DatePicker(today);
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList(typeSuggestions));
        type.setEditable(true);
        type.setMaxWidth(Double.MAX_VALUE);
        type.setValue(presetType);
        TextField mileage = new TextField();
        Integer estimate = stats.estimatedMileageAt(today);
        if (estimate != null) {
            mileage.setPromptText("szacowany dziś ≈ " + Formats.km(estimate));
        }
        TextArea description = textArea(null);

        FormDialog<MaintenanceRecord> dialog = new FormDialog<>("Dodaj wydarzenie");
        dialog.addRow("Data *", date);
        dialog.addRow("Rodzaj *", type);
        dialog.addRow("Przebieg (km)", mileage);
        dialog.addRow("Opis", description);
        dialog.addHint("Każdy wpisany przebieg przelicza średni przebieg i prognozy wszystkich przypomnień. "
                + "Rodzaj o tej samej nazwie co przypomnienie (np. „Wymiana oleju”) oznacza je jako wykonane.");
        Platform.runLater(mileage::requestFocus);
        return dialog.show(() -> {
            LocalDate day = dateValue(date, "Data");
            if (day == null) {
                throw new IllegalArgumentException("Podaj datę.");
            }
            if (day.isAfter(today)) {
                throw new IllegalArgumentException("Data nie może być z przyszłości.");
            }
            String typeText = required(type.getEditor().getText(), "Rodzaj");
            Integer km = parseInt(mileage.getText(), "Przebieg");
            if (km == null && typeText.equalsIgnoreCase(MaintenanceRecord.MILEAGE_UPDATE_TYPE)) {
                throw new IllegalArgumentException("Podaj przebieg.");
            }
            MaintenanceRecord record = new MaintenanceRecord();
            record.setDate(day);
            record.setType(typeText);
            record.setMileage(km);
            record.setDescription(blankToNull(description.getText()));
            return record;
        });
    }

    public static Optional<ServiceReminder> reminderDialog(ServiceReminder existing) {
        boolean editing = existing != null;
        ComboBox<String> name = new ComboBox<>(FXCollections.observableArrayList(
                ReminderPreset.PRESETS.stream().map(ReminderPreset::name).toList()));
        name.setEditable(true);
        name.setMaxWidth(Double.MAX_VALUE);
        TextField intervalKm = new TextField();
        TextField intervalMonths = new TextField();
        DatePicker lastDoneDate = new DatePicker();
        TextField lastDoneMileage = new TextField();
        TextField notifyDays = new TextField(String.valueOf(ServiceReminder.DEFAULT_NOTIFY_DAYS_BEFORE));

        if (editing) {
            name.setValue(existing.getName());
            intervalKm.setText(toText(existing.getIntervalKm()));
            intervalMonths.setText(toText(existing.getIntervalMonths()));
            lastDoneDate.setValue(existing.getLastDoneDate());
            lastDoneMileage.setText(toText(existing.getLastDoneMileage()));
            notifyDays.setText(String.valueOf(existing.notifyDaysBeforeOrDefault()));
        } else {
            name.valueProperty().addListener((obs, old, value) -> ReminderPreset.find(value).ifPresent(preset -> {
                intervalKm.setText(toText(preset.intervalKm()));
                intervalMonths.setText(toText(preset.intervalMonths()));
            }));
        }

        FormDialog<ServiceReminder> dialog = new FormDialog<>(editing ? "Edytuj przypomnienie" : "Dodaj przypomnienie");
        dialog.addRow("Nazwa *", name);
        dialog.addRow("Co ile km", intervalKm);
        dialog.addRow("Co ile miesięcy", intervalMonths);
        dialog.addRow("Ostatnio wykonano – data", lastDoneDate);
        dialog.addRow("Ostatnio wykonano – przebieg", lastDoneMileage);
        dialog.addRow("Powiadom ile dni wcześniej", notifyDays);
        dialog.addHint("Termin to to, co nastąpi pierwsze: km (przeliczone na dni ze średniego przebiegu) albo miesiące. "
                + "Puste „ostatnio wykonano” = ostatnie wydarzenie o tej nazwie z historii, a gdy go nie ma – dziś i aktualny przebieg.");
        return dialog.show(() -> {
            Integer km = parseInt(intervalKm.getText(), "Co ile km");
            Integer months = parseInt(intervalMonths.getText(), "Co ile miesięcy");
            if (km == null && months == null) {
                throw new IllegalArgumentException("Podaj co ile km lub co ile miesięcy.");
            }
            if ((km != null && km == 0) || (months != null && months == 0)) {
                throw new IllegalArgumentException("Interwał musi być większy od zera.");
            }
            Integer days = parseInt(notifyDays.getText(), "Powiadom ile dni wcześniej");
            ServiceReminder reminder = new ServiceReminder();
            reminder.setId(editing ? existing.getId() : 0);
            reminder.setName(required(name.getEditor().getText(), "Nazwa"));
            reminder.setIntervalKm(km);
            reminder.setIntervalMonths(months);
            reminder.setLastDoneDate(dateValue(lastDoneDate, "Ostatnio wykonano – data"));
            reminder.setLastDoneMileage(parseInt(lastDoneMileage.getText(), "Ostatnio wykonano – przebieg"));
            reminder.setNotifyDaysBefore(days != null ? days : ServiceReminder.DEFAULT_NOTIFY_DAYS_BEFORE);
            return reminder;
        });
    }

    public static boolean confirm(String question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, question,
                new ButtonType("Tak", ButtonBar.ButtonData.YES), new ButtonType("Nie", ButtonBar.ButtonData.NO));
        alert.setTitle("Potwierdź");
        alert.setHeaderText(null);
        return alert.showAndWait().map(b -> b.getButtonData() == ButtonBar.ButtonData.YES).orElse(false);
    }

    public static void info(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text);
        alert.setTitle("CarTracker");
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public static void error(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static Integer parseInt(String text, String label) {
        String cleaned = text == null ? "" : text.replaceAll("[\\s\\u00A0\\u202F]", "").replace("km", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(cleaned);
            if (value < 0) {
                throw new IllegalArgumentException(label + ": wartość nie może być ujemna.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + ": wpisz liczbę całkowitą.");
        }
    }

    private static LocalDate dateValue(DatePicker picker, String label) {
        String text = picker.getEditor().getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return picker.getConverter().fromString(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(label + ": niepoprawna data.");
        }
    }

    private static String required(String text, String label) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Pole „" + label + "” jest wymagane.");
        }
        return text.trim();
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static String toText(Integer value) {
        return value == null ? "" : value.toString();
    }

    private static TextArea textArea(String text) {
        TextArea area = new TextArea(nullToEmpty(text));
        area.setPrefRowCount(4);
        area.setWrapText(true);
        return area;
    }

    private static final class FormDialog<T> {
        private final Dialog<T> dialog = new Dialog<>();
        private final GridPane grid = new GridPane();
        private final Label error = new Label();
        private int row;

        FormDialog(String title) {
            dialog.setTitle(title);
            dialog.setHeaderText(null);
            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes().addAll(SAVE, CANCEL);
            pane.getStylesheets().add(Dialogs.class.getResource("/ui/style.css").toExternalForm());
            pane.setPrefWidth(520);
            grid.setHgap(10);
            grid.setVgap(8);
            grid.setPadding(new Insets(10));
            error.getStyleClass().add("form-error");
            error.setWrapText(true);
            pane.setContent(new VBox(8, grid, error));
        }

        void addRow(String label, Node field) {
            grid.add(new Label(label), 0, row);
            grid.add(field, 1, row);
            GridPane.setHgrow(field, Priority.ALWAYS);
            row++;
        }

        void addHint(String text) {
            Label hint = new Label(text);
            hint.getStyleClass().add("hint");
            hint.setWrapText(true);
            hint.setMaxWidth(480);
            grid.add(hint, 0, row, 2, 1);
            row++;
        }

        Optional<T> show(Supplier<T> builder) {
            AtomicReference<T> result = new AtomicReference<>();
            Button save = (Button) dialog.getDialogPane().lookupButton(SAVE);
            save.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    result.set(builder.get());
                } catch (IllegalArgumentException e) {
                    error.setText(e.getMessage());
                    event.consume();
                }
            });
            dialog.setResultConverter(button -> button == SAVE ? result.get() : null);
            return dialog.showAndWait();
        }
    }
}
