package com.example.echols.studentschedule.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.adapters.AssessmentDetailsPagerAdapter;
import com.example.echols.studentschedule.adapters.NotesListAdapter;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.dialogs.ConfirmDeleteDialogFragment;
import com.example.echols.studentschedule.dialogs.TextNoteDialogFragment;
import com.example.echols.studentschedule.fragments.AssessmentInfoFragment;
import com.example.echols.studentschedule.fragments.NotesListFragment;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Note;
import com.example.echols.studentschedule.utils.ActionModeCallback;
import com.example.echols.studentschedule.utils.NotesUtility;
import com.example.echols.studentschedule.utils.PermissionUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The activity to display when a user is viewing, adding, or editing an assessment
 */
public class AssessmentDetailsActivity extends Activity implements
        AssessmentInfoFragment.OnAssessmentInfoListener, NotesListFragment.OnNotesListListener,
        ItemDetailActivity {

    private ScheduleDbHelper dbHelper; // the database helper object

    private String intent_action; // the result of the user closing this activity (save or delete/close)

    private ActionMode actionMode; // the action mode
    private ActionModeCallback actionModeCallback; // the action mode callback

    private FloatingActionButton fab; // the floating action button displayed for the course list

    private String source;// the activity that started this activity

    private boolean isNewAssessment; // true if this is a new assessment, false if not
    private Assessment assessment; // the assessment being displayed
    private Course course; // the course that this course is in

    private List<Integer> selectedIndexes; // a list of indexes of the notes that are selected

    private PermissionUtility permissionUtility; // the permission utility object to assist with getting permissions
    private NotesUtility notesUtility; // the notes utility object to assist with note methods and manipulation
    private List<Note> notesToDelete; // a list of notes to be deleted if the user confirms
    private Uri addedImageUri; // the uri to an image file that is added to the notes list

    private MenuItem shareMenuItem; // the 'share' menu item - only displayed on the notes tab

    private NotesListFragment notesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_details);

        // change status bar color to primary dark color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        // get database helper
        dbHelper = ScheduleDbHelper.getInstance(this);

        // get the toolbar and set title and icons
        setupToolbar();

        // get the which activity started this activity
        Intent intent = getIntent();
        if (getCallingActivity() != null) {
            source = getCallingActivity().getClassName();
        }

        // get the course from the intent
        long courseId = intent.getLongExtra(getString(R.string.intent_parent_id), 0);
        course = dbHelper.getCourseById(courseId);

        // get the assessment from the intent
        long id = intent.getLongExtra(getString(R.string.intent_child_id), 0);
        assessment = dbHelper.getAssessmentById(id);
        if (assessment == null) {
            isNewAssessment = true;
            assessment = new Assessment(getString(R.string.assessment_new_title), null, null, course);
        }

        // get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager)findViewById(R.id.view_pager);
        AssessmentDetailsPagerAdapter adapter = new AssessmentDetailsPagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(adapter);
        notesListFragment = (NotesListFragment)adapter.getItem(1);

        // get the TabLayout and set it up with the ViewPager
        final TabLayout tabLayout = (TabLayout)findViewById(R.id.assessment_detail_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0 && actionMode != null) {
                    actionMode.finish();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // get the floating action button
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNoteClicked(null);
            }
        });
        fab.hide();

        // initialize an action mode callback for this activity
        actionModeCallback = new ActionModeCallback();
        actionModeCallback.setOnActionModeListener(new ActionModeCallback.ActionModeListener() {
            @Override
            public void onActionClick() {
                deleteNotes();
            }
        });

        // add a page change listener so the fab can be hidden/shown per tab
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        fab.show();
                        shareMenuItem.setVisible(true);
                        break;
                    default:
                        fab.hide();
                        shareMenuItem.setVisible(false);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // initialize a permission utility object for this activity
        permissionUtility = new PermissionUtility(this, new PermissionUtility.OnPermissionGrantedListener() {
            @Override
            public void onPermissionGranted() {
                notesUtility.getNoteTypeToAdd();
            }
        });

        // initialize a notes utility object for this activity
        notesUtility = new NotesUtility(this, new NotesUtility.OnAddNoteListener() {
            @Override
            public void addTextNote() {
                getTextFromUser();
            }

            @Override
            public void addImageNote() {
                getImageFromUser();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        // show/hide the delete and close icons based on if this is a new file
        MenuItem hiddenIcon;
        if (isNewAssessment) {
            hiddenIcon = menu.findItem(R.id.action_delete);
        } else {
            hiddenIcon = menu.findItem(R.id.action_close);
        }
        hiddenIcon.setVisible(false);

        // get the share icon and hide it
        shareMenuItem = menu.findItem(R.id.action_share);
        shareMenuItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_close:
                super.onBackPressed();
                getWindow().setStatusBarColor(Color.TRANSPARENT);
                return true;
            case R.id.action_share:
                Intent shareIntent = notesUtility.getShareNotesIntent(notesListFragment.getNotes());
                startActivity(Intent.createChooser(shareIntent, "Share notes to.."));
                return true;
            case R.id.action_save:
                saveAssessment();
                return true;
            case R.id.action_delete:
                ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance("Delete " + assessment.getTitle() + "?");
                yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
                    @Override
                    public void onConfirmDelete() {
                        deleteAssessment();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                yesNoDialog.show(getFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.title_assessment_details);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationContentDescription(R.string.navigation_drawer_open);

        toolbar.inflateMenu(R.menu.menu_details);

        setActionBar(toolbar);
    }

    @Override
    public void onNoteClicked(Note note) {

        if (note == null) {
            permissionUtility.getWriteExternalStoragePermission();
        } else {
            switch (note.getType()) {
                case Note.IMAGE:
                    File imageFile = new File(note.getUri().getPath());
                    startActivity(notesUtility.getShowImageIntent(imageFile));
                    break;
                case Note.TEXT:
                    String text = "";
                    try (BufferedReader reader = new BufferedReader(new FileReader(note.getUri().getPath()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            text += "\n" + line;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getTextFromUser(text.substring(1), note);
                    break;
            }
        }
    }

    /**
     * Get the id for the assessment being displayed
     *
     * @return the id of the assessment being displayed, zero if new assessment
     */
    public long getAssessmentId() {
        if (assessment != null) {
            return assessment.getId();
        } else {
            return 0;
        }
    }

    /**
     * Get the id for the course that this assessment is in
     *
     * @return the id of the course that this assessment is in
     */
    public long getCourseId() {
        return course.getId();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case NotesUtility.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    notesUtility.getNoteTypeToAdd();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Snackbar.make(findViewById(R.id.view_pager), getString(R.string.external_storage_permission), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(android.R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    }
                                }).show();
                    }
                }
                break;
        }
    }

    /**
     * Start the image chooser to get an image from the user
     */
    private void getImageFromUser() {
        NotesUtility.UriAndIntent uriAndIntent = notesUtility.getImageChooserIntent(assessment);
        addedImageUri = uriAndIntent.getUri();
        Intent intent = uriAndIntent.getIntent();
        startActivityForResult(intent, MainActivity.RequestType.IMAGE_CAPTURE.getValue());
    }

    /**
     * Create a new text note via the text note dialog fragment
     */
    private void getTextFromUser() {
        getTextFromUser("", null);
    }

    /**
     * Edit an existing text note
     *
     * @param existingText the existing text in the note
     * @param note         the note to edit
     */
    private void getTextFromUser(final CharSequence existingText, final Note note) {
        TextNoteDialogFragment enterTextFragment = TextNoteDialogFragment.newInstance(existingText);
        enterTextFragment.setOnSaveTextListener(new TextNoteDialogFragment.OnSaveTextListener() {
            @Override
            public void onSaveText(CharSequence text) {
                if (note == null) {
                    File newTextFile = notesUtility.createTextFile(assessment, text.toString());
                    notesListFragment.addNote(new Note(Uri.fromFile(newTextFile)));
                } else {
                    try (BufferedWriter out = new BufferedWriter(new PrintWriter(note.getUri().getPath()))) {
                        out.write(text.toString());
                    } catch (IOException e) {
                        Log.e("WriteFileError", e.getMessage());
                    }
                    notesListFragment.updateNote(note);
                }
            }
        });
        enterTextFragment.show(getFragmentManager(), "enter_text_for_note");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainActivity.RequestType request = MainActivity.RequestType.getEnum(requestCode);

        if (request == MainActivity.RequestType.IMAGE_CAPTURE && resultCode == RESULT_OK) {
            boolean isCamera;
            Uri resultUri;

            if (data != null) {
                resultUri = data.getData();
                isCamera = (resultUri == null) || MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
            } else {
                isCamera = true;
            }

            if (!isCamera) {
                notesUtility.copyFile(data.getData(), addedImageUri);
            }

            notesListFragment.addNote(new Note(addedImageUri));
        }
    }

    @Override
    public void updateAssessmentTitle(String title) {
        assessment.setTitle(title);
    }

    @Override
    public void updateDate(Date date) {
        assessment.setDate(date);
    }

    @Override
    public void updateAlert(boolean value) {
        assessment.setAlert(value);
    }

    @Override
    public void updateType(Assessment.Type type) {
        assessment.setType(type);
    }

    /**
     * Save this assessment and finish the activity
     */
    private void saveAssessment() {
        if (isNewAssessment) {
            long id = dbHelper.addItem(assessment);
            assessment.setId(id);
        } else {
            dbHelper.updateItem(assessment);
        }
        intent_action = getString(R.string.intent_action_save);
        finish();
    }

    /**
     * Delete this assessment and finish the activity
     */
    private void deleteAssessment() {
        intent_action = getString(R.string.intent_action_delete);
        finish();
    }

    /**
     * Remove any notes from the list after the user confirms
     */
    private void deleteNotes() {
        String question = "Delete ";
        List<Note> notes = notesListFragment.getNotes();
        selectedIndexes = notesListFragment.getAdapter().getSelectedIndexes();
        int num = selectedIndexes.size();
        if (num == 1) {
            question += notes.get(selectedIndexes.get(0)).getFileName() + "?";
        } else {
            question += num + " notes?";
        }

        ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance(question);

        yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                // get the items in the fragment list
                List<Note> notes = notesListFragment.getNotes();

                // initialize a list of items to delete
                notesToDelete = new ArrayList<>();

                // get the list adapter
                NotesListAdapter adapter = notesListFragment.getAdapter();

                // for each selection, add it to the list of items to remove and remove it from the fragment list
                for (int i = selectedIndexes.size() - 1; i >= 0; i--) {
                    int index = selectedIndexes.get(i);
                    notesToDelete.add(notes.get(index));
                    notes.remove(index);
                    adapter.notifyItemRemoved(index);
                    deleteNoteFiles();
                }
            }

            @Override
            public void onCancel() {
                // get the list adapter
                NotesListAdapter adapter = notesListFragment.getAdapter();

                for (int i = 0; i < selectedIndexes.size(); i++) {
                    adapter.toggleSelection(selectedIndexes.get(i));
                }
                actionMode = startActionMode(actionModeCallback);
            }
        });

        yesNoDialog.show(getFragmentManager(), null);
    }

    /**
     * Delete the note files from the device
     */
    private void deleteNoteFiles() {
        for (Note note : notesToDelete) {
            File file = new File(note.getUri().getPath());
            if (file.exists() && file.delete()) {
                Log.i("ExternalFileDeleted", note.toString() + " was deleted successfully");
            }
        }
    }

    @Override
    public void finish() {
        // if the intent action is not set to nothing or is not null
        if (intent_action != null && !intent_action.equals(getString(R.string.intent_action_nothing))) {
            if (source.equals(MainActivity.class.getName())) {

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(getString(R.string.intent_parent_id), assessment.getId());
                intent.putExtra(getString(R.string.intent_action), intent_action);
                setResult(MainActivity.RESULT_OK, intent);

            } else if (source.equals(CourseDetailsActivity.class.getName())) {

                Intent intent = new Intent(this, CourseDetailsActivity.class);
                intent.putExtra(getString(R.string.intent_child_id), assessment.getId());
                intent.putExtra(getString(R.string.intent_action), intent_action);
                setResult(CourseDetailsActivity.RESULT_OK, intent);
            }
        }
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MainActivity.ACTION_MODE_FLAG, actionMode != null);

        NotesListAdapter notesListAdapter = notesListFragment.getAdapter();
        List<Integer> selectedList = notesListAdapter.getSelectedIndexes();
        int[] selectedArray = new int[selectedList.size()];
        for (int i = 0; i < selectedList.size(); i++) {
            selectedArray[i] = selectedList.get(i);
        }
        outState.putIntArray(MainActivity.SELECTED_INDEXES, selectedArray);

        if (notesListFragment != null && notesListFragment.isAdded()) {
            getFragmentManager().putFragment(outState, "notesListFragment", notesListFragment);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        notesListFragment = (NotesListFragment)getFragmentManager().getFragment(savedInstanceState, "notesListFragment");

        if (savedInstanceState != null && savedInstanceState.getBoolean(MainActivity.ACTION_MODE_FLAG, false)) {
            actionMode = startActionMode(actionModeCallback);
            int[] selectedIndexes = savedInstanceState.getIntArray(MainActivity.SELECTED_INDEXES);
            NotesListAdapter notesListAdapter = notesListFragment.getAdapter();
            for (int i = 0; selectedIndexes != null && i < selectedIndexes.length; i++) {
                notesListAdapter.toggleSelection(selectedIndexes[i]);
            }
        }
    }

    @Override
    public void startActionMode() {
        actionMode = startActionMode(actionModeCallback);
    }

    @Override
    public ActionMode getActionMode() {
        return actionMode;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        fab.hide();
        shareMenuItem.setVisible(false);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        actionMode = null;
        fab.show();
        shareMenuItem.setVisible(true);
        if(notesListFragment != null) {
            notesListFragment.getAdapter().clearSelections();
        }
    }
}
