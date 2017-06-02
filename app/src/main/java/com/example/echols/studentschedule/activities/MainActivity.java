package com.example.echols.studentschedule.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.ReminderService;
import com.example.echols.studentschedule.adapters.ItemListAdapter;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.dialogs.ConfirmDeleteDialogFragment;
import com.example.echols.studentschedule.fragments.HomeFragment;
import com.example.echols.studentschedule.fragments.ItemListFragment;
import com.example.echols.studentschedule.fragments.SettingsFragment;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Term;
import com.example.echols.studentschedule.utils.ActionModeCallback;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements ItemListFragment.OnItemListListener, ItemDetailActivity {

    // constants used for fragments
    public static final String ACTION_MODE_FLAG = "action_mode_flag";
    public static final String SELECTED_INDEXES = "selected_indexes";
    public static final String ARG_PARENT_ID = "parent_id";
    public static final String ARG_CHILD_ID = "child_id";
    public static final String TAG_DATE_PICKER = "date_picker";

    private static final int REMINDER_JOB = 42;
    public static boolean HAS_CAMERA;

    public static final DateFormat FORMATTER = DateFormat.getDateInstance(DateFormat.SHORT);

    private ScheduleDbHelper dbHelper; // the database helper object

    private ActionMode actionMode; // the action mode
    private ActionModeCallback actionModeCallback; // the action mode callback

    private int position; // the position of an item that was selected
    private List<Integer> selectedIndexes; // a list of indexes of the items that are selected
    private int listTypeId; // the type of list currently being displayed

    private FloatingActionButton fab; // the floating action button displayed for the course list

    private DrawerLayout drawerLayout; // the drawer layout for main navigation
    private NavigationView navigationView; // the navigation view used in the navigation drawer
    private Toolbar toolbar; // the toolbar for this activity

    // enumerator of all intent request options
    enum RequestType {
        ADD_TERM(0), EDIT_TERM(1), ADD_COURSE(2), EDIT_COURSE(3), ADD_ASSESSMENT(4), EDIT_ASSESSMENT(5), IMAGE_CAPTURE(6);

        final int value;

        RequestType(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

        @Nullable
        static RequestType getEnum(int value) {
            for (RequestType r : RequestType.values()) {
                if (r.getValue() == value) {
                    return r;
                }
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HAS_CAMERA = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        initDefaultsInClasses();

        // initialize the database helper
        dbHelper = ScheduleDbHelper.getInstance(this);

        // initialize the toolbar
        setupToolbar();

        // initialize the drawer layout
        drawerLayout = setUpDrawerLayout();

        // initialize the navigation view
        navigationView = setUpNavigationView();

        if (savedInstanceState == null) {
            // initialize home fragment
            HomeFragment fragment = HomeFragment.newInstance();
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        // get the floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClicked(null);
            }
        });
        fab.hide();

        // initialize an action mode callback for this activity
        actionModeCallback = new ActionModeCallback();
        actionModeCallback.setOnActionModeListener(new ActionModeCallback.ActionModeListener() {
            @Override
            public void onActionClick() {
                selectedIndexes = getListAdapter().getSelectedIndexes();
                deleteItems();
            }
        });

        // add a back stack listener to the fragment manager which will update the title in the action bar
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setTitleFromFragment();
            }
        });

        // set preferences to default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        createReminderJob();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof ItemListFragment &&
                ((ItemListFragment) fragment).getTitle().equals(getString(R.string.title_terms))) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    /**
     * set the static initial length fields from the preferences
     */
    private void initDefaultsInClasses() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String initial_term_length = getString(R.string.pref_default_term_length_initial_value);
        String initial_course_length = getString(R.string.pref_default_course_length_initial_value);

        Term.length = Integer.parseInt(sharedPref.getString(getString(R.string.pref_default_term_length_key), initial_term_length));
        Course.length = Integer.parseInt(sharedPref.getString(getString(R.string.pref_default_course_length_key), initial_course_length));
    }

    /**
     * start the reminder job service to watch for upcoming date
     */
    private void createReminderJob() {

        ComponentName reminderService = new ComponentName(this, ReminderService.class);
        JobInfo jobInfo = new JobInfo.Builder(REMINDER_JOB, reminderService)
                .setPeriodic(1000 * 60 * 60 * 24) // daily
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Reminder Job scheduled successfully!");
        }
    }

    @SuppressWarnings("unused")
    public void cancelReminderJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(REMINDER_JOB);
    }

    /**
     * Set the school and degree from user preferences in the navigation view header
     *
     * @param navigationView the navigation view containing the header
     */
    private void setSchoolAndDegree(NavigationView navigationView) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String schoolName = sharedPref.getString(getString(R.string.pref_school_name_key), getString(R.string.school_name));
        String degreeName = sharedPref.getString(getString(R.string.pref_degree_key), getString(R.string.degree));

        View header = navigationView.getHeaderView(0);
        TextView school = (TextView) header.findViewById(R.id.school);
        TextView degree = (TextView) header.findViewById(R.id.degree);
        school.setText(schoolName);
        degree.setText(degreeName);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_home);
        toolbar.setNavigationContentDescription(R.string.navigation_drawer_open);

        setActionBar(toolbar);
    }

    /**
     * Set up the navigation drawer layout
     *
     * @return the drawer layout
     */
    private DrawerLayout setUpDrawerLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        return drawerLayout;
    }

    /**
     * Set up the navigation view
     *
     * @return the navigation view object
     */
    private NavigationView setUpNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // toggle the checked state
                item.setChecked(!item.isChecked());

                // close the drawer on item click
                drawerLayout.closeDrawers();

                listTypeId = item.getItemId();
                return onDrawerItemSelected();
            }
        });
        navigationView.setItemIconTintList(null);
        return navigationView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setSchoolAndDegree(navigationView);
                drawerLayout.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClicked(NavigationItem item) {
        Intent intent;

        ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            position = fragment.getPosition(item);
        }

        if (item instanceof Term) {
            // set the intent to term details activity
            intent = new Intent(this, TermDetailsActivity.class);
            intent.putExtra(getString(R.string.intent_parent_id), item.getId());
            // start the activity to edit/delete an existing term
            startActivityForResult(intent, RequestType.EDIT_TERM.getValue());
        } else if (item instanceof Course) {
            // set the intent to term details activity
            intent = new Intent(this, CourseDetailsActivity.class);
            intent.putExtra(getString(R.string.intent_child_id), item.getId());
            intent.putExtra(getString(R.string.intent_parent_id), ((Course) item).getTerm().getId());
            // start the activity to edit/delete an existing course
            startActivityForResult(intent, RequestType.EDIT_COURSE.getValue());
        } else if (item instanceof Assessment) {
            intent = new Intent(this, AssessmentDetailsActivity.class);
            // start the activity to edit/delete an existing assessment
            intent.putExtra(getString(R.string.intent_child_id), item.getId());
            intent.putExtra(getString(R.string.intent_parent_id), ((Assessment) item).getCourse().getId());
            startActivityForResult(intent, RequestType.EDIT_ASSESSMENT.getValue());
        } else if (item == null) {
            switch (listTypeId) {
                case R.id.terms:
                    // start the activity to add a term
                    intent = new Intent(this, TermDetailsActivity.class);
                    startActivityForResult(intent, RequestType.ADD_TERM.getValue());
                    break;
                case R.id.courses:
                    // start the activity to add a course
                    intent = new Intent(this, CourseDetailsActivity.class);
                    startActivityForResult(intent, RequestType.ADD_COURSE.getValue());
                    break;
                case R.id.assessments:
                    // start the activity to add an assessment
                    intent = new Intent(this, AssessmentDetailsActivity.class);
                    startActivityForResult(intent, RequestType.ADD_ASSESSMENT.getValue());
                    break;
            }
        }
    }

    /**
     * Action to take when a drawer item is selected
     *
     * @return true
     */
    @SuppressWarnings("SameReturnValue")
    private boolean onDrawerItemSelected() {
        // this flag is set to true if the screen needs to go 'up' in navigation
        boolean goBack = false;

        Fragment fragment;

        switch (listTypeId) {
            case R.id.terms:
                fab.show();
                fragment = ItemListFragment.newInstance(listTypeId);
                goBack = true;
                break;
            case R.id.courses:
                fab.hide();
                fragment = ItemListFragment.newInstance(listTypeId);
                goBack = true;
                break;
            case R.id.assessments:
                fab.hide();
                fragment = ItemListFragment.newInstance(listTypeId);
                goBack = true;
                break;
            case R.id.settings:
                fragment = new SettingsFragment();
                goBack = true;
                fab.hide();
                break;
            default:
                fragment = HomeFragment.newInstance();
                fab.hide();
                break;
        }

        // update the main content by replacing fragments
        setFragment(fragment, goBack);

        return true;
    }

    /**
     * Set the fragment to be displayed in the container
     *
     * @param fragment       the fragment to display
     * @param addToBackStack true to add this fragment to the back stack
     */
    private void setFragment(Fragment fragment, boolean addToBackStack) {

        FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        ft.commit();

        setTitleFromFragment();
    }

    /**
     * Set the title of the action bar based on the fragment in the container
     */
    private void setTitleFromFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        // default title
        String title = getString(R.string.app_name);

        if (fragment instanceof ItemListFragment) {
            title = ((ItemListFragment) fragment).getTitle();
        } else if (fragment instanceof SettingsFragment) {
            title = getString(R.string.title_settings);
        } else if (fragment instanceof HomeFragment) {
            title = getString(R.string.app_name);
        }

        toolbar.setTitle(title);
    }

    /**
     * Get the items currently displayed
     *
     * @return a list of navigation items
     */
    private List<NavigationItem> getItems() {
        List<NavigationItem> items = new ArrayList<>();
        ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            items = fragment.getItems();
        }
        return items;
    }

    /**
     * Get a list of items that the use wants to delete and show delete confirmation dialog
     */
    private void deleteItems() {
        String type = "";
        switch (listTypeId) {
            case R.id.terms:
                type = getString(R.string.title_terms);
                break;
            case R.id.courses:
                type = getString(R.string.title_courses);
                break;
            case R.id.assessments:
                type = getString(R.string.title_assessments);
                break;
        }

        String question = "Delete ";
        List<NavigationItem> items = getItems();
        int num = selectedIndexes.size();
        if (num == 1) {
            question += items.get(selectedIndexes.get(0)).getTitle() + "?";
        } else {
            question += num + " " + type + "?";
        }

        ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance(question);

        yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                confirmDelete();
            }

            @Override
            public void onCancel() {
                restoreSelections(selectedIndexes);
            }
        });

        yesNoDialog.show(getFragmentManager(), null);

    }

    /**
     * Delete the items from the list and from the database
     */
    private void confirmDelete() {
        // get the items in the fragment list
        List<NavigationItem> items = getItems();

        // initialize a list of items to delete
        List<NavigationItem> itemsToDelete = new ArrayList<>();

        // for each selection, add it to the list of items to remove
        for (int i = 0; i < selectedIndexes.size(); i++) {
            itemsToDelete.add(items.get(selectedIndexes.get(i)));
        }

        try {
            // remove the items from the data
            dbHelper.deleteItems(itemsToDelete);

            // remove the items from the collection
            items.removeAll(itemsToDelete);

            // remove the items from the adapter
            removeItemsFromAdapter();

        } catch (SQLiteConstraintException ex) { // catch exception if Term is not empty
            Snackbar.make(findViewById(R.id.container), getString(R.string.term_prevent_delete), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(android.R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            restoreSelections(selectedIndexes);
                        }
                    }).show();
        }
    }

    /**
     * Remove the items from the item list adapter
     */
    private void removeItemsFromAdapter() {
        // get the list adapter
        ItemListAdapter adapter = getListAdapter();

        // sort the list in reverse order
        Collections.sort(selectedIndexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // continue to notify the adapter of removals until all indexes have been removed
        while (!selectedIndexes.isEmpty()) {

            // if there is only one item left, remove it
            if (selectedIndexes.size() == 1) {
                adapter.notifyItemRemoved(selectedIndexes.get(0));
                selectedIndexes.remove(0);
            } else {
                // count the number items that are in the next consecutive block
                int count = 1;
                while (selectedIndexes.size() > count && selectedIndexes.get(count).equals(selectedIndexes.get(count - 1) - 1)) {
                    ++count;
                }

                // notify the adapter that we have remove the next consecutive block
                if (count == 1) {
                    adapter.notifyItemRemoved(selectedIndexes.get(0));
                } else {
                    adapter.notifyItemRangeRemoved(selectedIndexes.get(count - 1), count);
                }

                // remove the next consecutive block
                for (int i = 0; i < count; ++i) {
                    selectedIndexes.remove(0);
                }
            }
        }
    }

    /**
     * Get the item list adapter for the fragment currently displayed
     *
     * @return the item list adapter for the displayed item list fragment
     */
    private ItemListAdapter getListAdapter() {
        ItemListAdapter adapter = null;
        ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            adapter = fragment.getAdapter();
        }
        return adapter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ACTION_MODE_FLAG, actionMode != null);

        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        int[] selectedIndexes = null;
        if (fragment != null && fragment instanceof ItemListFragment) {
            ItemListAdapter itemListAdapter = ((ItemListFragment) fragment).getAdapter();
            List<Integer> selectedList = itemListAdapter.getSelectedIndexes();
            int[] selectedArray = new int[selectedList.size()];
            for (int i = 0; i < selectedList.size(); i++) {
                selectedArray[i] = selectedList.get(i);
            }
            selectedIndexes = selectedArray;
        }
        outState.putIntArray(SELECTED_INDEXES, selectedIndexes);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE_FLAG, false)) {
            int[] selectedIndexes = savedInstanceState.getIntArray(SELECTED_INDEXES);
            if (selectedIndexes == null) {
                return;
            }
            List<Integer> positions = new ArrayList<>();
            for (int index : selectedIndexes) {
                positions.add(index);
            }
            restoreSelections(positions);
        }
    }

    /**
     * Restore the items that were previously selected
     *
     * @param positions a list of indexes of the items to be re-selected
     */
    private void restoreSelections(List<Integer> positions) {
        ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            ItemListAdapter itemListAdapter = fragment.getAdapter();
            for (int i = 0; positions != null && i < positions.size(); i++) {
                itemListAdapter.toggleSelection(positions.get(i));
            }
        }

        actionMode = startActionMode(actionModeCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RequestType request = RequestType.getEnum(requestCode);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        ItemListFragment itemListFragment = null;
        if (fragment instanceof ItemListFragment) {
            itemListFragment = (ItemListFragment) fragment;
        }
        // return if any required data is null or the result is not OK
        if (request == null || resultCode != RESULT_OK || data == null || itemListFragment == null) {
            return;
        }

        String action = data.getStringExtra(getString(R.string.intent_action));
        long id = data.getLongExtra(getString(R.string.intent_parent_id), 0);

        switch (request) {
            case ADD_TERM:
                itemListFragment.addItem(dbHelper.getTermById(id));
                break;
            case EDIT_TERM:
                if (action.equals(getString(R.string.intent_action_delete))) {
                    itemListFragment.removeItem(position);
                } else if (action.equals(getString(R.string.intent_action_save))) {
                    itemListFragment.updateItem(dbHelper.getTermById(id));
                }
                break;
            case ADD_COURSE:
                itemListFragment.addItem(dbHelper.getCourseById(id));
                break;
            case EDIT_COURSE:
                Course course = dbHelper.getCourseById(id);
                if (action.equals(getString(R.string.intent_action_delete))) {
                    List<NavigationItem> courses = new ArrayList<>();
                    courses.add(course);
                    dbHelper.deleteItems(courses);
                    itemListFragment.removeItem(position);
                } else if (action.equals(getString(R.string.intent_action_save))) {
                    itemListFragment.updateItem(course);
                }
                break;
            case ADD_ASSESSMENT:
                itemListFragment.addItem(dbHelper.getAssessmentById(id));
                break;
            case EDIT_ASSESSMENT:
                Assessment assessment = dbHelper.getAssessmentById(id);
                if (action.equals(getString(R.string.intent_action_delete))) {
                    List<NavigationItem> assessments = new ArrayList<>();
                    assessments.add(assessment);
                    dbHelper.deleteItems(assessments);
                    itemListFragment.removeItem(position);
                } else if (action.equals(getString(R.string.intent_action_save))) {
                    itemListFragment.updateItem(assessment);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
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
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        actionMode = null;
        if (listTypeId == R.id.terms) {
            fab.show();
        } else {
            fab.hide();
        }
        ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            fragment.getAdapter().clearSelections();
        }
    }
}