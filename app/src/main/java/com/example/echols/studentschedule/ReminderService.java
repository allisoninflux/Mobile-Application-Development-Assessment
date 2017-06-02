package com.example.echols.studentschedule;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.example.echols.studentschedule.activities.AssessmentDetailsActivity;
import com.example.echols.studentschedule.activities.CourseDetailsActivity;
import com.example.echols.studentschedule.activities.TermDetailsActivity;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Term;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ReminderService extends JobService {

    private static final int REMINDER_JOB_ID = 33;

    private ScheduleDbHelper dbHelper;

    private Calendar tomorrow;
    private Calendar nextWeek;

    // these constants are used to insure that every notification id is unique
    private static final int TERM_ID_OFFSET = 10000;
    private static final int COURSE_ID_OFFSET = 20000;
    private static final int ASSESSMENT_ID_OFFSET = 30000;
    private static final int START_DAY_ID_OFFSET = 1000;
    private static final int START_WEEK_ID_OFFSET = 2000;
    private static final int END_DAY_ID_OFFSET = 3000;
    private static final int END_WEEK_ID_OFFSET = 4000;

    @Override
    public boolean onStartJob(JobParameters params) {
        // initialize the database helper
        dbHelper = ScheduleDbHelper.getInstance(this);
        mJobHandler.sendMessage(Message.obtain(mJobHandler, REMINDER_JOB_ID, params));
        // if this returns true, jobFinished(JobParameters params, boolean needsRescheduled) needs to be called
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        dbHelper.close();
        // note: return true to reschedule this job.
        mJobHandler.removeMessages(REMINDER_JOB_ID);
        return true;
    }

    private final Handler mJobHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            showReminders();
            jobFinished((JobParameters)msg.obj, false);
            return true;
        }

    });


    private void showReminders() {
        // get all the items from the database with a reminder
        Map<String, List<NavigationItem>> items = dbHelper.getReminders(this);

        // if there are no items with reminders, end the job
        if (items.size() == 0) {
            return;
        }

        // create a date to be used to check if a date is in the next 24 hours
        tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow.clear(Calendar.HOUR);
        tomorrow.clear(Calendar.MINUTE);
        tomorrow.clear(Calendar.SECOND);
        tomorrow.clear(Calendar.MILLISECOND);

        // create a date to be used to check if a date is in the next week
        nextWeek = Calendar.getInstance();
        nextWeek.add(Calendar.DATE, 7);
        nextWeek.clear(Calendar.HOUR);
        nextWeek.clear(Calendar.MINUTE);
        nextWeek.clear(Calendar.SECOND);
        nextWeek.clear(Calendar.MILLISECOND);

        // get which items the user wants notifications for
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean termAlerts = sharedPref.getBoolean(getString(R.string.pref_notify_terms_key), false);
        boolean courseAlerts = sharedPref.getBoolean(getString(R.string.pref_notify_courses_key), false);
        boolean assessmentAlerts = sharedPref.getBoolean(getString(R.string.pref_notify_assessments_key), false);

        if (termAlerts) {
            showTermReminders(items.get(getString(R.string.title_terms)));
        }
        if (courseAlerts) {
            showCourseReminders(items.get(getString(R.string.title_courses)));
        }
        if (assessmentAlerts) {
            showAssessmentReminders(items.get(getString(R.string.title_assessments)));
        }
    }

    private void showTermReminders(List<NavigationItem> items) {
        for (NavigationItem item : items) {
            Term term = (Term)item;

            int id = (int)term.getId() + TERM_ID_OFFSET;

            Calendar start = Calendar.getInstance();
            start.setTime(term.getStartDate());
            Calendar end = Calendar.getInstance();
            end.setTime(term.getEndDate());

            if (tomorrow.after(start) && term.isStartAlert()) {
                createNotification(id + START_DAY_ID_OFFSET, getString(R.string.term_reminder),
                        getString(R.string.term_reminder_start_day), term);
            } else if (nextWeek.after(start) && term.isStartAlert()) {
                createNotification(id + START_WEEK_ID_OFFSET, getString(R.string.term_reminder),
                        getString(R.string.term_reminder_start_week), term);
            }
            if (tomorrow.after(end) && term.isEndAlert()) {
                createNotification(id + END_DAY_ID_OFFSET, getString(R.string.term_reminder),
                        getString(R.string.term_reminder_end_day), term);
            } else if (nextWeek.after(end) && term.isEndAlert()) {
                createNotification(id + END_WEEK_ID_OFFSET, getString(R.string.term_reminder),
                        getString(R.string.term_reminder_end_week), term);
            }
        }
    }

    private void showCourseReminders(List<NavigationItem> items) {
        for (NavigationItem item : items) {
            Course course = (Course)item;

            int id = (int)course.getId() + COURSE_ID_OFFSET;

            Calendar start = Calendar.getInstance();
            start.setTime(course.getStartDate());
            Calendar end = Calendar.getInstance();
            end.setTime(course.getEndDate());

            if (tomorrow.after(start) && course.isStartAlert()) {
                createNotification(id + START_DAY_ID_OFFSET, getString(R.string.course_reminder),
                        getString(R.string.course_reminder_start_day), course);
            } else if (nextWeek.after(start) && course.isStartAlert()) {
                createNotification(id + START_WEEK_ID_OFFSET, getString(R.string.course_reminder),
                        getString(R.string.course_reminder_start_week), course);
            }
            if (tomorrow.after(end) && course.isEndAlert()) {
                createNotification(id + END_DAY_ID_OFFSET, getString(R.string.course_reminder),
                        getString(R.string.course_reminder_end_day), course);
            } else if (nextWeek.after(end) && course.isEndAlert()) {
                createNotification(id + END_WEEK_ID_OFFSET, getString(R.string.course_reminder),
                        getString(R.string.course_reminder_end_week), course);
            }
        }
    }

    private void showAssessmentReminders(List<NavigationItem> items) {
        for (NavigationItem item : items) {
            Assessment assessment = (Assessment)item;

            int id = (int)assessment.getId() + ASSESSMENT_ID_OFFSET;

            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(assessment.getDate());

            if (tomorrow.after(dueDate) && assessment.isAlert()) {
                createNotification(id + END_DAY_ID_OFFSET, getString(R.string.assessment_reminder),
                        getString(R.string.assessment_reminder_day), assessment);
            } else if (nextWeek.after(dueDate) && assessment.isAlert()) {
                createNotification(id + END_WEEK_ID_OFFSET, getString(R.string.assessment_reminder),
                        getString(R.string.assessment_reminder_week), assessment);
            }
        }
    }

    private void createNotification(int id, String title, String body, NavigationItem item) {
        // create an intent that will go to the correct activity
        Intent intent = null;
        if (item instanceof Term) {
            intent = new Intent(this, TermDetailsActivity.class);
            intent.putExtra(getString(R.string.intent_parent_id), item.getId());
        } else if (item instanceof Course) {
            intent = new Intent(this, CourseDetailsActivity.class);
            intent.putExtra(getString(R.string.intent_child_id), item.getId());
            intent.putExtra(getString(R.string.intent_parent_id), ((Course)item).getTerm().getId());
        } else if (item instanceof Assessment) {
            intent = new Intent(this, AssessmentDetailsActivity.class);
            intent.putExtra(getString(R.string.intent_child_id), item.getId());
            intent.putExtra(getString(R.string.intent_parent_id), ((Assessment)item).getCourse().getId());
        }

        int requestID = (int)System.currentTimeMillis(); // unique request id
        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // cancel the old intent and create a new one
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestID, intent, flags);

        // build the notification
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build();

        // send the notification to the notification manager
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}
