package org.example.timeout.subject;

import org.example.timeout.observer.IObserver;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimeServer implements Subject {

    private static final long TICK_PERIOD_MS = 1000L;

    private final Timer timer = new Timer("time-server", true);
    private TimerTask task;

    private final List<IObserver> observers = new ArrayList<>();

    private int timeState = 0;
    private Instant referenceUtc;
    private Instant lastTickUtc;

    public synchronized void start() {
        if (task != null) {
            return;
        }
        referenceUtc = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        lastTickUtc = referenceUtc;
        task = new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        };
        timer.scheduleAtFixedRate(task, 0, TICK_PERIOD_MS);
    }

    public synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            timer.purge();
        }
    }

    public synchronized void reset() {
        timeState = 0;
        referenceUtc = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        lastTickUtc = referenceUtc;
        notifyAllObservers();
    }

    private synchronized void tick() {
        Instant nowUtc = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        lastTickUtc = nowUtc;
        if (referenceUtc == null) {
            referenceUtc = nowUtc;
        }
        timeState = (int) Duration.between(referenceUtc, nowUtc).getSeconds();
        notifyAllObservers();
    }

    @Override
    public synchronized int getState() {
        return timeState;
    }

    @Override
    public synchronized Instant getUtcTime() {
        if (lastTickUtc != null) {
            return lastTickUtc;
        }
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public synchronized void attach(IObserver obs) {
        observers.add(obs);
    }

    @Override
    public synchronized void detach(IObserver obs) {
        observers.remove(obs);
    }

    @Override
    public void notifyAllObservers() {
        List<IObserver> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(observers);
        }
        for (IObserver obs : snapshot) {
            obs.update(this);
        }
    }
}
