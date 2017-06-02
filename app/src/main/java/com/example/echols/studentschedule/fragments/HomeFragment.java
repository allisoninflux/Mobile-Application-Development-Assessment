package com.example.echols.studentschedule.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Term;

import java.util.List;

public class HomeFragment extends Fragment {

    private ScheduleDbHelper dbHelper;

    private String currentTerm;
    private StringBuilder currentCourses;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = ScheduleDbHelper.getInstance(getActivity());

        Term term = dbHelper.getCurrentTerm();
        if (term != null) {
            currentTerm = term.getTitle();
        } else {
            currentTerm = getString(R.string.home_current_term_none);
        }

        currentCourses = new StringBuilder();

        List<NavigationItem> courses = dbHelper.getCurrentCourses();
        if (courses.size() > 0) {
            for (NavigationItem course : courses) {
                currentCourses.append(course.getTitle()).append("\n");
            }
        } else {
            currentCourses.append(getString(R.string.home_current_courses_none));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView termTextView = (TextView) view.findViewById(R.id.textCurrentTerm);
        TextView coursesTextView = (TextView) view.findViewById(R.id.textCurrentCourses);
        termTextView.setText(currentTerm);
        coursesTextView.setText(currentCourses.toString());

        Button button = (Button) view.findViewById(R.id.buttonGenerateData);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.generateDummyData(3, 4);
            }
        });

        return view;
    }

}
