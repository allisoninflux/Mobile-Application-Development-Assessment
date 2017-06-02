package com.example.echols.studentschedule.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.activities.CourseDetailsActivity;
import com.example.echols.studentschedule.fragments.CourseInfoFragment;
import com.example.echols.studentschedule.fragments.ItemListFragment;
import com.example.echols.studentschedule.fragments.MentorInfoFragment;
import com.example.echols.studentschedule.fragments.NotesListFragment;

/**
 * View pager adapter for the course details activity
 */
public class CourseDetailsPagerAdapter extends FragmentPagerAdapter {
    private final static int PAGE_COUNT = 4;
    private final String[] tabTitles = new String[PAGE_COUNT];

    private final Fragment courseInfoFragment;
    private final Fragment assessmentListFragment;
    private final Fragment notesFragment;
    private final Fragment mentorFragment;

    public CourseDetailsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        CourseDetailsActivity activity = (CourseDetailsActivity)context;

        courseInfoFragment = CourseInfoFragment.newInstance(activity.getCourseId(), activity.getTermId());
        assessmentListFragment = ItemListFragment.newInstance(activity.getCourseId());
        notesFragment = NotesListFragment.newInstance(activity.getCourseId());
        mentorFragment = MentorInfoFragment.newInstance(activity.getCourseId());
        tabTitles[0] = context.getString(R.string.course_info);
        tabTitles[1] = context.getString(R.string.course_assessment_list);
        tabTitles[2] = context.getString(R.string.label_notes);
        tabTitles[3] = context.getString(R.string.label_mentor_info);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return courseInfoFragment;
            case 1:
                return assessmentListFragment;
            case 2:
                return notesFragment;
            case 3:
                return mentorFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }


}
