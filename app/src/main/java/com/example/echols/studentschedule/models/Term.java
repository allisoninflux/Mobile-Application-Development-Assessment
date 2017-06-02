package com.example.echols.studentschedule.models;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A term that will include courses that the student is enrolled in
 */
public class Term implements NavigationItem, Comparable<Term> {

    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    private long id;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private boolean startAlert;
    private boolean endAlert;
    public static int length = 6;

    /**
     * Create a new term
     *
     * @param title     the title of the term
     * @param startDate the start date of the term
     */
    public Term(@NonNull String title, @NonNull Date startDate) {
        this.title = title;
        this.startDate = startDate;
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.MONTH, length);
        c.add(Calendar.DATE, -1);
        this.endDate = c.getTime();
        setDefaultDescription();
    }

    @Override
    public String toString() {
        return title + ": " + description;
    }

    @Override
    public int compareTo(@NonNull Term other) {
        return this.startDate.compareTo(other.getStartDate());
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
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
        // check if the current description is the default description before the date is changed
        boolean isDefaultDescription = isDefaultDescription();
        // change the date
        if(startDate.compareTo(endDate) > 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(endDate);
            c.add(Calendar.MONTH, -length);
            c.add(Calendar.DATE, 1);
            this.startDate = c.getTime();
        } else {
            this.startDate = startDate;
        }
        // if it was the default description, reset the default description
        if(isDefaultDescription) {
            setDefaultDescription();
        }
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        // check if the current description is the default description before the date is changed
        boolean isDefaultDescription = isDefaultDescription();
        // change the date
        if(startDate.compareTo(endDate) > 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.MONTH, length);
            c.add(Calendar.DATE, -1);
            this.endDate = c.getTime();
        } else {
            this.endDate = endDate;
        }
        // if it was the default description, reset the default description
        if (isDefaultDescription) {
            setDefaultDescription();
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

    /**
     * Set the default description based on the start and end dates of the Term
     */
    private void setDefaultDescription() {
        description = dateFormat.format(startDate) + " - " + dateFormat.format(endDate);
    }

    /**
     * Check if the current description is the default description
     * @return true if the current description is the default description, false if not
     */
    private boolean isDefaultDescription() {
        String temp = dateFormat.format(startDate) + " - " + dateFormat.format(endDate);
        return description.equals(temp);
    }

}