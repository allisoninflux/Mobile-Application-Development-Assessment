package com.example.echols.studentschedule.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.activities.AssessmentDetailsActivity;
import com.example.echols.studentschedule.fragments.AssessmentInfoFragment;
import com.example.echols.studentschedule.fragments.NotesListFragment;

/**
 * View pager adapter for the assessment details activity
 */
public class AssessmentDetailsPagerAdapter extends FragmentPagerAdapter {
    private final static int PAGE_COUNT = 2;
    private final String[] tabTitles = new String[PAGE_COUNT];

    private final Fragment assessmentInfoFragment;
    private final Fragment notesFragment;

    public AssessmentDetailsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        AssessmentDetailsActivity activity = (AssessmentDetailsActivity)context;

        assessmentInfoFragment = AssessmentInfoFragment.newInstance(activity.getAssessmentId(), activity.getCourseId());
        notesFragment = NotesListFragment.newInstance(activity.getAssessmentId());
        tabTitles[0] = context.getString(R.string.assessment_info);
        tabTitles[1] = context.getString(R.string.label_notes);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return assessmentInfoFragment;
            case 1:
                return notesFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }


}
