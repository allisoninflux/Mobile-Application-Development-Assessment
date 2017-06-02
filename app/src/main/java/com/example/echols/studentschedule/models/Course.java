package com.example.echols.studentschedule.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

/**
 * A course that the student is enrolled in
 */
public class Course implements NavigationItem, Comparable<Course> {

    private long id;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private boolean startAlert;
    private boolean endAlert;
    public static int length = 1;
    private StatusType status = StatusType.ENROLLED;
    public enum StatusType {
        ENROLLED, IN_PROGRESS, ASSESSMENT_SCHEDULED, ASSESSMENT_SUBMITTED, COMPLETE, DROPPED;

        @Override
        public String toString() {
            switch (this) {
                case ENROLLED: return "Enrolled";
                case IN_PROGRESS: return "In Progress";
                case ASSESSMENT_SCHEDULED: return "Assessment Scheduled";
                case ASSESSMENT_SUBMITTED: return "Assessment Submitted";
                case COMPLETE: return "Complete";
                case DROPPED: return "Dropped";
                default: throw new IllegalArgumentException("course status not set");
            }
        }

        public static StatusType getType(String value) {
            for(StatusType v : values())
                if(v.toString().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }

    }
    private Mentor mentor;
    private final Term term;

    /**
     * Create a new course
     * @param title         a title of the course
     * @param description   a description for the course
     * @param startDate     the date that the course begins
     * @param term          the term that this course is included in
     */
    public Course(@NonNull String title, @Nullable String description, @Nullable Date startDate, @NonNull Term term) {
        Calendar c = Calendar.getInstance();

        this.title = title;

        if(description ==  null) {
            this.description = "Type: " + status;
        } else {
            this.description = description;
        }

        if(startDate == null) {
            this.startDate = c.getTime();
        } else {
            this.startDate = startDate;
            c.setTime(startDate);
        }

        c.add(Calendar.MONTH, length);
        c.add(Calendar.DATE, -1);
        this.endDate = c.getTime();
        this.term = term;
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
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        if(startDate.compareTo(endDate) > 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(endDate);
            c.add(Calendar.MONTH, -length);
            c.add(Calendar.DATE, 1);
            this.startDate = c.getTime();
        } else {
            this.startDate = startDate;
        }
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        if(startDate.compareTo(endDate) > 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.MONTH, length);
            c.add(Calendar.DATE, -1);
            this.endDate = c.getTime();
        } else {
            this.endDate = endDate;
        }
    }

    public boolean isStartAlert() {
        return startAlert;
    }

    public void setStartAlert(boolean startAlert) {
        this.startAlert = startAlert;
    }

    public boolean isEndAlert() {
        return endAlert;
    }

    public void setEndAlert(boolean endAlert) {
        this.endAlert = endAlert;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType type) {
        this.status = type;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public void setMentor(Mentor mentor) {
        this.mentor = mentor;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return title + ": " + description;
    }

    @Override
    public int compareTo(@NonNull Course other) {
        return this.title.compareTo(other.getTitle());
    }
}
