package org.example.timeout;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.timeout.observer.AlarmObserver;
import org.example.timeout.observer.IObserver;
import org.example.timeout.observer.StopwatchObserver;
import org.example.timeout.observer.TimerObserver;
import org.example.timeout.subject.Subject;
import org.example.timeout.subject.TimeServer;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class HelloController {

    private static final DateTimeFormatter UTC_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter LOG_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private Label serverStatusLabel;
    @FXML private Label tickCountLabel;
    @FXML private Label utcTimeLabel;
    @FXML private Label stopwatchDisplay;
    @FXML private Label timerDisplay;
    @FXML private Label passiveAlarmStatus;
    @FXML private TextField alarmOffsetInput;
    @FXML private TextField timerCountdownInput;
    @FXML private TextArea eventLog;
    @FXML private Button startServerButton;
    @FXML private Button stopServerButton;

    private TimeServer server;
    private TimerObserver timerObserver;
    private AlarmObserver alarmObserver;

    private final IObserver monitorObserver = new IObserver() {
        @Override
        public void update(Subject subject) {
            String ticks = String.valueOf(subject.getState());
            String utc = UTC_FORMATTER.format(subject.getUtcTime());
            Platform.runLater(() -> {
                tickCountLabel.setText(ticks);
                utcTimeLabel.setText(utc);
            });
        }
    };

    @FXML
    public void initialize() {
        server = new TimeServer();
        server.attach(monitorObserver);

        StopwatchObserver stopwatchObserver = new StopwatchObserver(stopwatchDisplay);
        timerObserver = new TimerObserver(timerDisplay);
        alarmObserver = new AlarmObserver(alarmOffsetInput, passiveAlarmStatus);

        server.attach(stopwatchObserver);
        server.attach(timerObserver);
        server.attach(alarmObserver);

        timerCountdownInput.setText("10");
        updateServerStatus(false);
        logEvent("Приложение готово к работе.");
    }

    @FXML
    private void onStartServer() {
        server.start();
        updateServerStatus(true);
        logEvent("Сервер времени запущен.");
    }

    @FXML
    private void onStopServer() {
        server.stop();
        updateServerStatus(false);
        logEvent("Сервер времени остановлен.");
    }

    @FXML
    private void onResetTicks() {
        server.reset();
        timerObserver.cancel();
        alarmObserver.rearm();
        logEvent("Счётчик тиков и компоненты сброшены.");
    }

    @FXML
    private void onStartTimer() {
        try {
            int seconds = Integer.parseInt(timerCountdownInput.getText().trim());
            timerObserver.startCountdown(seconds);
            logEvent("Таймер запущен на " + seconds + " с.");
        } catch (NumberFormatException ex) {
            logEvent("Ошибка: введите число секунд для таймера.");
        }
    }

    @FXML
    private void onArmAlarm() {
        alarmObserver.rearm();
        logEvent("Будильник перезапущен.");
    }

    private void updateServerStatus(boolean running) {
        Platform.runLater(() -> {
            serverStatusLabel.setText(running ? "Активен" : "Остановлен");
            startServerButton.setDisable(running);
            stopServerButton.setDisable(!running);
        });
    }

    private void logEvent(String message) {
        String timestamp = LocalTime.now().format(LOG_TIME_FORMATTER);
        Platform.runLater(() -> {
            String line = "[" + timestamp + "] " + message;
            if (eventLog.getText().isEmpty()) {
                eventLog.setText(line);
            } else {
                eventLog.appendText(System.lineSeparator() + line);
            }
        });
    }
}
