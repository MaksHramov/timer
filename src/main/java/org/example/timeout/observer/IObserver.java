package org.example.timeout.observer;

import org.example.timeout.subject.Subject;

public interface IObserver {
    void update(Subject subject);
}