package com.example.echols.studentschedule.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.models.NavigationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView adapter for a item list
 */
public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private final List<NavigationItem> items;
    private final Context context;

    /**
     * Create a new item list adapter.
     * The custom ViewHolder is included in this class.
     *
     * @param context the context this adapter is in
     * @param items   the list of items displayed in the adapter
     */
    public ItemListAdapter(Context context, List<NavigationItem> items) {
        this.context = context;
        this.items = items;
        selectedItems = new SparseBooleanArray();
    }

    /**
     * Inflate the layout and return the custom ViewHolder
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the RecyclerView
     * @return the ViewHolder for the RecyclerView
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the layout of the navigation item view
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.navigation_item, parent, false);

        return new ViewHolder(itemView);
    }

    /**
     * Populate the data from the TextViewHolder
     *
     * @param viewHolder the TextViewHolder that contains the data
     * @param position   the position of the item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // get the item at the position specified
        NavigationItem item = items.get(position);

        // populate the data of the item view
        TextView title = viewHolder.title;
        title.setText(item.getTitle());
        TextView description = viewHolder.description;
        description.setText(item.getDescription());

        // get colors used to differentiate between selected and non-selected items
        int transparent = ContextCompat.getColor(context, android.R.color.transparent);
        int selectedColor = ContextCompat.getColor(context, R.color.colorAccent);
        int light_primary_text = ContextCompat.getColor(context, android.R.color.primary_text_dark);
        int dark_primary_text = ContextCompat.getColor(context, android.R.color.primary_text_light);
        int light_secondary_text = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
        int dark_secondary_text = ContextCompat.getColor(context, android.R.color.secondary_text_light);

        // change colors if item is selected or de-selected
        RelativeLayout selectableItem = viewHolder.selectableItem;
        if (isSelected(position)) {
            selectableItem.setBackgroundColor(selectedColor);
            viewHolder.title.setTextColor(dark_primary_text);
            viewHolder.description.setTextColor(dark_secondary_text);
        } else {
            selectableItem.setBackgroundColor(transparent);
            viewHolder.title.setTextColor(light_primary_text);
            viewHolder.description.setTextColor(light_secondary_text);
        }
    }

    /**
     * Get the number of items in this list
     *
     * @return the number of items in the list
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Indicates if the item at position position is selected
     *
     * @param position position of the item to check
     * @return true if the item is selected, false otherwise
     */
    private boolean isSelected(int position) {
        return getSelectedIndexes().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelections() {
        List<Integer> selectedIndexes = getSelectedIndexes();
        selectedItems.clear();
        for (Integer i : selectedIndexes) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     *
     * @return selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of selected items
     *
     * @return list of selected items ids
     */
    public List<Integer> getSelectedIndexes() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    /**
     * A custom ViewHolder for a NavigationItem
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final RelativeLayout selectableItem;

        ViewHolder(final View itemView) {
            super(itemView);

            title = (TextView)itemView.findViewById(android.R.id.text1);
            description = (TextView)itemView.findViewById(android.R.id.text2);
            selectableItem = (RelativeLayout)itemView.findViewById(R.id.selectable_item);

            // setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onClick(position);
                        }
                    }
                }
            });

            // setup the long click listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longClickListener.onLongClick(position);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    /**
     * Click listener interface that is used in this adapter
     */
    public interface OnItemClickListener {
        void onClick(int position);
    }

    /**
     * Long click listener interface that is used in this adapter
     */
    public interface OnItemLongClickListener {
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}

