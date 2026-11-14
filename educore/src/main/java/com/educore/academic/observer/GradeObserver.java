package com.educore.academic.observer;

import com.educore.academic.model.Grade;

public interface GradeObserver {
    void onGradeAdded(Grade grade);
}
