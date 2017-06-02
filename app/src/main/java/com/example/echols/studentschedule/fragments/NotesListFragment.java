package com.example.echols.studentschedule.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.activities.AssessmentDetailsActivity;
import com.example.echols.studentschedule.activities.CourseDetailsActivity;
import com.example.echols.studentschedule.activities.ItemDetailActivity;
import com.example.echols.studentschedule.activities.MainActivity;
import com.example.echols.studentschedule.activities.TermDetailsActivity;
import com.example.echols.studentschedule.adapters.NotesListAdapter;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Note;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link OnNotesListListener} interface to handle interaction events.
 * Use the {@link NotesListFragment#newInstance} factory method to create an instance of this fragment.
 */
public class NotesListFragment extends Fragment {

    private Context context; // the context that contains this fragment
    private NotesListAdapter adapter; // the notes list adapter for the RecyclerView

    private long parentId; // the id of the parent for this notes list
    private NavigationItem parent; // the parent object for this notes list

    private List<Note> notes; // the notes displayed in this list

    public NotesListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param parentId the id for the parent
     * @return A new instance of fragment ItemListFragment.
     */
    public static NotesListFragment newInstance(long parentId) {
        NotesListFragment fragment = new NotesListFragment();
        Bundle args = new Bundle();
        args.putLong(MainActivity.ARG_PARENT_ID, parentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parentId = getArguments().getLong(MainActivity.ARG_PARENT_ID);
        }

        // get the context of this fragment
        context = getActivity();

        // initialize the database helper
        ScheduleDbHelper dbHelper = ScheduleDbHelper.getInstance(context);

        // get the parent from the database
        if (context instanceof TermDetailsActivity) {
            parent = dbHelper.getTermById(parentId);
        } else if (context instanceof CourseDetailsActivity) {
            parent = dbHelper.getCourseById(parentId);
        } else if (context instanceof AssessmentDetailsActivity) {
            parent = dbHelper.getAssessmentById(parentId);
        }

        setNotes(parent);

        // initialize the adapter for the ListView
        adapter = new NotesListAdapter(context, notes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notes_list, container, false);

        setUpRecyclerView(view);

        return view;
    }

    /**
     * Set up the RecycleView
     *
     * @param view the parent view containing the RecyclerView
     */
    private void setUpRecyclerView(View view) {

        // initialize the RecyclerView
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        // initialize the layout manager for RecyclerView
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // link the adapter to the recyclerView
        recyclerView.setAdapter(adapter);

        // set up click listener
        adapter.setOnNoteClickListener(new NotesListAdapter.OnNoteClickListener() {
            @Override
            public void onClick(int position) {
                onNoteClick(position);
            }
        });

        // set up long click listener
        adapter.setOnNoteLongClickListener(new NotesListAdapter.OnNoteLongClickListener() {
            @Override
            public void onLongClick(int position) {
                onNoteLongClick(position);
            }
        });

        // set default item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // add dividers
//        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        // add snap-to-item functionality
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Populate the list of notes
     *
     * @param parent the parent item for this list of notes
     */
    private void setNotes(NavigationItem parent) {
        notes = new ArrayList<>();

        if (parent != null) {
            List<Uri> notesUri = getNoteUris();
            if (notesUri.size() > 0) {
                for (Uri uri : notesUri) {
                    String fileName = uri.getLastPathSegment();
                    String ext = fileName.substring(fileName.length() - 3);
                    if (ext.equals("txt")) {
                        notes.add(new Note(uri));
                    } else if (ext.equals("jpg")) {
                        notes.add(new Note(uri));
                    }
                }
            }
        }
    }

    /**
     * Get a list of note Uri objects from the device's external storage
     *
     * @return a list of Uri objects for the notes in this list
     */
    private List<Uri> getNoteUris() {
        String type = "unknown";
        if (parent instanceof Course) {
            type = getString(R.string.notes_course_directory);
        } else if (parent instanceof Assessment) {
            type = getString(R.string.notes_assessments_directory);
        }

        File mainFolder = Environment.getExternalStorageDirectory();
        File folder = new File(mainFolder, "Student Schedule/notes/" + type + "/" + parentId);
        List<Uri> uris = new ArrayList<>();

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    uris.add(Uri.fromFile(file));
                }
            }
        }

        return uris;
    }

    /**
     * Add a note to this list
     *
     * @param note the note to add
     */
    public void addNote(Note note) {
        notes.add(note);
        adapter.notifyItemInserted(notes.size() - 1);
    }

    /**
     * Update a note in this list
     *
     * @param note the note to be updated
     */
    public void updateNote(Note note) {
        int index = 0;
        for (Note n : notes) {
            if (n.getUri() == note.getUri()) {
                index = notes.indexOf(n);
                break;
            }
        }

        if (index >= 0) {
            notes.set(index, note);
            adapter.notifyItemChanged(index);
        }
    }

    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Method that fires when an item is clicked
     *
     * @param position the position of the item clicked
     */
    private void onNoteClick(int position) {
        ItemDetailActivity activity = (ItemDetailActivity) context;
        ActionMode actionMode = activity.getActionMode();

        if (actionMode == null) {
            listener.onNoteClicked(notes.get(position));
        } else {
            adapter.toggleSelection(position);
            if (adapter.getSelectedItemCount() == 0) {
                actionMode.finish();
            }
        }
    }

    /**
     * Method that fires when an item is long clicked
     *
     * @param position the position of the item clicked
     */
    private void onNoteLongClick(int position) {
        ItemDetailActivity activity = (ItemDetailActivity) context;
        ActionMode actionMode = activity.getActionMode();

        if (actionMode == null) {
            activity.startActionMode();
            adapter.toggleSelection(position);
        }
    }

    public NotesListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNotesListListener) {
            listener = (OnNotesListListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnNotesListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private OnNotesListListener listener;

    public interface OnNotesListListener {
        void onNoteClicked(Note note);
    }

}
