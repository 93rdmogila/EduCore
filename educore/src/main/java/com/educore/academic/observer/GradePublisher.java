package com.educore.academic.observer;

import com.educore.academic.model.Grade;
import java.util.ArrayList;
import java.util.List;

public class GradePublisher {
    private final List<GradeObserver> observers = new ArrayList<>();

    public void subscribe(GradeObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(GradeObserver observer) {
        observers.remove(observer);
    }

    public void notifyGradeAdded(Grade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }

        for (GradeObserver observer : new ArrayList<>(observers)) {
            observer.onGradeAdded(grade);
        }
    }
}
