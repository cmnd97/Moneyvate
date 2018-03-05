package com.cmnd97.moneyvate;

import android.widget.ImageView;

/**
 * Created by cristi-mnd on 15.02.18.
 */

public class Task {
    String id;
    String status;
    String deadline;
    Tag tag;
    ImageView directionsView;


    public Task(String id, String status, String deadline, Tag tag) {
        this.id = id;
        this.status = status;
        this.deadline = deadline;
        this.tag = tag;
    }

    ImageView getDirectionsView() {
        return directionsView;
    }

    void setDirectionsView(ImageView directionsView) {
        this.directionsView = directionsView;
    }
}
