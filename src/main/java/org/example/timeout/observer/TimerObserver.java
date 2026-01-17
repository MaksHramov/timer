package org.example.timeout.observer;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.example.timeout.subject.Subject;

public class TimerObserver implements IObserver {

    private final Label display;

    private Integer pendingSeconds;
    private Integer targetTick;

    public TimerObserver(Label display) {
        this.display = display;
        Platform.runLater(() -> display.setText("—"));
    }

    public synchronized void startCountdown(int seconds) {
        if (seconds <= 0) {
            pendingSeconds = null;
            targetTick = null;
            Platform.runLater(() -> display.setText("0 c"));
            return;
        }
        pendingSeconds = seconds;
        targetTick = null;
        Platform.runLater(() -> display.setText(seconds + " c"));
    }

    public synchronized void cancel() {
        pendingSeconds = null;
        targetTick = null;
        Platform.runLater(() -> display.setText("—"));
    }

    @Override
    public void update(Subject subject) {
        int currentTick = subject.getState();
        Integer localTarget;

        synchronized (this) {
            if (pendingSeconds != null && targetTick == null) {
                targetTick = currentTick + pendingSeconds;
                pendingSeconds = null;
            }
            localTarget = targetTick;
        }

        if (localTarget == null) {
            return;
        }

        int remaining = localTarget - currentTick;
        if (remaining <= 0) {
            synchronized (this) {
                targetTick = null;
            }
            Platform.runLater(() -> display.setText("Таймер сработал"));
        } else {
            Platform.runLater(() -> display.setText(remaining + " c"));
        }
    }
}
