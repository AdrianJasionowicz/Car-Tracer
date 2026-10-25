package com.carTracker.carTracker.ui;

import com.carTracker.carTracker.CarTrackerApplication;
import com.carTracker.carTracker.car.CarService;
import com.carTracker.carTracker.maintenanceRecord.MaintenanceRecordInterface;
import com.carTracker.carTracker.notification.NotificationService;
import com.carTracker.carTracker.reminder.ReminderService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private static final String START_IN_TRAY_ARG = "--tray";

    private ConfigurableApplicationContext context;
    private NotificationService notificationService;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(CarTrackerApplication.class)
                .headless(false)
                .run(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stage) {
        MainWindow mainWindow = new MainWindow(
                context.getBean(CarService.class),
                context.getBean(MaintenanceRecordInterface.class),
                context.getBean(ReminderService.class));

        Scene scene = new Scene(mainWindow.getRoot(), 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/ui/style.css").toExternalForm());
        stage.setTitle("CarTracker");
        stage.getIcons().add(AppIcon.fxImage());
        stage.setScene(scene);

        Runnable showWindow = () -> Platform.runLater(() -> {
            mainWindow.refresh();
            stage.show();
            stage.setIconified(false);
            stage.toFront();
        });

        notificationService = context.getBean(NotificationService.class);
        boolean trayInstalled = notificationService.install(showWindow, () -> Platform.runLater(Platform::exit));
        if (trayInstalled) {
            Platform.setImplicitExit(false);
            stage.setOnCloseRequest(event -> {
                event.consume();
                stage.hide();
                notificationService.showHiddenHintOnce();
            });
        }
        SingleInstance.setActivateHandler(showWindow);
        notificationService.startScheduledChecks();

        boolean startInTray = trayInstalled && getParameters().getRaw().contains(START_IN_TRAY_ARG);
        if (!startInTray) {
            stage.show();
        }
    }

    @Override
    public void stop() {
        if (notificationService != null) {
            notificationService.shutdown();
        }
        if (context != null) {
            context.close();
        }
        System.exit(0);
    }
}
