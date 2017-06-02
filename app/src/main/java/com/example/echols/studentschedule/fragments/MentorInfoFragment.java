package com.example.echols.studentschedule.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.Mentor;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link OnMentorInfoListener} interface to handle interaction events.
 * Use the {@link MentorInfoFragment#newInstance} factory method to create an instance of this fragment.
 */
public class MentorInfoFragment extends Fragment {

    private static final String ARG_COURSE_ID = "course_id";

    private Context context; // the context that contains this fragment
    private ScheduleDbHelper dbHelper; // the database helper object

    private long courseId; // the course id that for the currently displayed mentor
    private Mentor mentor; // the mentor displayed
    private final List<CharSequence> mentorNames = new ArrayList<>(); // a list of all the mentors in the database

    private EditText mentorNameEditText;
    private EditText mentorNumberEditText;
    private EditText mentorEmailEditText;

    public MentorInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param courseId the course id for this course
     * @return A new instance of fragment MentorInfoFragment.
     */
    public static MentorInfoFragment newInstance(long courseId) {
        MentorInfoFragment fragment = new MentorInfoFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_COURSE_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getLong(ARG_COURSE_ID);
        }

        // get the context of this fragment
        context = getActivity();

        // get database helper
        dbHelper = ScheduleDbHelper.getInstance(context);

        List<Mentor> mentors = dbHelper.getAllMentors();
        for (Mentor mentor : mentors) {
            mentorNames.add(mentor.toString());
        }
        mentorNames.add(getString(R.string.course_add_new_mentor));

        // get get the mentor from this course
        Course course = dbHelper.getCourseById(courseId);
        mentor = course.getMentor();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mentor_info, container, false);

        mentorNameEditText = (EditText)view.findViewById(R.id.editTextMentorName);
        mentorNumberEditText = (EditText)view.findViewById(R.id.editTextMentorNumber);
        mentorEmailEditText = (EditText)view.findViewById(R.id.editTextMentorEmail);
        mentorNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && mentor == null) {
                    mentor = new Mentor();
                }
                if (!s.toString().isEmpty()) {
                    mentor.setName(s.toString());
                    listener.updateMentor(mentor);
                }
            }
        });
        mentorNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && mentor == null) {
                    mentor = new Mentor();
                }
                if (!s.toString().isEmpty()) {
                    mentor.setNumber(s.toString());
                    listener.updateMentor(mentor);
                }
            }
        });
        mentorEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && mentor == null) {
                    mentor = new Mentor();
                }
                if (!s.toString().isEmpty()) {
                    mentor.setEmail(s.toString());
                    listener.updateMentor(mentor);
                }
            }
        });

        Spinner mentorSpinner = (Spinner)view.findViewById(R.id.courseMentorSpinner);

        // setup mentor spinner adapter and initial value
        ArrayAdapter<CharSequence> adapterMentor = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, mentorNames);
        adapterMentor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mentorSpinner.setAdapter(adapterMentor);
        if (mentor != null) {
            mentorSpinner.setSelection(adapterMentor.getPosition(mentor.toString()));
        } else {
            mentorSpinner.setSelection(adapterMentor.getPosition(getString(R.string.course_add_new_mentor)));
        }

        mentorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = parent.getSelectedItem().toString();

                if (value.equals(getString(R.string.course_add_new_mentor))) {
                    mentor = null;
                    mentorNameEditText.setText(null);
                    mentorNumberEditText.setText(null);
                    mentorEmailEditText.setText(null);
                } else {
                    mentor = dbHelper.findMentorByName(value);
                    mentorNameEditText.setText(mentor.getName());
                    mentorNumberEditText.setText(mentor.getNumber());
                    mentorEmailEditText.setText(mentor.getEmail());
                }
                listener.updateMentor(mentor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMentorInfoListener) {
            listener = (OnMentorInfoListener)context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMentorInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private OnMentorInfoListener listener;

    public interface OnMentorInfoListener {
        void updateMentor(Mentor mentor);
    }
}
