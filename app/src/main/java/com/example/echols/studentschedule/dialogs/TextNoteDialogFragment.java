package com.example.echols.studentschedule.dialogs;


import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.echols.studentschedule.R;

/**
 * A dialog fragment that allows the user to enter text to save into a note
 */
public class TextNoteDialogFragment extends DialogFragment {

    private final static String ARG_TEXT = "initial_text";
    private CharSequence initialText;

    private EditText editText;

    public TextNoteDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Create a new instance of the text note dialog fragment
     * @param initialText if the user selected an existing text note, set the text to show upon creation
     * @return A new instance of fragment TextNoteDialogFragment
     */
    public static TextNoteDialogFragment newInstance(CharSequence initialText) {
        TextNoteDialogFragment fragment = new TextNoteDialogFragment();
        Bundle args = new Bundle();
        args.putCharSequence(ARG_TEXT, initialText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialText = getArguments().getCharSequence(ARG_TEXT);
        }

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_note_dialog, container, false);

        editText = (EditText)view.findViewById(R.id.editTextNote);
        editText.setText(initialText);

        Button saveButton = (Button)view.findViewById(R.id.buttonSave);
        Button cancelButton = (Button)view.findViewById(R.id.buttonCancel);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSaveText(editText.getText());
                closeDialogFragment();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialogFragment();
            }
        });

        return view;
    }

    /**
     * This override will allow the fragment to display on most of the screen
     */
    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        super.onResume();
    }

    private void closeDialogFragment() {
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();
    }

    private OnSaveTextListener listener;

    public interface OnSaveTextListener {
        void onSaveText(CharSequence text);
    }

    public void setOnSaveTextListener(OnSaveTextListener listener) {
        this.listener = listener;
    }

}
