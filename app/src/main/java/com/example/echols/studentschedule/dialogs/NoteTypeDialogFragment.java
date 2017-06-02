package com.example.echols.studentschedule.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.echols.studentschedule.R;

/**
 * A simple dialog that asks the user which type of note to create
 */
public class NoteTypeDialogFragment extends DialogFragment {

    public NoteTypeDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Create a new instance of the delete confirmation dialog
     * @return A new instance of fragment NoteTypeDialogFragment
     */
    public static NoteTypeDialogFragment newInstance() {

        return new NoteTypeDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_note_type_dialog))
                .setItems(R.array.note_types, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(listener != null) {
                            listener.onSelectNoteType(which);
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private OnSelectNoteTypeListener listener;
    public interface OnSelectNoteTypeListener {
        void onSelectNoteType(int selection);
    }
    public void setOnSelectNoteTypeListener(OnSelectNoteTypeListener listener) {
        this.listener = listener;
    }

}
