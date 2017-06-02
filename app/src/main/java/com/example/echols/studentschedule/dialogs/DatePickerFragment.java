package com.example.echols.studentschedule.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * A dialog to select a date
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Date date;
    private final Calendar c = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        date = new Date(bundle.getLong("date"));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        c.set(year, month, day);
        date = c.getTime();
        listener.setDateFromPicker(date);
    }

    private DatePickerListener listener;
    public interface DatePickerListener {
        void setDateFromPicker(Date date);
    }
    public void onSetDatePickerListener(DatePickerListener listener) {
        this.listener = listener;
    }

}
