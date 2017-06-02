package com.example.echols.studentschedule.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

/**
 * An assessment that the student is included an a student course
 */
public class Assessment implements NavigationItem {

    private long id;
    private String title;
    private Date date;
    private boolean alert;

    private Type type = Type.OBJECTIVE;
    public enum Type {
        OBJECTIVE, PERFORMANCE;

        @Override
        public String toString() {
            switch (this) {
                case OBJECTIVE: return "Objective";
                case PERFORMANCE: return "Performance";
                default: throw new IllegalArgumentException("assessment type not set");
            }
        }

        public static Type getType(String value) {
            for(Type v : values())
                if(v.toString().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }
    }
    private final Course course;

    /**
     * Create a new assessment
     * @param title     a title for the assessment
     * @param type      the type of assessment
     * @param date      the date the assessment is due
     * @param course    the course that the assessment belongs to
     */
    public Assessment(@NonNull String title, @Nullable Type type, @Nullable Date date, @NonNull Course course) {
        this.title = title;
        if(date == null) {
            Calendar c = Calendar.getInstance();
            this.date = c.getTime();
        } else {
            this.date = date;
        }
        this.course = course;
        setType(type);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return type.toString();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if(type == null) {
            this.type = Type.PERFORMANCE;
        } else {
            this.type = type;
        }
    }

    public Course getCourse() {
        return course;
    }

    @Override
    public String toString() {
        return title;
    }
}
