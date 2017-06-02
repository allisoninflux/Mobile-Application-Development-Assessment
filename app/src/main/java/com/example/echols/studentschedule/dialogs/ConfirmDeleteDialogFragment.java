package com.example.echols.studentschedule.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A simple yes or no dialog that asks the user to confirm delete
 */
public class ConfirmDeleteDialogFragment extends DialogFragment {

    private final static String ARG_TITLE = "title";
    private String title;

    /**
     * Create a new instance of the delete confirmation dialog
     *
     * @param title the title of the dialog
     * @return A new instance of fragment ConfirmDeleteDialogFragment
     */
    public static ConfirmDeleteDialogFragment newInstance(String title) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the type of item this list contains
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
        }

        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        listener.onConfirmDelete();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        listener.onCancel();
                    }
                })
                .create();
    }

    private ConfirmDeleteListener listener;
    public interface ConfirmDeleteListener {
        void onConfirmDelete();
        void onCancel();
    }
    public void setConfirmDeleteListener(ConfirmDeleteListener listener) {
        this.listener = listener;
    }

}
