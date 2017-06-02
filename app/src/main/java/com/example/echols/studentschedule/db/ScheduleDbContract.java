package com.example.echols.studentschedule.db;

import android.provider.BaseColumns;

final class ScheduleDbContract {
    private ScheduleDbContract() {}

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "Schedule.db";

    /* ------------------------------------------- TERMS ------------------------------------------- */
    static class Terms implements BaseColumns {
        static final String TABLE_NAME = "term";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_DESCRIPTION = "description";
        static final String COLUMN_NAME_START_DATE = "start_date";
        static final String COLUMN_NAME_START_ALERT = "start_alert";
        static final String COLUMN_NAME_END_DATE = "end_date";
        static final String COLUMN_NAME_END_ALERT = "end_alert";
    }
    static final String SQL_CREATE_TERM_TABLE =
            "CREATE TABLE " + Terms.TABLE_NAME + " (" +
                    Terms._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Terms.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                    Terms.COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    Terms.COLUMN_NAME_START_DATE + " INTEGER, " +
                    Terms.COLUMN_NAME_START_ALERT + " INTEGER NOT NULL DEFAULT 0, " +
                    Terms.COLUMN_NAME_END_DATE + " INTEGER, " +
                    Terms.COLUMN_NAME_END_ALERT + " INTEGER NOT NULL DEFAULT 0)";
    static final String SQL_DELETE_TERM_TABLE = "DROP TABLE IF EXISTS " + Terms.TABLE_NAME;

    /* ------------------------------------------- COURSES ------------------------------------------- */
    static class Courses implements BaseColumns {
        static final String TABLE_NAME = "course";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_DESCRIPTION = "description";
        static final String COLUMN_NAME_START_DATE = "start_date";
        static final String COLUMN_NAME_START_ALERT = "start_alert";
        static final String COLUMN_NAME_END_DATE = "end_date";
        static final String COLUMN_NAME_END_ALERT = "end_alert";
        static final String COLUMN_NAME_STATUS = "status";
        static final String COLUMN_NAME_MENTOR_ID = "mentor_id";
        static final String COLUMN_NAME_TERM_ID = "intent_parent_id";
    }
    static final String SQL_CREATE_COURSE_TABLE =
            "CREATE TABLE " + Courses.TABLE_NAME + " (" +
                    Courses._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Courses.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                    Courses.COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    Courses.COLUMN_NAME_START_DATE + " INTEGER, " +
                    Courses.COLUMN_NAME_START_ALERT + " INTEGER NOT NULL DEFAULT 0, " +
                    Courses.COLUMN_NAME_END_DATE + " INTEGER, " +
                    Courses.COLUMN_NAME_END_ALERT + " INTEGER NOT NULL DEFAULT 0, " +
                    Courses.COLUMN_NAME_STATUS + " TEXT, " +
                    Courses.COLUMN_NAME_MENTOR_ID + " INTEGER, " +
                    Courses.COLUMN_NAME_TERM_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + Courses.COLUMN_NAME_MENTOR_ID + ") " +
                    "REFERENCES " + Mentors.TABLE_NAME + "(" + Mentors._ID + ") ON DELETE SET NULL, " +
                    "FOREIGN KEY (" + Courses.COLUMN_NAME_TERM_ID + ") " +
                    "REFERENCES " + Terms.TABLE_NAME + "(" + Terms._ID + ") ON DELETE RESTRICT)";
    static final String SQL_DELETE_COURSE_TABLE = "DROP TABLE IF EXISTS " + Courses.TABLE_NAME;

    /* ------------------------------------------- ASSESSMENTS ------------------------------------------- */
    static class Assessments implements BaseColumns {
        static final String TABLE_NAME = "assessment";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_DATE = "date";
        static final String COLUMN_NAME_ALERT = "alert";
        static final String COLUMN_NAME_COURSE_ID = "course_id";
    }
    static final String SQL_CREATE_ASSESSMENT_TABLE =
            "CREATE TABLE " + Assessments.TABLE_NAME + " (" +
                    Assessments._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Assessments.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                    Assessments.COLUMN_NAME_TYPE + " TEXT, " +
                    Assessments.COLUMN_NAME_DATE + " INTEGER, " +
                    Assessments.COLUMN_NAME_ALERT + " INTEGER NOT NULL DEFAULT 0, " +
                    Assessments.COLUMN_NAME_COURSE_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + Assessments.COLUMN_NAME_COURSE_ID + ") " +
                    "REFERENCES " + Courses.TABLE_NAME + "(" + Courses._ID + ") ON DELETE CASCADE)";
    static final String SQL_DELETE_ASSESSMENT_TABLE = "DROP TABLE IF EXISTS " + Assessments.TABLE_NAME;

    /* ------------------------------------------- MENTORS ------------------------------------------- */
    static class Mentors implements BaseColumns {
        static final String TABLE_NAME = "mentor";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_NUMBER = "number";
        static final String COLUMN_NAME_EMAIL = "email";
    }
    static final String SQL_CREATE_MENTOR_TABLE =
            "CREATE TABLE " + Mentors.TABLE_NAME + " (" +
                    Mentors._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Mentors.COLUMN_NAME_NAME + " TEXT UNIQUE, " +
                    Mentors.COLUMN_NAME_NUMBER + " TEXT, " +
                    Mentors.COLUMN_NAME_EMAIL + " TEXT)";
    static final String SQL_DELETE_MENTOR_TABLE = "DROP TABLE IF EXISTS " + Mentors.TABLE_NAME;

}
