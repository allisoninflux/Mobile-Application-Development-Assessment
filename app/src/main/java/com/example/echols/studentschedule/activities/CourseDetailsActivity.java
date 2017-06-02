package com.example.echols.studentschedule.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
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
import com.example.echols.studentschedule.adapters.CourseDetailsPagerAdapter;
import com.example.echols.studentschedule.adapters.ItemListAdapter;
import com.example.echols.studentschedule.adapters.NotesListAdapter;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.dialogs.ConfirmDeleteDialogFragment;
import com.example.echols.studentschedule.dialogs.TextNoteDialogFragment;
import com.example.echols.studentschedule.fragments.CourseInfoFragment;
import com.example.echols.studentschedule.fragments.ItemListFragment;
import com.example.echols.studentschedule.fragments.MentorInfoFragment;
import com.example.echols.studentschedule.fragments.NotesListFragment;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.Mentor;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Note;
import com.example.echols.studentschedule.models.Term;
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
 * The activity to display when a user is viewing, adding, or editing a course
 */
public class CourseDetailsActivity extends Activity implements
        CourseInfoFragment.OnCourseInfoListener, ItemListFragment.OnItemListListener,
        MentorInfoFragment.OnMentorInfoListener, NotesListFragment.OnNotesListListener,
        ItemDetailActivity {

    private ScheduleDbHelper dbHelper; // the database helper object

    private String intent_action; // the result of the user closing this activity (save or delete/close)

    private ActionMode actionMode; // the action mode
    private ActionModeCallback actionModeCallback; // the action mode callback

    private CourseDetailsPagerAdapter adapter; // the adapter for the view pager
    private ViewPager viewPager; // the view pager
    private FloatingActionButton fab; // the floating action button displayed for the course list

    private String source;// the activity that started this activity

    private boolean isNewCourse; // true if this is a new course, false if not
    private Course course; // the course being displayed
    private Term term; // the term that this course is in

    private int position; // the position of a assessment that was selected
    private List<Integer> selectedIndexes; // a list of indexes of the assessments or notes that are selected

    private boolean isItemListShown; // true if the item list is currently being displayed in the ViewPager
    private boolean isNoteListShown; // true if the notes list is currently being displayed in the ViewPager

    private PermissionUtility permissionUtility; // the permission utility object to assist with getting permissions
    private NotesUtility notesUtility; // the notes utility object to assist with note methods and manipulation
    private Uri addedImageUri; // the uri to an image file that is added to the notes list

    private MenuItem shareMenuItem; // the 'share' menu item - only displayed on the notes tab

    private ItemListFragment itemListFragment;
    private NotesListFragment notesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

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

        // get the term from the intent
        long termId = intent.getLongExtra(getString(R.string.intent_parent_id), 0);
        term = dbHelper.getTermById(termId);

        // get the course from the intent
        long id = intent.getLongExtra(getString(R.string.intent_child_id), 0);
        course = dbHelper.getCourseById(id);
        if (course == null) {
            isNewCourse = true;
            course = new Course(getString(R.string.course_new_title), getString(R.string.course_new_description), null, term);
        }

        // get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        adapter = new CourseDetailsPagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(adapter);
        itemListFragment = (ItemListFragment)adapter.getItem(1);
        notesListFragment = (NotesListFragment)adapter.getItem(2);

        // get the TabLayout and set it up with the ViewPager
        final TabLayout tabLayout = (TabLayout)findViewById(R.id.course_detail_tabs);
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
                if (isItemListShown) {
                    onItemClicked(null);
                } else if (isNoteListShown) {
                    onNoteClicked(null);
                }
            }
        });
        fab.hide();

        // initialize an action mode callback for this activity
        actionModeCallback = new ActionModeCallback();
        actionModeCallback.setOnActionModeListener(new ActionModeCallback.ActionModeListener() {
            @Override
            public void onActionClick() {
                if (isItemListShown) {
                    deleteAssessments();
                } else if (isNoteListShown) {
                    deleteNotes();
                }
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
                        isItemListShown = true;
                        isNoteListShown = false;
                        if(shareMenuItem != null) {
                            shareMenuItem.setVisible(false);
                        }
                        break;
                    case 2:
                        fab.show();
                        isItemListShown = false;
                        isNoteListShown = true;
                        if(shareMenuItem != null) {
                            shareMenuItem.setVisible(true);
                        }
                        break;
                    default:
                        fab.hide();
                        if(shareMenuItem != null) {
                            shareMenuItem.setVisible(false);
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // initialize a permission utility for this activity
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
        if (isNewCourse) {
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
                saveCourse();
                return true;
            case R.id.action_delete:
                ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance("Delete " + course.getTitle() + "?");
                yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
                    @Override
                    public void onConfirmDelete() {
                        deleteCourse();
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

        toolbar.setTitle(R.string.title_course_details);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationContentDescription(R.string.navigation_drawer_open);

        toolbar.inflateMenu(R.menu.menu_details);

        setActionBar(toolbar);
    }

    @Override
    public void onItemClicked(NavigationItem item) {

        position = itemListFragment.getPosition(item);

        Intent intent = new Intent(this, AssessmentDetailsActivity.class);
        if (item == null) {
            intent.putExtra(getString(R.string.intent_parent_id), course.getId());
            startActivityForResult(intent, MainActivity.RequestType.ADD_ASSESSMENT.getValue());
        } else {
            intent.putExtra(getString(R.string.intent_child_id), item.getId());
            intent.putExtra(getString(R.string.intent_parent_id), course.getId());
            startActivityForResult(intent, MainActivity.RequestType.EDIT_ASSESSMENT.getValue());
        }
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
     * Get the id for the course being displayed
     *
     * @return the id of the course being displayed, zero if new course
     */
    public long getCourseId() {
        if (course != null) {
            return course.getId();
        } else {
            return 0;
        }
    }

    /**
     * Get the id for the term that this course is in
     *
     * @return the id of the term that this course is in
     */
    public long getTermId() {
        return term.getId();
    }

    @Override
    public void updateCourseTitle(String title) {
        course.setTitle(title);
    }

    @Override
    public void updateCourseDescription(String description) {
        course.setDescription(description);
    }

    @Override
    public void updateMentor(Mentor mentor) {
        course.setMentor(mentor);
    }

    @Override
    public void updateStartDate(Date date) {
        course.setStartDate(date);
    }

    @Override
    public void updateStartAlert(boolean value) {
        course.setStartAlert(value);
    }

    @Override
    public void updateEndDate(Date date) {
        course.setEndDate(date);
    }

    @Override
    public void updateEndAlert(boolean value) {
        course.setEndAlert(value);
    }

    @Override
    public void updateStatus(Course.StatusType status) {
        course.setStatus(status);
    }

    /**
     * Save this course and finish the activity
     */
    private void saveCourse() {
        Mentor mentor = course.getMentor();
        if (mentor != null && mentor.getId() == 0) {
            long id = dbHelper.addMentor(mentor);
            mentor.setId(id);
        } else if (mentor != null) {
            dbHelper.updateMentor(mentor);
        }
        if (isNewCourse) {
            long id = dbHelper.addItem(course);
            course.setId(id);
        } else {
            dbHelper.updateItem(course);
        }
        intent_action = getString(R.string.intent_action_save);
        finish();
    }

    /**
     * Delete this course and finish the activity
     */
    private void deleteCourse() {
        intent_action = getString(R.string.intent_action_delete);
        finish();
    }

    /**
     * Remove any assessments from the list after the user confirms
     * Note: the actual removal of assessments from the database occurs if this course is saved
     */
    private void deleteAssessments() {
        String question = "Delete ";
        List<NavigationItem> courses = itemListFragment.getItems();
        selectedIndexes = itemListFragment.getAdapter().getSelectedIndexes();
        int num = selectedIndexes.size();
        if (num == 1) {
            question += courses.get(selectedIndexes.get(0)).getTitle() + "?";
        } else {
            question += num + " assessments?";
        }

        ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance(question);

        yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                // get the items in the fragment list
                List<NavigationItem> items = itemListFragment.getItems();

                // initialize a list of items to delete
                List<NavigationItem> assessmentsToDelete = new ArrayList<>();

                // get the list adapter
                ItemListAdapter adapter = itemListFragment.getAdapter();

                // for each selection, add it to the list of items to remove and remove it from the fragment list
                for (int i = selectedIndexes.size() - 1; i >= 0; i--) {
                    int index = selectedIndexes.get(i);
                    assessmentsToDelete.add(items.get(index));
                    items.remove(index);
                    adapter.notifyItemRemoved(index);
                }
                dbHelper.deleteItems(assessmentsToDelete);

            }

            @Override
            public void onCancel() {
                // get the list adapter
                ItemListAdapter adapter = itemListFragment.getAdapter();

                for (int i = 0; i < selectedIndexes.size(); i++) {
                    adapter.toggleSelection(selectedIndexes.get(i));
                }
                actionMode = startActionMode(actionModeCallback);
            }
        });

        yesNoDialog.show(getFragmentManager(), null);
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
                List<Note> notesToDelete = new ArrayList<>();

                // get the list adapter
                NotesListAdapter adapter = notesListFragment.getAdapter();

                // for each selection, add it to the list of items to remove and remove it from the fragment list
                for (int i = selectedIndexes.size() - 1; i >= 0; i--) {
                    int index = selectedIndexes.get(i);
                    notesToDelete.add(notes.get(index));
                    notes.remove(index);
                    adapter.notifyItemRemoved(index);
                }
                deleteNoteFiles(notesToDelete);
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
    private void deleteNoteFiles(List<Note> notesToDelete) {
        for (Note note : notesToDelete) {
            File file = new File(note.getUri().getPath());
            if (file.exists() && file.delete()) {
                Log.i("ExternalFileDeleted", note.toString() + " was deleted successfully");
            }
        }
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
        NotesUtility.UriAndIntent uriAndIntent = notesUtility.getImageChooserIntent(course);
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
                    File newTextFile = notesUtility.createTextFile(course, text.toString());
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
    public void finish() {
        // if the intent action is not set to nothing or is not null
        if (intent_action != null && !intent_action.equals(getString(R.string.intent_action_nothing))) {
            if (source.equals(MainActivity.class.getName())) {

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(getString(R.string.intent_parent_id), course.getId());
                intent.putExtra(getString(R.string.intent_action), intent_action);
                setResult(MainActivity.RESULT_OK, intent);

            } else if (source.equals(TermDetailsActivity.class.getName())) {

                Intent intent = new Intent(this, TermDetailsActivity.class);
                intent.putExtra(getString(R.string.intent_child_id), course.getId());
                intent.putExtra(getString(R.string.intent_action), intent_action);
                setResult(TermDetailsActivity.RESULT_OK, intent);
            }
        }
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MainActivity.ACTION_MODE_FLAG, actionMode != null);

        Fragment fragment = adapter.getItem(viewPager.getCurrentItem());

        int[] selectedIndexes = null;
        if (fragment == itemListFragment) {
            ItemListAdapter itemListAdapter = itemListFragment.getAdapter();
            List<Integer> selectedList = itemListAdapter.getSelectedIndexes();
            int[] selectedArray = new int[selectedList.size()];
            for (int i = 0; i < selectedList.size(); i++) {
                selectedArray[i] = selectedList.get(i);
            }
            selectedIndexes = selectedArray;
        } else if (fragment == notesListFragment) {
            NotesListAdapter notesListAdapter = notesListFragment.getAdapter();
            List<Integer> selectedList = notesListAdapter.getSelectedIndexes();
            int[] selectedArray = new int[selectedList.size()];
            for (int i = 0; i < selectedList.size(); i++) {
                selectedArray[i] = selectedList.get(i);
            }
            selectedIndexes = selectedArray;
        }
        outState.putIntArray(MainActivity.SELECTED_INDEXES, selectedIndexes);

        if (itemListFragment != null && itemListFragment.isAdded()) {
            getFragmentManager().putFragment(outState, "itemListFragment", itemListFragment);
        }

        if (notesListFragment != null && notesListFragment.isAdded()) {
            getFragmentManager().putFragment(outState, "notesListFragment", notesListFragment);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        itemListFragment = (ItemListFragment)getFragmentManager().getFragment(savedInstanceState, "itemListFragment");
        notesListFragment = (NotesListFragment)getFragmentManager().getFragment(savedInstanceState, "notesListFragment");

        if (savedInstanceState != null && savedInstanceState.getBoolean(MainActivity.ACTION_MODE_FLAG, false)) {
            actionMode = startActionMode(actionModeCallback);
            int[] selectedIndexes = savedInstanceState.getIntArray(MainActivity.SELECTED_INDEXES);

            Fragment fragment = adapter.getItem(viewPager.getCurrentItem());

            if (fragment == itemListFragment) {
                ItemListAdapter itemListAdapter = itemListFragment.getAdapter();
                for (int i = 0; selectedIndexes != null && i < selectedIndexes.length; i++) {
                    itemListAdapter.toggleSelection(selectedIndexes[i]);
                }
            } else if (fragment == notesListFragment) {
                NotesListAdapter notesListAdapter = notesListFragment.getAdapter();
                for (int i = 0; selectedIndexes != null && i < selectedIndexes.length; i++) {
                    notesListAdapter.toggleSelection(selectedIndexes[i]);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        MainActivity.RequestType request = MainActivity.RequestType.getEnum(requestCode);

        // return if the result is not OK or it is not a recognized request
        if (request == null || resultCode != RESULT_OK) {
            return;
        }

        switch (request) {
            case ADD_ASSESSMENT:
                if (data != null && itemListFragment != null) {
                    long id = data.getLongExtra(getString(R.string.intent_child_id), 0);
                    itemListFragment.addItem(dbHelper.getAssessmentById(id));
                }
                break;
            case EDIT_ASSESSMENT:
                if (data != null && itemListFragment != null) {
                    String action = data.getStringExtra(getString(R.string.intent_action));
                    long id = data.getLongExtra(getString(R.string.intent_child_id), 0);
                    Assessment assessment = dbHelper.getAssessmentById(id);
                    if (action.equals(getString(R.string.intent_action_delete))) {
                        List<NavigationItem> assessments = new ArrayList<>();
                        assessments.add(assessment);
                        dbHelper.deleteItems(assessments);
                        itemListFragment.removeItem(position);
                    } else if (action.equals(getString(R.string.intent_action_save))) {
                        itemListFragment.updateItem(assessment);
                    }
                }
                break;
            case IMAGE_CAPTURE:
                if (notesListFragment != null) {
                    Uri resultUri = null;
                    boolean isCamera;
                    if (data != null) {
                        resultUri = data.getData();
                    } else {
                        //noinspection UnusedAssignment
                        isCamera = true;
                    }

                    isCamera = (resultUri == null) || MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());

                    if (!isCamera) {
                        notesUtility.copyFile(data.getData(), addedImageUri);
                    }

                    notesListFragment.addNote(new Note(addedImageUri));
                }
                break;
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
        if(itemListFragment != null) {
            itemListFragment.getAdapter().clearSelections();
        }
        if(notesListFragment != null) {
            notesListFragment.getAdapter().clearSelections();
        }
    }

}
