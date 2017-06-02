package com.example.echols.studentschedule.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.activities.TermDetailsActivity;
import com.example.echols.studentschedule.fragments.ItemListFragment;
import com.example.echols.studentschedule.fragments.TermInfoFragment;

/**
 * View pager adapter for the term details activity
 */
public class TermDetailsPagerAdapter extends FragmentPagerAdapter {
    private final static int PAGE_COUNT = 2;
    private final String[] tabTitles = new String[PAGE_COUNT];

    private final Fragment termInfoFragment;
    private final Fragment courseListFragment;

    public TermDetailsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        TermDetailsActivity activity = (TermDetailsActivity)context;

        termInfoFragment = TermInfoFragment.newInstance(activity.getTermId());
        courseListFragment = ItemListFragment.newInstance(activity.getTermId());
        tabTitles[0] = context.getString(R.string.term_info);
        tabTitles[1] = context.getString(R.string.term_course_list);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return termInfoFragment;
            case 1:
                return courseListFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
