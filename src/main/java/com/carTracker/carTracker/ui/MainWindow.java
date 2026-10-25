package com.carTracker.carTracker.ui;

import com.carTracker.carTracker.car.CarService;
import com.carTracker.carTracker.car.CarView;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecord;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecordInterface;
import com.carTracker.carTracker.mileage.MileageCheck;
import com.carTracker.carTracker.mileage.MileageStats;
import com.carTracker.carTracker.reminder.ReminderPreset;
import com.carTracker.carTracker.reminder.ReminderService;
import com.carTracker.carTracker.reminder.ReminderStatus;
import com.carTracker.carTracker.util.Formats;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class MainWindow {

    private final CarService carService;
    private final MaintenanceRecordInterface recordService;
    private final ReminderService reminderService;

    private final BorderPane root = new BorderPane();
    private final ListView<CarView> carList = new ListView<>();
    private final Label titleLabel = new Label();
    private final Label ownerLabel = new Label();
    private final Label mileageLabel = new Label();
    private final Label averageLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final TableView<ReminderStatus> reminderTable = new TableView<>();
    private final TableView<MaintenanceRecord> recordTable = new TableView<>();
    private final Node detailsPane;
    private final Label emptyLabel = new Label("Dodaj pierwsze auto przyciskiem „Dodaj auto”.");

    private CarView currentCar;

    public MainWindow(CarService carService, MaintenanceRecordInterface recordService, ReminderService reminderService) {
        this.carService = carService;
        this.recordService = recordService;
        this.reminderService = reminderService;

        detailsPane = buildDetailsPane();
        root.setLeft(buildCarPane());
        carList.getSelectionModel().selectedItemProperty().addListener((obs, old, car) -> showCar(car));
        reloadCars(null);
    }

    public Parent getRoot() {
        return root;
    }

    public void refresh() {
        reloadCars(currentCar != null ? currentCar.id() : null);
    }

    private Node buildCarPane() {
        carList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CarView car, boolean empty) {
                super.updateItem(car, empty);
                if (empty || car == null) {
                    setText(null);
                } else {
                    String owner = car.owner() != null ? "\n" + car.owner() : "";
                    setText(car.brand() + " " + car.model() + owner);
                }
            }
        });
        Button add = new Button("Dodaj auto");
        add.setOnAction(e -> run(this::addCar));
        Button delete = new Button("Usuń auto");
        delete.setOnAction(e -> run(this::deleteCar));
        delete.disableProperty().bind(carList.getSelectionModel().selectedItemProperty().isNull());

        Label header = new Label("Auta");
        header.getStyleClass().add("car-title");
        VBox pane = new VBox(8, header, carList, new HBox(8, add, delete));
        VBox.setVgrow(carList, Priority.ALWAYS);
        pane.setPadding(new Insets(10));
        pane.setPrefWidth(250);
        return pane;
    }

    private Node buildDetailsPane() {
        titleLabel.getStyleClass().add("car-title");
        Button edit = new Button("Edytuj auto");
        edit.setOnAction(e -> run(this::editCar));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, titleLabel, spacer, edit);
        header.setAlignment(Pos.CENTER_LEFT);

        descriptionLabel.setWrapText(true);
        GridPane info = new GridPane();
        info.setHgap(12);
        info.setVgap(4);
        addInfoRow(info, 0, "Osoba odpowiedzialna:", ownerLabel);
        addInfoRow(info, 1, "Przebieg:", mileageLabel);
        addInfoRow(info, 2, "Średni przebieg:", averageLabel);
        addInfoRow(info, 3, "Opis:", descriptionLabel);

        Tab reminders = new Tab("Przypomnienia", buildReminderPane());
        Tab history = new Tab("Historia wydarzeń", buildHistoryPane());
        TabPane tabs = new TabPane(reminders, history);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox pane = new VBox(10, header, info, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        pane.setPadding(new Insets(10));
        return pane;
    }

    private Node buildReminderPane() {
        reminderTable.getColumns().setAll(List.of(
                column("Czynność", s -> s.reminder().getName(), 160),
                column("Interwał", s -> Formats.interval(s.reminder()), 150),
                column("Ostatnio", s -> Formats.lastDone(s.reminder()), 170),
                column("Następnie przy", s -> Formats.km(s.dueMileage()), 110),
                column("Zostało km", s -> Formats.km(s.kmLeft()), 100),
                column("Prognozowana data", s -> Formats.date(s.dueDate()), 130),
                column("Kiedy", s -> Formats.days(s.daysLeft()), 100),
                column("Status", s -> s.status().getLabel(), 100)));
        reminderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        reminderTable.setPlaceholder(new Label("Brak przypomnień. Dodaj np. „Wymiana oleju”."));
        reminderTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(ReminderStatus status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("overdue", "soon");
                if (!empty && status != null) {
                    if (status.status() == ReminderStatus.Status.OVERDUE) {
                        getStyleClass().add("overdue");
                    } else if (status.status() == ReminderStatus.Status.SOON) {
                        getStyleClass().add("soon");
                    }
                }
            }
        });

        Button add = new Button("Dodaj przypomnienie");
        add.setOnAction(e -> run(this::addReminder));
        Button edit = new Button("Edytuj");
        edit.setOnAction(e -> run(this::editReminder));
        Button done = new Button("Oznacz jako wykonane…");
        done.setOnAction(e -> run(this::markReminderDone));
        Button delete = new Button("Usuń");
        delete.setOnAction(e -> run(this::deleteReminder));
        var noSelection = reminderTable.getSelectionModel().selectedItemProperty().isNull();
        edit.disableProperty().bind(noSelection);
        done.disableProperty().bind(noSelection);
        delete.disableProperty().bind(noSelection);

        return tablePane(reminderTable, add, edit, done, delete);
    }

    private Node buildHistoryPane() {
        recordTable.getColumns().setAll(List.of(
                column("Data", r -> Formats.date(r.getDate()), 100),
                column("Przebieg", r -> Formats.km(r.getMileage()), 110),
                column("Rodzaj", MaintenanceRecord::getType, 200),
                column("Opis", r -> r.getDescription() != null ? r.getDescription() : "", 450)));
        recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recordTable.setPlaceholder(new Label("Brak wydarzeń."));

        Button add = new Button("Dodaj wydarzenie");
        add.setOnAction(e -> run(() -> addRecord(null)));
        Button mileage = new Button("Aktualizuj przebieg");
        mileage.setOnAction(e -> run(() -> addRecord(MaintenanceRecord.MILEAGE_UPDATE_TYPE)));
        Button delete = new Button("Usuń wpis");
        delete.setOnAction(e -> run(this::deleteRecord));
        delete.disableProperty().bind(recordTable.getSelectionModel().selectedItemProperty().isNull());

        return tablePane(recordTable, add, mileage, delete);
    }

    private static Node tablePane(TableView<?> table, Button... buttons) {
        VBox pane = new VBox(8, table, new HBox(8, buttons));
        VBox.setVgrow(table, Priority.ALWAYS);
        pane.setPadding(new Insets(8, 0, 0, 0));
        return pane;
    }

    private static void addInfoRow(GridPane grid, int row, String label, Label value) {
        Label name = new Label(label);
        name.getStyleClass().add("info-label");
        name.setMinWidth(Region.USE_PREF_SIZE);
        grid.add(name, 0, row);
        grid.add(value, 1, row);
    }

    private static <S> TableColumn<S, String> column(String title, Function<S, String> getter, double width) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        column.setPrefWidth(width);
        column.setSortable(false);
        return column;
    }

    private void reloadCars(Long selectId) {
        List<CarView> cars = carService.getCars();
        carList.setItems(FXCollections.observableArrayList(cars));
        CarView toSelect = cars.stream()
                .filter(c -> c.id().equals(selectId))
                .findFirst()
                .orElse(cars.isEmpty() ? null : cars.getFirst());
        carList.getSelectionModel().select(toSelect);
        showCar(toSelect);
    }

    private void showCar(CarView car) {
        currentCar = car;
        if (car == null) {
            root.setCenter(emptyLabel);
            return;
        }
        root.setCenter(detailsPane);
        titleLabel.setText(car.brand() + " " + car.model());
        ownerLabel.setText(car.owner() != null ? car.owner() : "–");
        descriptionLabel.setText(car.description() != null ? car.description() : "–");

        MileageStats stats = recordService.getMileageStats(car.id());
        if (stats.lastMileage() == null) {
            mileageLabel.setText("–");
        } else {
            Integer estimate = stats.estimatedMileageAt(LocalDate.now());
            String estimateText = stats.averageKmPerDay() != null && !estimate.equals(stats.lastMileage())
                    ? ", szacowany dziś ≈ " + Formats.km(estimate)
                    : "";
            mileageLabel.setText("ostatni odczyt " + Formats.km(stats.lastMileage())
                    + " (" + Formats.date(stats.lastReadingDate()) + ")" + estimateText);
        }
        averageLabel.setText(stats.averageKmPerDay() != null
                ? Formats.average(stats.averageKmPerDay())
                : "brak danych – potrzebne co najmniej 2 odczyty przebiegu z różnych dni");

        reminderTable.setItems(FXCollections.observableArrayList(reminderService.getStatuses(car.id())));
        recordTable.setItems(FXCollections.observableArrayList(recordService.getAllMaintenanceRecordsToCar(car.id())));
    }

    private void addCar() {
        Dialogs.carDialog(null).ifPresent(car -> reloadCars(carService.addCar(car).id()));
    }

    private void editCar() {
        Dialogs.carDialog(currentCar).ifPresent(car -> reloadCars(carService.updateCar(car).id()));
    }

    private void deleteCar() {
        if (currentCar != null && Dialogs.confirm("Usunąć auto " + currentCar.brand() + " " + currentCar.model()
                + " razem z całą historią i przypomnieniami?")) {
            carService.deleteCar(currentCar.id());
            reloadCars(null);
        }
    }

    private void addRecord(String presetType) {
        if (currentCar == null) {
            return;
        }
        MileageStats stats = recordService.getMileageStats(currentCar.id());
        Dialogs.recordDialog(presetType, stats, typeSuggestions()).ifPresent(record -> {
            MileageCheck check = recordService.addMaintenanceRecord(currentCar.id(), record);
            refresh();
            showMileageCheck(check);
        });
    }

    private void deleteRecord() {
        MaintenanceRecord record = recordTable.getSelectionModel().getSelectedItem();
        if (record != null && Dialogs.confirm("Usunąć wpis „" + record.getType() + "” z " + Formats.date(record.getDate()) + "?")) {
            recordService.deleteMaintenanceRecord(record.getId());
            refresh();
        }
    }

    private void addReminder() {
        if (currentCar != null) {
            Dialogs.reminderDialog(null).ifPresent(reminder -> {
                reminderService.save(currentCar.id(), reminder);
                refresh();
            });
        }
    }

    private void editReminder() {
        ReminderStatus selected = reminderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Dialogs.reminderDialog(selected.reminder()).ifPresent(reminder -> {
                reminderService.save(currentCar.id(), reminder);
                refresh();
            });
        }
    }

    private void markReminderDone() {
        ReminderStatus selected = reminderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            addRecord(selected.reminder().getName());
        }
    }

    private void deleteReminder() {
        ReminderStatus selected = reminderTable.getSelectionModel().getSelectedItem();
        if (selected != null && Dialogs.confirm("Usunąć przypomnienie „" + selected.reminder().getName() + "”?")) {
            reminderService.delete(selected.reminder().getId());
            refresh();
        }
    }

    private void showMileageCheck(MileageCheck check) {
        StringBuilder text = new StringBuilder();
        if (check.enteredMileage() != null && check.predictedMileage() != null && check.previousAverage() != null) {
            int difference = check.enteredMileage() - check.predictedMileage();
            text.append("Prognozowany przebieg na ten dzień: ").append(Formats.km(check.predictedMileage()))
                    .append("\nWpisany przebieg: ").append(Formats.km(check.enteredMileage()))
                    .append(difference == 0 ? " (zgodnie z prognozą)" : " (" + Formats.kmDifference(difference) + ")")
                    .append("\n\nŚredni przebieg: ").append(Formats.average(check.previousAverage()))
                    .append("\n                 → ").append(Formats.average(check.newAverage()))
                    .append("\n\nTerminy wszystkich przypomnień zostały przeliczone.");
        } else if (check.previousAverage() == null && check.newAverage() != null) {
            text.append("Wyliczono średni przebieg: ").append(Formats.average(check.newAverage()))
                    .append("\nOd teraz terminy z km są prognozowane w dniach.");
        }
        if (!check.updatedReminders().isEmpty()) {
            if (!text.isEmpty()) {
                text.append("\n\n");
            }
            text.append("Oznaczono jako wykonane: ").append(String.join(", ", check.updatedReminders()));
        }
        if (!text.isEmpty()) {
            Dialogs.info("Zapisano wydarzenie", text.toString());
        }
    }

    private List<String> typeSuggestions() {
        Set<String> types = new LinkedHashSet<>();
        types.add(MaintenanceRecord.MILEAGE_UPDATE_TYPE);
        reminderTable.getItems().forEach(s -> types.add(s.reminder().getName()));
        ReminderPreset.PRESETS.forEach(p -> types.add(p.name()));
        return List.copyOf(types);
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            Dialogs.error(e.getMessage());
            refresh();
        } catch (RuntimeException e) {
            Dialogs.error("Coś poszło nie tak: " + e.getMessage());
            refresh();
        }
    }
}
