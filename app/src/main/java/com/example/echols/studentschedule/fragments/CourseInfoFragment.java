package com.example.echols.studentschedule.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.activities.MainActivity;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.dialogs.DatePickerFragment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.Mentor;
import com.example.echols.studentschedule.models.Term;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link CourseInfoFragment.OnCourseInfoListener} interface to handle interaction events.
 * Use the {@link CourseInfoFragment#newInstance} factory method to create an instance of this fragment.
 */
public class CourseInfoFragment extends Fragment {

    private Context context;
    private View view;
    private ScheduleDbHelper dbHelper;

    private Term term;
    private long termId;
    private long courseId;
    private String courseTitle;
    private String courseDescription;
    private Date startDate;
    private Date endDate;
    private boolean startAlert;
    private boolean endAlert;
    private Course.StatusType status;

    private Button startButton;
    private Button endButton;

    private DatePickerFragment startDatePicker;
    private DatePickerFragment endDatePicker;

    public CourseInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param courseId the course id for this course
     * @param termId   the term id for the term that this course is in
     * @return A new instance of fragment CourseInfoFragment.
     */
    public static CourseInfoFragment newInstance(long courseId, long termId) {
        CourseInfoFragment fragment = new CourseInfoFragment();
        Bundle args = new Bundle();
        args.putLong(MainActivity.ARG_CHILD_ID, courseId);
        args.putLong(MainActivity.ARG_PARENT_ID, termId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getLong(MainActivity.ARG_CHILD_ID);
            termId = getArguments().getLong(MainActivity.ARG_PARENT_ID);
        }

        // get the context of this fragment
        context = getActivity();

        // get database helper
        dbHelper = ScheduleDbHelper.getInstance(context);

        // get all of the course data
        setCourseData(dbHelper.getCourseById(courseId));

        startDatePicker = new DatePickerFragment();
        startDatePicker.onSetDatePickerListener(new DatePickerFragment.DatePickerListener() {
            @Override
            public void setDateFromPicker(Date date) {
                setStartDate(date);
            }
        });

        endDatePicker = new DatePickerFragment();
        endDatePicker.onSetDatePickerListener(new DatePickerFragment.DatePickerListener() {
            @Override
            public void setDateFromPicker(Date date) {
                setEndDate(date);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_course_info, container, false);

        // initialize the term that this course is in
        TextView termTitle = (TextView) view.findViewById(R.id.textViewTerm);
        termTitle.setText(term.getTitle());

        // EditText Views
        EditText titleEditText = (EditText) view.findViewById(R.id.editTextCourseTitle);
        EditText descriptionEditText = (EditText) view.findViewById(R.id.editTextCourseDescription);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String title = s.toString();
                courseTitle = title;
                listener.updateCourseTitle(title);
            }
        });
        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String description = s.toString();
                courseDescription = description;
                listener.updateCourseDescription(description);
            }
        });
        titleEditText.setText(courseTitle);
        descriptionEditText.setText(courseDescription);

        // status spinner
        Spinner statusSpinner = (Spinner) view.findViewById(R.id.courseStatusSpinner);

        // setup status spinner adapter and initial value
        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(context, R.array.course_status_array, android.R.layout.simple_spinner_item);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapterStatus);
        statusSpinner.setSelection(adapterStatus.getPosition(status.toString()));

        // setup listeners on both adapters
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String statusString = parent.getSelectedItem().toString();
                status = Course.StatusType.getType(statusString);
                listener.updateStatus(status);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setupSwitches();

        setupButtons();

        return view;
    }

    /**
     * Set the course info in the fragment
     *
     * @param course the course displayed in this fragment
     */
    private void setCourseData(Course course) {
        if (course == null) {
            term = dbHelper.getTermById(termId);

            Calendar c = Calendar.getInstance();
            startDate = c.getTime();
            course = new Course(getString(R.string.course_new_title), getString(R.string.course_new_description), startDate, term);
            course.setMentor(new Mentor());
        } else {
            term = course.getTerm();
            startDate = course.getStartDate();
        }

        courseTitle = course.getTitle();
        courseDescription = course.getDescription();
        endDate = course.getEndDate();
        status = course.getStatus();
        startAlert = course.isStartAlert();
        endAlert = course.isEndAlert();

    }

    /**
     * Setup the buttons used in this fragment
     */
    private void setupButtons() {
        startButton = (Button) view.findViewById(R.id.buttonCourseStart);
        endButton = (Button) view.findViewById(R.id.buttonCourseEnd);
        startButton.setText(MainActivity.FORMATTER.format(startDate));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStartDate();
            }
        });
        endButton.setText(MainActivity.FORMATTER.format(endDate));
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeEndDate();
            }
        });
    }

    /**
     * Setup the switches used in this fragment
     */
    private void setupSwitches() {
        Switch startSwitch = (Switch) view.findViewById(R.id.startSwitch);
        Switch endSwitch = (Switch) view.findViewById(R.id.endSwitch);
        startSwitch.setChecked(startAlert);
        endSwitch.setChecked(endAlert);
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startAlert = isChecked;
                listener.updateStartAlert(isChecked);
            }
        });
        endSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                endAlert = isChecked;
                listener.updateEndAlert(isChecked);
            }
        });
    }

    /**
     * Display the date picker dialog to let the user select a new start date
     */
    private void changeStartDate() {
        Date date = getDateFromButton(startButton);

        // check if the start date picker has arguments
        if (startDatePicker.getArguments() == null) {
            // set up a bundle to transfer the date
            Bundle bundle = new Bundle();
            bundle.putLong("date", date.getTime());
            startDatePicker.setArguments(bundle);

            getFragmentManager().beginTransaction().add(startDatePicker, MainActivity.TAG_DATE_PICKER).addToBackStack(null).commit();

        } else {
            startDatePicker.show(getFragmentManager(), MainActivity.TAG_DATE_PICKER);
        }
    }

    /**
     * Set the new start date
     *
     * @param date the date to change to
     */
    private void setStartDate(Date date) {
        if (date.compareTo(endDate) > 0) {
            Snackbar.make(view, getString(R.string.start_after_end), Snackbar.LENGTH_LONG).show();
        } else {
            // update the start date
            startDate = date;
            startButton.setText(MainActivity.FORMATTER.format(date));
        }
        listener.updateStartDate(date);
    }

    /**
     * Display the date picker dialog to let the user select a new end date
     */
    private void changeEndDate() {
        Date date = getDateFromButton(endButton);

        // check if the end date picker has arguments
        if (endDatePicker.getArguments() == null) {
            // set up a bundle to transfer the date
            Bundle bundle = new Bundle();
            bundle.putLong("date", date.getTime());
            endDatePicker.setArguments(bundle);

            getFragmentManager().beginTransaction().add(endDatePicker, MainActivity.TAG_DATE_PICKER).addToBackStack(null).commit();
        } else {
            endDatePicker.show(getFragmentManager(), MainActivity.TAG_DATE_PICKER);
        }
    }

    /**
     * Set the new end date
     *
     * @param date the date to change to
     */
    private void setEndDate(Date date) {
        if (date.compareTo(startDate) < 0) {
            Snackbar.make(view, getString(R.string.end_before_start), Snackbar.LENGTH_LONG).show();
        } else {
            // update the end date
            endDate = date;
            endButton.setText(MainActivity.FORMATTER.format(date));
        }
        listener.updateEndDate(date);
    }

    /**
     * Get the date currently displayed on a button
     *
     * @param button the button to get the date from
     * @return the date displayed on the button
     */
    private Date getDateFromButton(Button button) {
        Date date = null;
        try {
            date = MainActivity.FORMATTER.parse(button.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCourseInfoListener) {
            listener = (OnCourseInfoListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMentorInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private OnCourseInfoListener listener;

    public interface OnCourseInfoListener {
        void updateCourseTitle(String title);

        void updateCourseDescription(String description);

        void updateStartDate(Date date);

        void updateEndDate(Date date);

        void updateStatus(Course.StatusType status);

        void updateStartAlert(boolean value);

        void updateEndAlert(boolean value);
    }
}
