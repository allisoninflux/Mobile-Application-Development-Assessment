package com.example.echols.studentschedule.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.adapters.ItemListAdapter;
import com.example.echols.studentschedule.adapters.TermDetailsPagerAdapter;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.dialogs.ConfirmDeleteDialogFragment;
import com.example.echols.studentschedule.fragments.ItemListFragment;
import com.example.echols.studentschedule.fragments.TermInfoFragment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Term;
import com.example.echols.studentschedule.utils.ActionModeCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The activity to display when a user is viewing, adding, or editing a term
 */
public class TermDetailsActivity extends Activity implements
        TermInfoFragment.OnTermInfoListener, ItemListFragment.OnItemListListener,
        ItemDetailActivity {

    private ScheduleDbHelper dbHelper; // the database helper object

    private String intent_action; // the result of the user closing this activity (save or delete/close)

    private ActionMode actionMode; // the action mode
    private ActionModeCallback actionModeCallback; // the action mode callback

    private FloatingActionButton fab; // the floating action button displayed for the course list

    private String source;// the activity that started this activity

    private boolean isNewTerm; // true if this is a new term, false if not
    private Term term; // the term being displayed

    private int position; // the position of a course that was selected
    private List<Integer> selectedIndexes; // a list of indexes of the courses that are selected
    private List<NavigationItem> coursesToDelete; // a list of courses to be deleted if this term is saved

    private ItemListFragment itemListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_details);

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
        long id = intent.getLongExtra(getString(R.string.intent_parent_id), 0);
        term = dbHelper.getTermById(id);
        if (term == null) {
            isNewTerm = true;
            Calendar c = Calendar.getInstance();
            term = new Term(getString(R.string.term_new_title), c.getTime());
        }

        // get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager)findViewById(R.id.view_pager);
        TermDetailsPagerAdapter adapter = new TermDetailsPagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(adapter);
        itemListFragment = (ItemListFragment)adapter.getItem(1);

        // get the TabLayout and set it up with the ViewPager
        final TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
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
                onItemClicked(null);
            }
        });
        fab.hide();

        // initialize an action mode callback for this activity
        actionModeCallback = new ActionModeCallback();
        actionModeCallback.setOnActionModeListener(new ActionModeCallback.ActionModeListener() {
            @Override
            public void onActionClick() {
                deleteCourses();
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
                        break;
                    default:
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        // show/hide the delete and close icons based on if this is a new file
        MenuItem hiddenIcon;
        if (isNewTerm) {
            hiddenIcon = menu.findItem(R.id.action_delete);
        } else {
            hiddenIcon = menu.findItem(R.id.action_close);
        }
        hiddenIcon.setVisible(false);

        // get the share icon and hide it
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
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
            case R.id.action_save:
                saveTerm();
                return true;
            case R.id.action_delete:
                ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance("Delete " + term.getTitle() + "?");
                yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
                    @Override
                    public void onConfirmDelete() {
                        deleteTerm();
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

        toolbar.setTitle(R.string.title_term_details);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationContentDescription(R.string.navigation_drawer_open);

        toolbar.inflateMenu(R.menu.menu_details);

        setActionBar(toolbar);
    }

    @Override
    public void onItemClicked(NavigationItem item) {

        position = itemListFragment.getPosition(item);


        Intent intent = new Intent(this, CourseDetailsActivity.class);
        if (item == null) {
            intent.putExtra(getString(R.string.intent_parent_id), term.getId());
            startActivityForResult(intent, MainActivity.RequestType.ADD_COURSE.getValue());
        } else {
            intent.putExtra(getString(R.string.intent_child_id), item.getId());
            intent.putExtra(getString(R.string.intent_parent_id), term.getId());
            startActivityForResult(intent, MainActivity.RequestType.EDIT_COURSE.getValue());
        }
    }

    /**
     * Get the id for the term being displayed
     *
     * @return the id of the term being displayed, zero if new term
     */
    public long getTermId() {
        if (term != null) {
            return term.getId();
        } else {
            return 0;
        }
    }

    @Override
    public void updateTermTitle(String title) {
        term.setTitle(title);
    }

    @Override
    public void updateTermDescription(String description) {
        term.setDescription(description);
    }

    @Override
    public void updateStartDate(Date date) {
        term.setStartDate(date);
    }

    @Override
    public void updateStartAlert(boolean value) {
        term.setStartAlert(value);
    }

    @Override
    public void updateEndDate(Date date) {
        term.setEndDate(date);
    }

    @Override
    public void updateEndAlert(boolean value) {
        term.setEndAlert(value);
    }

    /**
     * Save this term and finish the activity
     */
    private void saveTerm() {
        if (isNewTerm) {
            long id = dbHelper.addItem(term);
            term.setId(id);
        } else {
            dbHelper.updateItem(term);
        }
        intent_action = getString(R.string.intent_action_save);
        finish();
    }

    /**
     * Delete this term and finish the activity
     */
    private void deleteTerm() {
        List<NavigationItem> terms = new ArrayList<>();
        terms.add(term);
        try {
            // remove the items from the data
            dbHelper.deleteItems(terms);

            intent_action = getString(R.string.intent_action_delete);
            finish();

        } catch (SQLiteConstraintException ex) { // catch exception if Term is not empty
            Snackbar.make(findViewById(R.id.view_pager), getString(R.string.term_prevent_delete), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(android.R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
        }
    }

    /**
     * Remove any courses from the list after the user confirms
     * Note: the actual removal of courses from the database occurs if this term is saved
     */
    private void deleteCourses() {
        String question = "Delete ";
        List<NavigationItem> courses = itemListFragment.getItems();
        selectedIndexes = itemListFragment.getAdapter().getSelectedIndexes();
        int num = selectedIndexes.size();
        if (num == 1) {
            question += courses.get(selectedIndexes.get(0)).getTitle() + "?";
        } else {
            question += num + " courses?";
        }

        ConfirmDeleteDialogFragment yesNoDialog = ConfirmDeleteDialogFragment.newInstance(question);

        yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialogFragment.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                // get the items in the fragment list
                List<NavigationItem> items = itemListFragment.getItems();

                // initialize a list of items to delete
                List<NavigationItem> coursesToDelete = new ArrayList<>();

                // get the list adapter
                ItemListAdapter adapter = itemListFragment.getAdapter();

                // for each selection, add it to the list of items to remove and remove it from the fragment list
                for (int i = selectedIndexes.size() - 1; i >= 0; i--) {
                    int index = selectedIndexes.get(i);
                    coursesToDelete.add(items.get(index));
                    items.remove(index);
                    adapter.notifyItemRemoved(index);
                }
                dbHelper.deleteItems(coursesToDelete);
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

    @Override
    public void finish() {
        // if the intent action is not set to nothing or is not null
        if (intent_action != null && !intent_action.equals(getString(R.string.intent_action_nothing))) {
            if (source.equals(MainActivity.class.getName())) {

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(getString(R.string.intent_parent_id), term.getId());
                intent.putExtra(getString(R.string.intent_action), intent_action);
                setResult(MainActivity.RESULT_OK, intent);
            }
        }
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MainActivity.ACTION_MODE_FLAG, actionMode != null);

        ItemListAdapter itemListAdapter = itemListFragment.getAdapter();

        List<Integer> selectedList = itemListAdapter.getSelectedIndexes();
        int[] selectedArray = new int[selectedList.size()];
        for (int i = 0; i < selectedList.size(); i++) {
            selectedArray[i] = selectedList.get(i);
        }

        outState.putIntArray(MainActivity.SELECTED_INDEXES, selectedArray);

        if (itemListFragment != null && itemListFragment.isAdded()) {
            getFragmentManager().putFragment(outState, "itemListFragment", itemListFragment);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        itemListFragment = (ItemListFragment)getFragmentManager().getFragment(savedInstanceState, "itemListFragment");

        if (savedInstanceState != null && savedInstanceState.getBoolean(MainActivity.ACTION_MODE_FLAG, false)) {
            actionMode = startActionMode(actionModeCallback);
            int[] selectedIndexes = savedInstanceState.getIntArray(MainActivity.SELECTED_INDEXES);
            ItemListAdapter itemListAdapter = itemListFragment.getAdapter();
            for (int i = 0; selectedIndexes != null && i < selectedIndexes.length; i++) {
                itemListAdapter.toggleSelection(selectedIndexes[i]);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainActivity.RequestType request = MainActivity.RequestType.getEnum(requestCode);

        // return if any required data is null or the result is not OK
        if (request == null || resultCode != RESULT_OK || data == null || itemListFragment == null) {
            return;
        }

        String action = data.getStringExtra(getString(R.string.intent_action));
        long id = data.getLongExtra(getString(R.string.intent_child_id), 0);

        switch (request) {
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
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        actionMode = null;
        fab.show();
        if(itemListFragment != null) {
            itemListFragment.getAdapter().clearSelections();
        }
    }

}
