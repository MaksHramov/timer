package org.example.timeout.subject;

import org.example.timeout.observer.IObserver;

import java.time.Instant;

public interface Subject {
    void attach(IObserver obs);
    void detach(IObserver obs);
    void notifyAllObservers();
    int getState();
    Instant getUtcTime();
}
