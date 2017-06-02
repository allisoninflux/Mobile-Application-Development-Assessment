package com.example.echols.studentschedule.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
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
import com.example.echols.studentschedule.adapters.ItemListAdapter;
import com.example.echols.studentschedule.db.ScheduleDbHelper;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link OnItemListListener} interface to handle interaction events.
 * Use the {@link ItemListFragment#newInstance} factory method to create an instance of this fragment.
 */
public class ItemListFragment extends Fragment {

    private Context context; // the context that contains this fragment
    private ScheduleDbHelper dbHelper; // the database helper object
    private ItemListAdapter adapter; // the item list adapter for the RecyclerView

    private long parentId; // the id of the parent for this item list
    private NavigationItem parent; // the parent object for this item list
    private List<NavigationItem> items; // the items displayed in this list
    private String title; // the title of the fragment

    public ItemListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param parentId the id for the parent
     * @return A new instance of fragment ItemListFragment.
     */
    public static ItemListFragment newInstance(long parentId) {
        ItemListFragment fragment = new ItemListFragment();
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
        dbHelper = ScheduleDbHelper.getInstance(context);

        // get the parent from the database
        if (context instanceof TermDetailsActivity) {
            parent = dbHelper.getTermById(parentId);
        } else if (context instanceof CourseDetailsActivity) {
            parent = dbHelper.getCourseById(parentId);
        } else if (context instanceof AssessmentDetailsActivity) {
            parent = dbHelper.getAssessmentById(parentId);
        }

        getItemsFromDb();

        // initialize the adapter for the ListView
        adapter = new ItemListAdapter(context, items);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        setUpRecyclerView(view);

        return view;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Add an item to the list
     *
     * @param item the item to add
     */
    public void addItem(NavigationItem item) {
        items.add(item);
        adapter.notifyItemInserted(items.size() - 1);
    }

    /**
     * Remove an item from the list
     *
     * @param position the position in the list of the item to remove
     */
    public void removeItem(int position) {
        items.remove(position);
        adapter.notifyItemRemoved(position);
    }

    /**
     * Update an item in this list
     *
     * @param item the item to be updated
     */
    public void updateItem(NavigationItem item) {
        int index = 0;
        for (NavigationItem c : items) {
            if (c.getId() == item.getId()) {
                index = items.indexOf(c);
                break;
            }
        }

        if (index >= 0) {
            items.set(index, item);
            adapter.notifyItemChanged(index);
        }
    }

    public int getPosition(NavigationItem item) {
        return items.indexOf(item);
    }

    public List<NavigationItem> getItems() {
        return items;
    }

    /**
     * Get the items from the database to populate this list
     */
    private void getItemsFromDb() {
        if (parent != null) {
            if (parent instanceof Term) {
                items = dbHelper.getCoursesInTerm(parentId);
            } else if (parent instanceof Course) {
                items = dbHelper.getAssessmentsInCourse(parentId);
            }
        } else if (parentId == R.id.terms) {
            title = getString(R.string.title_terms);
            items = dbHelper.getAllTerms();
        } else if (parentId == R.id.courses) {
            title = getString(R.string.title_courses);
            items = dbHelper.getAllCourses();
        } else if (parentId == R.id.assessments) {
            title = getString(R.string.title_assessments);
            items = dbHelper.getAllAssessments();
        } else {
            items = new ArrayList<>();
        }
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
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // link the adapter to the recyclerView
        recyclerView.setAdapter(adapter);

        // set up click listener
        adapter.setOnItemClickListener(new ItemListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                onItemClick(position);
            }
        });

        // set up long click listener
        adapter.setOnItemLongClickListener(new ItemListAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(int position) {
                onItemLongClick(position);
            }
        });

        // set default item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // add dividers
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        // add snap-to-item functionality
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Method that fires when an item is clicked
     *
     * @param position the position of the item clicked
     */
    private void onItemClick(int position) {
        ItemDetailActivity activity = (ItemDetailActivity) context;
        ActionMode actionMode = activity.getActionMode();

        if (actionMode == null) {
            listener.onItemClicked(items.get(position));
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
    private void onItemLongClick(int position) {
        ItemDetailActivity activity = (ItemDetailActivity) context;
        ActionMode actionMode = activity.getActionMode();

        if (actionMode == null) {
            activity.startActionMode();
            adapter.toggleSelection(position);
        }
    }

    public ItemListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemListListener) {
            listener = (OnItemListListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnNotesListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private OnItemListListener listener;

    public interface OnItemListListener {
        void onItemClicked(NavigationItem item);
    }
}