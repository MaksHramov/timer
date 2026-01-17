package org.example.timeout.observer;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.example.timeout.subject.Subject;

public record StopwatchObserver(Label display) implements IObserver {

    @Override
    public void update(Subject subject) {
        int seconds = subject.getState();
        int minutes = seconds / 60;
        int secs = seconds % 60;
        String formatted = minutes >= 60
                ? String.format("%02d:%02d:%02d", minutes / 60, minutes % 60, secs)
                : String.format("%02d:%02d", minutes, secs);

        Platform.runLater(() -> display.setText(formatted));
    }
}
