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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.activities.MainActivity;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.dialogs.DatePickerFragment;
import com.example.echols.studentschedule.models.Term;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the {@link OnTermInfoListener} interface to handle interaction events.
 * Use the {@link TermInfoFragment#newInstance} factory method to create an instance of this fragment.
 */
public class TermInfoFragment extends Fragment {

    private long termId;
    private String termTitle;
    private String termDescription;
    private Date startDate;
    private Date endDate;
    private boolean startAlert;
    private boolean endAlert;

    private Button startButton;
    private Button endButton;

    private DatePickerFragment startDatePicker;
    private DatePickerFragment endDatePicker;

    private View view;

    public TermInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param termId the id of this term
     * @return A new instance of fragment TermInfoFragment.
     */
    public static TermInfoFragment newInstance(long termId) {
        TermInfoFragment fragment = new TermInfoFragment();
        Bundle args = new Bundle();
        args.putLong(MainActivity.ARG_PARENT_ID, termId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            termId = getArguments().getLong(MainActivity.ARG_PARENT_ID);
        }

        // get the context of this fragment
        Context context = getActivity();

        // get database helper
        ScheduleDbHelper dbHelper = ScheduleDbHelper.getInstance(context);

        // get the term from the database
        setTermData(dbHelper.getTermById(termId));

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
        view = inflater.inflate(R.layout.fragment_term_info, container, false);

        // get the views on the fragment
        EditText titleEditText = (EditText)view.findViewById(R.id.editTextTermTitle);
        EditText descriptionEditText = (EditText)view.findViewById(R.id.editTextTermDescription);

        // add listeners for text boxes
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
                termTitle = title;
                listener.updateTermTitle(title);
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
                termDescription = description;
                listener.updateTermDescription(description);
            }
        });

        // initialize the text in the views if this is an existing term
        titleEditText.setText(termTitle);
        descriptionEditText.setText(termDescription);

        setupButtons();

        setupSwitches();

        return view;
    }

    /**
     * Set the term info in the fragment
     * @param term the term displayed in this fragment
     */
    private void setTermData(Term term) {
        if (term == null) {
            Calendar c = Calendar.getInstance();
            startDate = c.getTime();
            term = new Term(getString(R.string.term_new_title), startDate);
        } else {
            startDate = term.getStartDate();
        }

        termTitle = term.getTitle();
        termDescription = term.getDescription();
        endDate = term.getEndDate();

        startAlert = term.isStartAlert();
        endAlert = term.isEndAlert();
    }

    /**
     * Setup the buttons used in this fragment
     */
    private void setupButtons() {
        startButton = (Button)view.findViewById(R.id.buttonTermStart);
        endButton = (Button)view.findViewById(R.id.buttonTermEnd);
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
        Switch startSwitch = (Switch)view.findViewById(R.id.startSwitch);
        Switch endSwitch = (Switch)view.findViewById(R.id.endSwitch);
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
     * @param date the date to change to
     */
    private void setStartDate(Date date) {
        if (date.compareTo(endDate) > 0) {
            Snackbar.make(view, getString(R.string.start_after_end), Snackbar.LENGTH_LONG).show();
        } else {
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
     * @param date the date to change to
     */
    private void setEndDate(Date date) {
        if (date.compareTo(startDate) < 0) {
            Snackbar.make(view, getString(R.string.end_before_start), Snackbar.LENGTH_LONG).show();
        } else {
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
        if (context instanceof OnTermInfoListener) {
            listener = (OnTermInfoListener)context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTermInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private OnTermInfoListener listener;

    public interface OnTermInfoListener {
        void updateTermTitle(String title);

        void updateTermDescription(String description);

        void updateStartDate(Date date);

        void updateEndDate(Date date);

        void updateStartAlert(boolean value);

        void updateEndAlert(boolean value);
    }
}
