package com.example.echols.studentschedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.Mentor;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Term;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class ScheduleDbHelper extends SQLiteOpenHelper {
    private static ScheduleDbHelper instance;

    /**
     * Instance initializer. Returns the copy in memory if one has already been created
     *
     * @param context the context in which to create
     * @return the ScheduleDbHelper instance
     */
    public static synchronized ScheduleDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ScheduleDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ScheduleDbHelper(Context context) {
        super(context, ScheduleDbContract.DATABASE_NAME, null, ScheduleDbContract.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScheduleDbContract.SQL_CREATE_TERM_TABLE);
        db.execSQL(ScheduleDbContract.SQL_CREATE_MENTOR_TABLE);
        db.execSQL(ScheduleDbContract.SQL_CREATE_COURSE_TABLE);
        db.execSQL(ScheduleDbContract.SQL_CREATE_ASSESSMENT_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ScheduleDbContract.SQL_DELETE_ASSESSMENT_TABLE);
        db.execSQL(ScheduleDbContract.SQL_DELETE_MENTOR_TABLE);
        db.execSQL(ScheduleDbContract.SQL_DELETE_COURSE_TABLE);
        db.execSQL(ScheduleDbContract.SQL_DELETE_TERM_TABLE);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        // make sure that foreign key constrains are enabled for this database
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Generate dummy data to show app functionality
     */
    @SuppressWarnings("SameParameterValue")
    public void generateDummyData(int numberOfTerms, int coursesPerTerm) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(ScheduleDbContract.SQL_DELETE_ASSESSMENT_TABLE);
        db.execSQL(ScheduleDbContract.SQL_DELETE_MENTOR_TABLE);
        db.execSQL(ScheduleDbContract.SQL_DELETE_COURSE_TABLE);
        db.execSQL(ScheduleDbContract.SQL_DELETE_TERM_TABLE);

        db.execSQL(ScheduleDbContract.SQL_CREATE_ASSESSMENT_TABLE);
        db.execSQL(ScheduleDbContract.SQL_CREATE_MENTOR_TABLE);
        db.execSQL(ScheduleDbContract.SQL_CREATE_COURSE_TABLE);
        db.execSQL(ScheduleDbContract.SQL_CREATE_TERM_TABLE);

        generateDummyMentors(db);
        generateDummyTerms(db, numberOfTerms);
        generateDummyCourses(db, coursesPerTerm);
        generateDummyAssessments(db);
    }
    private List<Term> terms;
    private void generateDummyTerms(SQLiteDatabase db, int numberOfTerms) {
        ContentValues values = new ContentValues();

        Calendar calendar = Calendar.getInstance();

        terms = new ArrayList<>();
        for (int t = 0; t < numberOfTerms; t++) {
            Term term = new Term("Term " + (t + 1), calendar.getTime());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_TITLE, term.getTitle());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_DESCRIPTION, term.getDescription());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_START_DATE, term.getStartDate().getTime());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_END_DATE, term.getEndDate().getTime());
            long id = db.insert(ScheduleDbContract.Terms.TABLE_NAME, null, values);
            term.setId(id);

            terms.add(term);

            calendar.add(Calendar.MONTH, 6);
        }

    }
    private List<Mentor> mentors;
    private void generateDummyMentors(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        mentors = new ArrayList<>();

        Mentor mentor1 = new Mentor("Some Guy", "123-456-7890", "email_1@address.com");
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NAME, mentor1.getName());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER, mentor1.getNumber());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL, mentor1.getEmail());
        long mentor1_ID = db.insert(ScheduleDbContract.Mentors.TABLE_NAME, null, values);
        mentor1.setId(mentor1_ID);
        mentors.add(mentor1);

        Mentor mentor2 = new Mentor("Another Guy", "987-654-3210", "email_2@address.com");
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NAME, mentor2.getName());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER, mentor2.getNumber());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL, mentor2.getEmail());
        long mentor2_ID = db.insert(ScheduleDbContract.Mentors.TABLE_NAME, null, values);
        mentor2.setId(mentor2_ID);
        mentors.add(mentor2);
    }
    private List<Course> courses;
    private void generateDummyCourses(SQLiteDatabase db, int coursesPerTerm) {
        ContentValues values = new ContentValues();

        Calendar calendar = Calendar.getInstance();

        courses = new ArrayList<>();
        for (Term term : terms) {
            for (int c = 0; c < coursesPerTerm; c++) {
                Mentor mentor = mentors.get(c % 2);
                Course course = new Course("Course " + (c + 1), "This course is in " + term.getTitle(), calendar.getTime(), term);
                course.setMentor(mentor);
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_TITLE, course.getTitle());
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_DESCRIPTION, course.getDescription());
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_START_DATE, course.getStartDate().getTime());
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_END_DATE, course.getEndDate().getTime());
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_STATUS, course.getStatus().toString());
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_MENTOR_ID, mentor.getId());
                values.put(ScheduleDbContract.Courses.COLUMN_NAME_TERM_ID, term.getId());
                long id = db.insert(ScheduleDbContract.Courses.TABLE_NAME, null, values);
                course.setId(id);

                courses.add(course);

                calendar.add(Calendar.MONTH, 1);
            }
        }
    }
    private void generateDummyAssessments(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        Calendar c = Calendar.getInstance();

        for (int a = 0; a < courses.size(); a++) {
            Course course = courses.get(a);
            c.setTime(course.getEndDate());
            Assessment assessment = new Assessment("Assessment " + (a + 1), Assessment.Type.PERFORMANCE, c.getTime(), course);
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_TITLE, assessment.getTitle());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_TYPE, assessment.getType().toString());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_DATE, assessment.getDate().getTime());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_COURSE_ID, course.getId());
            db.insert(ScheduleDbContract.Assessments.TABLE_NAME, null, values);
        }
    }

    /* GENERIC */
    @SuppressWarnings("UnusedReturnValue")
    public int deleteItems(List<NavigationItem> items) {
        // return 0 if the set was null or included no items
        if (items == null || items.isEmpty()) return 0;

        String tableName = null; // the name of the table to delete rows from
        String selection = null; // the 'where' part of the query
        String[] selectionArgs = new String[items.size()]; // the arguments for the query

        int index = 0;
        for (NavigationItem item : items) {
            if (index == 0) { // if it is the first (or only) item
                if (item instanceof Term) {
                    tableName = ScheduleDbContract.Terms.TABLE_NAME;
                    selection = ScheduleDbContract.Terms._ID + " IN (?";
                } else if (item instanceof Course) {
                    tableName = ScheduleDbContract.Courses.TABLE_NAME;
                    selection = ScheduleDbContract.Courses._ID + " IN (?";
                } else if (item instanceof Assessment) {
                    tableName = ScheduleDbContract.Assessments.TABLE_NAME;
                    selection = ScheduleDbContract.Assessments._ID + " IN (?";
                }
            } else { // for additional items
                selection += ",?";
            }
            // add the id to the selection arguments
            selectionArgs[index++] = String.valueOf(item.getId());
        }

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Issue SQL statement.
        return db.delete(tableName, selection + ")", selectionArgs);
    }

    public long addItem(NavigationItem item) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        long newRowId = 0;

        if (item instanceof Term) {
            Term term = (Term)item;
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_TITLE, term.getTitle());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_DESCRIPTION, term.getDescription());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_START_DATE, term.getStartDate().getTime());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_START_ALERT, term.isStartAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_END_DATE, term.getEndDate().getTime());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_END_ALERT, term.isEndAlert() ? 1 : 0);
            // Insert the new row, returning the primary key value of the new row
            newRowId = db.insert(ScheduleDbContract.Terms.TABLE_NAME, null, values);
        } else if (item instanceof Course) {
            Course course = (Course)item;
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_TITLE, course.getTitle());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_DESCRIPTION, course.getDescription());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_START_DATE, course.getStartDate().getTime());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_START_ALERT, course.isStartAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_END_DATE, course.getEndDate().getTime());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_END_ALERT, course.isEndAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_STATUS, course.getStatus().toString());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_MENTOR_ID, course.getMentor().getId());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_TERM_ID, course.getTerm().getId());
            // Insert the new row, returning the primary key value of the new row
            newRowId = db.insert(ScheduleDbContract.Courses.TABLE_NAME, null, values);
        } else if (item instanceof Assessment) {
            Assessment assessment = (Assessment)item;
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_TITLE, assessment.getTitle());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_DATE, assessment.getDate().getTime());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_ALERT, assessment.isAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_TYPE, assessment.getType().toString());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_COURSE_ID, assessment.getCourse().getId());
            // Insert the new row, returning the primary key value of the new row
            newRowId = db.insert(ScheduleDbContract.Assessments.TABLE_NAME, null, values);
        }
        return newRowId;
    }

    @SuppressWarnings("UnusedReturnValue")
    public int updateItem(NavigationItem item) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        int count = 0;

        if (item instanceof Term) {
            Term term = (Term)item;
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_TITLE, term.getTitle());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_DESCRIPTION, term.getDescription());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_START_DATE, term.getStartDate().getTime());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_START_ALERT, term.isStartAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_END_DATE, term.getEndDate().getTime());
            values.put(ScheduleDbContract.Terms.COLUMN_NAME_END_ALERT, term.isEndAlert() ? 1 : 0);

            // Which row to update, based on the title
            String selection = ScheduleDbContract.Terms._ID + " = ?";
            String[] selectionArgs = {String.valueOf(term.getId())};

            count = db.update(ScheduleDbContract.Terms.TABLE_NAME, values, selection, selectionArgs);

        } else if (item instanceof Course) {
            Course course = (Course)item;

            // get the mentor id or null if no mentor is set
            Long mentorId = (course.getMentor() == null) ? null : course.getMentor().getId();

            values.put(ScheduleDbContract.Courses.COLUMN_NAME_TITLE, course.getTitle());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_DESCRIPTION, course.getDescription());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_START_DATE, course.getStartDate().getTime());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_START_ALERT, course.isStartAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_END_DATE, course.getEndDate().getTime());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_END_ALERT, course.isEndAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_STATUS, course.getStatus().toString());
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_MENTOR_ID, mentorId);
            values.put(ScheduleDbContract.Courses.COLUMN_NAME_TERM_ID, course.getTerm().getId());

            // Which row to update, based on the title
            String selection = ScheduleDbContract.Courses._ID + " = ?";
            String[] selectionArgs = {String.valueOf(course.getId())};

            count = db.update(ScheduleDbContract.Courses.TABLE_NAME, values, selection, selectionArgs);
        }
        else if (item instanceof Assessment) {
            Assessment assessment = (Assessment)item;

            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_TITLE, assessment.getTitle());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_DATE, assessment.getDate().getTime());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_ALERT, assessment.isAlert() ? 1 : 0);
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_TYPE, assessment.getType().toString());
            values.put(ScheduleDbContract.Assessments.COLUMN_NAME_COURSE_ID, assessment.getCourse().getId());

            // Which row to update, based on the title
            String selection = ScheduleDbContract.Assessments._ID + " = ?";
            String[] selectionArgs = {String.valueOf(assessment.getId())};

            count = db.update(ScheduleDbContract.Assessments.TABLE_NAME, values, selection, selectionArgs);
        }
        return count;
    }

    public Map<String, List<NavigationItem>> getReminders(Context context) {

        Map<String, List<NavigationItem>> results = new HashMap<>();

        results.put(context.getString(R.string.title_terms), getTermsWithAlerts());

        results.put(context.getString(R.string.title_courses), getCoursesWithAlerts());

        results.put(context.getString(R.string.title_assessments), getAssessmentsWithAlerts());

        return results;
    }

    /* TERMS */
    private List<NavigationItem> getTermsFromSelection(String selection, String[] selectionArgs) {
        List<NavigationItem> results = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = ScheduleDbContract.Terms.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                ScheduleDbContract.Terms._ID,
                ScheduleDbContract.Terms.COLUMN_NAME_TITLE,
                ScheduleDbContract.Terms.COLUMN_NAME_DESCRIPTION,
                ScheduleDbContract.Terms.COLUMN_NAME_START_DATE,
                ScheduleDbContract.Terms.COLUMN_NAME_START_ALERT,
                ScheduleDbContract.Terms.COLUMN_NAME_END_DATE,
                ScheduleDbContract.Terms.COLUMN_NAME_END_ALERT
        };

        // define the sort order
        String sortOrder = ScheduleDbContract.Terms.COLUMN_NAME_START_DATE + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // go through all query results and add to output
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Terms._ID));
            String title = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Terms.COLUMN_NAME_TITLE));
            String description = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Terms.COLUMN_NAME_DESCRIPTION));
            long start = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Terms.COLUMN_NAME_START_DATE));
            int startAlert = c.getInt(c.getColumnIndexOrThrow(ScheduleDbContract.Terms.COLUMN_NAME_START_ALERT));
            long end = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Terms.COLUMN_NAME_END_DATE));
            int endAlert = c.getInt(c.getColumnIndexOrThrow(ScheduleDbContract.Terms.COLUMN_NAME_END_ALERT));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(start);
            Date startDate = calendar.getTime();
            calendar.setTimeInMillis(end);
            Date endDate = calendar.getTime();

            Term term = new Term(title, startDate);
            term.setId(id);
            term.setDescription(description);
            term.setEndDate(endDate);
            term.setStartAlert(startAlert == 1);
            term.setEndAlert(endAlert == 1);

            results.add(term);
        }
        c.close();

        return results;
    }

    public List<NavigationItem> getAllTerms() {
        return getTermsFromSelection(null, null);
    }

    public Term getTermById(long id) {
        // define the query filter
        String selection = ScheduleDbContract.Terms._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        List<NavigationItem> results = getTermsFromSelection(selection, selectionArgs);

        if(results.size() > 0 ) {
            return (Term)results.get(0);
        } else {
            return null;
        }

    }

    private List<NavigationItem> getTermsWithAlerts() {
        String selection = ScheduleDbContract.Terms.COLUMN_NAME_START_ALERT + " = ? OR " +
                ScheduleDbContract.Terms.COLUMN_NAME_END_ALERT + " = ?";
        String[] selectionArgs = {String.valueOf(1), String.valueOf(1)};

        return getTermsFromSelection(selection, selectionArgs);
    }

    public Term getCurrentTerm() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTime().getTime();

        // define the query filter
        String selection = "? BETWEEN " + ScheduleDbContract.Terms.COLUMN_NAME_START_DATE +
                " AND " + ScheduleDbContract.Terms.COLUMN_NAME_END_DATE;
        String[] selectionArgs = {String.valueOf(now)};

        List<NavigationItem> results = getTermsFromSelection(selection, selectionArgs);

        if(results.size() > 0 ) {
            return (Term)results.get(0);
        } else {
            return null;
        }
    }

    /* COURSES */
    private List<NavigationItem> getCoursesFromSelection(String selection, String[] selectionArgs) {
        List<NavigationItem> results = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = ScheduleDbContract.Courses.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                ScheduleDbContract.Courses._ID,
                ScheduleDbContract.Courses.COLUMN_NAME_TITLE,
                ScheduleDbContract.Courses.COLUMN_NAME_DESCRIPTION,
                ScheduleDbContract.Courses.COLUMN_NAME_START_DATE,
                ScheduleDbContract.Courses.COLUMN_NAME_START_ALERT,
                ScheduleDbContract.Courses.COLUMN_NAME_END_DATE,
                ScheduleDbContract.Courses.COLUMN_NAME_END_ALERT,
                ScheduleDbContract.Courses.COLUMN_NAME_STATUS,
                ScheduleDbContract.Courses.COLUMN_NAME_MENTOR_ID,
                ScheduleDbContract.Courses.COLUMN_NAME_TERM_ID
        };

        // define the sort order
        String sortOrder = ScheduleDbContract.Courses.COLUMN_NAME_START_DATE + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // go through all query results and add to output
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Courses._ID));
            String title = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_TITLE));
            String description = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_DESCRIPTION));
            long start = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_START_DATE));
            int startAlert = c.getInt(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_START_ALERT));
            long end = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_END_DATE));
            int endAlert = c.getInt(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_END_ALERT));
            String status = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_STATUS));
            long mentor_id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_MENTOR_ID));
            long term_id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Courses.COLUMN_NAME_TERM_ID));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(start);
            Date startDate = calendar.getTime();
            calendar.setTimeInMillis(end);
            Date endDate = calendar.getTime();

            Term term = getTermById(term_id);

            Course course = new Course(title, description, startDate, term);
            course.setId(id);
            course.setEndDate(endDate);
            course.setMentor(getMentorById(mentor_id));
            course.setStatus(Course.StatusType.getType(status));
            course.setStartAlert(startAlert == 1);
            course.setEndAlert(endAlert == 1);

            results.add(course);
        }
        c.close();

        return results;
    }

    public List<NavigationItem> getAllCourses() {
        return getCoursesFromSelection(null, null);
    }

    public List<NavigationItem> getCoursesInTerm(long termId) {
        String selection = ScheduleDbContract.Courses.COLUMN_NAME_TERM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(termId)};
        return getCoursesFromSelection(selection, selectionArgs);
    }

    public Course getCourseById(long id) {
        String selection = ScheduleDbContract.Courses._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        List<NavigationItem> results = getCoursesFromSelection(selection, selectionArgs);

        if(results.size() > 0 ) {
            return (Course)results.get(0);
        } else {
            return null;
        }
    }

    private List<NavigationItem> getCoursesWithAlerts() {
        String selection = ScheduleDbContract.Courses.COLUMN_NAME_START_ALERT + " = ? OR " +
                ScheduleDbContract.Courses.COLUMN_NAME_END_ALERT + " = ?";
        String[] selectionArgs = {String.valueOf(1), String.valueOf(1)};

        return getCoursesFromSelection(selection, selectionArgs);
    }

    public List<NavigationItem> getCurrentCourses() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTime().getTime();

        // define the query filter
        String selection = "? BETWEEN " + ScheduleDbContract.Courses.COLUMN_NAME_START_DATE +
                " AND " + ScheduleDbContract.Courses.COLUMN_NAME_END_DATE;
        String[] selectionArgs = {String.valueOf(now)};

        return getCoursesFromSelection(selection, selectionArgs);
    }

    /* ASSESSMENTS */
    private List<NavigationItem> getAssessmentsFromSelection(String selection, String[] selectionArgs) {
        List<NavigationItem> results = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = ScheduleDbContract.Assessments.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                ScheduleDbContract.Assessments._ID,
                ScheduleDbContract.Assessments.COLUMN_NAME_TITLE,
                ScheduleDbContract.Assessments.COLUMN_NAME_TYPE,
                ScheduleDbContract.Assessments.COLUMN_NAME_DATE,
                ScheduleDbContract.Assessments.COLUMN_NAME_ALERT,
                ScheduleDbContract.Assessments.COLUMN_NAME_COURSE_ID
        };

        // define the sort order
        String sortOrder = ScheduleDbContract.Assessments.COLUMN_NAME_DATE + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // go through all query results and add to output
        while (c.moveToNext()) {

            long id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Assessments._ID));
            String title = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Assessments.COLUMN_NAME_TITLE));
            String type = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Assessments.COLUMN_NAME_TYPE));
            long dateValue = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Assessments.COLUMN_NAME_DATE));
            int alert = c.getInt(c.getColumnIndexOrThrow(ScheduleDbContract.Assessments.COLUMN_NAME_ALERT));
            long course_id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Assessments.COLUMN_NAME_COURSE_ID));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateValue);
            Date date = calendar.getTime();

            Assessment assessment = new Assessment(title, Assessment.Type.getType(type), date, getCourseById(course_id));
            assessment.setId(id);
            assessment.setAlert(alert == 1);

            results.add(assessment);
        }
        c.close();

        return results;
    }

    public List<NavigationItem> getAllAssessments() {
        return getAssessmentsFromSelection(null, null);
    }

    public List<NavigationItem> getAssessmentsInCourse(long courseId) {
        String selection = ScheduleDbContract.Assessments.COLUMN_NAME_COURSE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(courseId)};
        return getAssessmentsFromSelection(selection, selectionArgs);
    }

    public Assessment getAssessmentById(long id) {
        String selection = ScheduleDbContract.Assessments._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        List<NavigationItem> results = getAssessmentsFromSelection(selection, selectionArgs);

        if(results.size() > 0 ) {
            return (Assessment)results.get(0);
        } else {
            return null;
        }
    }

    private List<NavigationItem> getAssessmentsWithAlerts() {
        String selection = ScheduleDbContract.Assessments.COLUMN_NAME_ALERT + " = ?";
        String[] selectionArgs = {String.valueOf(1)};

        return getAssessmentsFromSelection(selection, selectionArgs);
    }

    /* MENTORS */
    public List<Mentor> getAllMentors() {
        List<Mentor> results = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = ScheduleDbContract.Mentors.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                ScheduleDbContract.Mentors._ID,
                ScheduleDbContract.Mentors.COLUMN_NAME_NAME,
                ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER,
                ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL
        };

        // define the sort order
        String sortOrder = ScheduleDbContract.Mentors.COLUMN_NAME_NAME + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, null, null, null, null, sortOrder);

        // go through all query results and add to output
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors._ID));
            String name = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_NAME));
            String number = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER));
            String email = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL));

            Mentor mentor = new Mentor(name, number, email);
            mentor.setId(id);

            results.add(mentor);
        }
        c.close();

        return results;
    }

    public long addMentor(Mentor mentor) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NAME, mentor.getName());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER, mentor.getNumber());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL, mentor.getEmail());

        return db.insert(ScheduleDbContract.Mentors.TABLE_NAME, null, values);
    }

    private Mentor getMentorById(long id) {
        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = ScheduleDbContract.Mentors.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                ScheduleDbContract.Mentors._ID,
                ScheduleDbContract.Mentors.COLUMN_NAME_NAME,
                ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER,
                ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL,
        };

        // define the query filter
        String selection = ScheduleDbContract.Mentors._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        // define the sort order
        String sortOrder = ScheduleDbContract.Mentors._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result from the query
        Mentor mentor = null;
        while (c.moveToNext()) {
            String mentorName = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_NAME));
            String mentorNumber = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER));
            String mentorEmail = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL));

            mentor = new Mentor(mentorName, mentorNumber, mentorEmail);
            mentor.setId(id);
        }
        c.close();

        return mentor;
    }

    @SuppressWarnings("UnusedReturnValue")
    public int updateMentor(Mentor mentor) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NAME, mentor.getName());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER, mentor.getNumber());
        values.put(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL, mentor.getEmail());

        // Which row to update, based on the title
        String selection = ScheduleDbContract.Mentors._ID + " = ?";
        String[] selectionArgs = {String.valueOf(mentor.getId())};

        return db.update(ScheduleDbContract.Mentors.TABLE_NAME, values, selection, selectionArgs);
    }

    public Mentor findMentorByName(String name) {
        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = ScheduleDbContract.Mentors.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                ScheduleDbContract.Mentors._ID,
                ScheduleDbContract.Mentors.COLUMN_NAME_NAME,
                ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER,
                ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL,
        };

        // define the query filter
        String selection = ScheduleDbContract.Mentors.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = {name};

        // define the sort order
        String sortOrder = ScheduleDbContract.Mentors._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result from the query
        Mentor mentor = null;
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors._ID));
            String mentorNumber = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_NUMBER));
            String mentorEmail = c.getString(c.getColumnIndexOrThrow(ScheduleDbContract.Mentors.COLUMN_NAME_EMAIL));

            mentor = new Mentor(name, mentorNumber, mentorEmail);
            mentor.setId(id);
        }
        c.close();

        return mentor;
    }

}