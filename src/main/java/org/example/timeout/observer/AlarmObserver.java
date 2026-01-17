package org.example.timeout.observer;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.timeout.subject.Subject;
import org.example.timeout.subject.Subject;

public class AlarmObserver implements IObserver {

    private final TextField offsetInput;
    private final Label statusLabel;

    private Integer alarmTick;
    private volatile boolean active = true;

    public AlarmObserver(TextField offsetInput, Label statusLabel) {
        this.offsetInput = offsetInput;
        this.statusLabel = statusLabel;
        Platform.runLater(() -> statusLabel.setText("Ожидание"));
    }

    public synchronized void rearm() {
        alarmTick = null;
        active = true;
        Platform.runLater(() -> statusLabel.setText("Ожидание"));
    }

    @Override
    public void update(Subject subject) {
        if (!active) {
            return;
        }

        int currentTick = subject.getState();
        Integer target;

        synchronized (this) {
            if (alarmTick == null) {
                try {
                    int offset = Integer.parseInt(offsetInput.getText());
                    if (offset <= 0) {
                        Platform.runLater(() -> statusLabel.setText("> 0 сек"));
                        active = false;
                        return;
                    }
                    alarmTick = currentTick + offset;
                    final int targetTick = alarmTick;
                    Platform.runLater(() -> statusLabel.setText("Сработает на тике " + targetTick));
                } catch (NumberFormatException e) {
                    Platform.runLater(() -> statusLabel.setText("Ошибка ввода"));
                    active = false;
                    return;
                }
            }
            target = alarmTick;
        }

        if (target != null && currentTick >= target) {
            active = false;
            Platform.runLater(() -> statusLabel.setText("Будильник сработал"));
        }
    }
}
