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
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link OnAssessmentInfoListener} interface to handle interaction events.
 * Use the {@link AssessmentInfoFragment#newInstance} factory method to create an instance of this fragment.
 */
public class AssessmentInfoFragment extends Fragment {

    private Context context;
    private ScheduleDbHelper dbHelper;
    private View view;

    private long assessmentId;
    private Course course;
    private long courseId;
    private String title;
    private Date date;
    private boolean alert;
    private Assessment.Type type;

    private DatePickerFragment datePicker;

    private Button dateButton;

    public AssessmentInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param assessmentId the assessment id for this assessment
     * @param courseId     the course id for the course that this assessment is in
     * @return A new instance of fragment CourseInfoFragment.
     */
    public static AssessmentInfoFragment newInstance(long assessmentId, long courseId) {
        AssessmentInfoFragment fragment = new AssessmentInfoFragment();
        Bundle args = new Bundle();
        args.putLong(MainActivity.ARG_CHILD_ID, assessmentId);
        args.putLong(MainActivity.ARG_PARENT_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            assessmentId = getArguments().getLong(MainActivity.ARG_CHILD_ID);
            courseId = getArguments().getLong(MainActivity.ARG_PARENT_ID);
        }

        // get the context of this fragment
        context = getActivity();

        // get database helper
        dbHelper = ScheduleDbHelper.getInstance(context);

        // get the assessment and set all of the assessment data
        setAssessmentData(dbHelper.getAssessmentById(assessmentId));

        datePicker = new DatePickerFragment();
        datePicker.onSetDatePickerListener(new DatePickerFragment.DatePickerListener() {
            @Override
            public void setDateFromPicker(Date date) {
                setDate(date);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_assessment_info, container, false);

        TextView courseTitle = (TextView) view.findViewById(R.id.textViewCourse);
        courseTitle.setText(course.getTitle());

        EditText titleEditText = (EditText) view.findViewById(R.id.editTextAssessmentTitle);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                title = s.toString();
                listener.updateAssessmentTitle(title);
            }
        });
        titleEditText.setText(title);

        setupSpinner();

        Switch startSwitch = (Switch) view.findViewById(R.id.alertSwitch);
        startSwitch.setChecked(alert);
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alert = isChecked;
                listener.updateAlert(isChecked);
            }
        });

        dateButton = (Button) view.findViewById(R.id.buttonAssessmentDate);
        dateButton.setText(MainActivity.FORMATTER.format(date));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDate();
            }
        });

        return view;
    }

    /**
     * Set the assessment info in the fragment
     *
     * @param assessment the assessment displayed in this fragment
     */
    private void setAssessmentData(Assessment assessment) {
        if (assessment == null) {
            course = dbHelper.getCourseById(courseId);

            Calendar c = Calendar.getInstance();
            date = c.getTime();
            assessment = new Assessment(getString(R.string.assessment_new_title), null, date, course);
        } else {
            course = assessment.getCourse();
            date = assessment.getDate();
        }
        title = assessment.getTitle();
        type = assessment.getType();
        alert = assessment.isAlert();
    }

    /**
     * Set up the spinner used to select the type of assessment
     */
    private void setupSpinner() {
        Spinner typeSpinner = (Spinner) view.findViewById(R.id.assessmentTypeSpinner);

        // setup spinner adapter and initial value
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.assessment_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setSelection(adapter.getPosition(type.toString()));

        // setup listeners
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String typeString = parent.getSelectedItem().toString();
                type = Assessment.Type.getType(typeString);
                listener.updateType(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Display the date picker dialog to let the user select a new due date
     */
    private void changeDate() {
        Date date = getDateFromButton(dateButton);

        // check if the start date picker has arguments
        if (datePicker.getArguments() == null) {
            // set up a bundle to transfer the date
            Bundle bundle = new Bundle();
            bundle.putLong("date", date.getTime());
            datePicker.setArguments(bundle);

            getFragmentManager().beginTransaction().add(datePicker, MainActivity.TAG_DATE_PICKER).addToBackStack(null).commit();

        } else {
            datePicker.show(getFragmentManager(), MainActivity.TAG_DATE_PICKER);
        }
    }

    /**
     * Set the new due date
     *
     * @param date the date to change to
     */
    private void setDate(Date date) {
        dateButton.setText(MainActivity.FORMATTER.format(date));
        this.date = date;
        listener.updateDate(date);
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
        if (context instanceof OnAssessmentInfoListener) {
            listener = (OnAssessmentInfoListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnAssessmentInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private OnAssessmentInfoListener listener;

    public interface OnAssessmentInfoListener {
        void updateAssessmentTitle(String title);

        void updateDate(Date date);

        void updateType(Assessment.Type type);

        void updateAlert(boolean value);
    }

}
