package com.example.echols.studentschedule.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.models.Note;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView adapter for a notes list
 */
public class NotesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private SparseBooleanArray selectedNotes = new SparseBooleanArray();
    private final List<Note> notes;
    private final Context context;

    // set the size limit for an image note
    private static final int THUMB_WIDTH_LIMIT = 800;
    private static final int THUMB_HEIGHT_LIMIT = 800;

    /**
     * Create a new notes list adapter.
     * The custom ViewHolders for text notes and image notes are included in this class.
     *
     * @param context the context this adapter is in
     * @param notes   the list of notes displayed in the adapter
     */
    public NotesListAdapter(Context context, @NonNull List<Note> notes) {
        this.context = context;
        this.notes = notes;
        selectedNotes = new SparseBooleanArray();
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    //Returns the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        Note note = notes.get(position);
        if (note != null) {
            return note.getType();
        }
        return -1;
    }

    /**
     * Inflate the correct layout and return the correct ViewHolder based on the type of note
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the RecyclerView
     * @return the ViewHolder for the RecyclerView
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case Note.TEXT:
                View textView = inflater.inflate(R.layout.notes_text_item, parent, false);
                viewHolder = new TextViewHolder(textView);
                break;
            case Note.IMAGE:
                View imageView = inflater.inflate(R.layout.notes_image_item, parent, false);
                viewHolder = new ImageViewHolder(imageView);
                break;
        }
        return viewHolder;
    }

    /**
     * Bind the appropriate ViewHolder to the View in the RecyclerView
     *
     * @param viewHolder the ViewHolder to bind
     * @param position   the position of the View in the RecyclerView
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case Note.TEXT:
                TextViewHolder textView = (TextViewHolder)viewHolder;
                configureTextViewHolder(textView, position);
                break;
            case Note.IMAGE:
                ImageViewHolder imageView = (ImageViewHolder)viewHolder;
                configureImageViewHolder(imageView, position);
                break;
        }
    }

    /**
     * Configure a text note ViewHolder
     *
     * @param viewHolder the text note ViewHolder to configure
     * @param position   the position of the View in the RecyclerView
     */
    private void configureTextViewHolder(TextViewHolder viewHolder, int position) {


        Note note = notes.get(position);
        if (note != null) {
            try (InputStream inputStream = context.getContentResolver().openInputStream(note.getUri())) {
                if (inputStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    viewHolder.setText(stringBuilder.toString());
                }
            } catch (FileNotFoundException e) {
                Log.w("ExternalStorage", "File was not found:" + note.getUri());
                e.printStackTrace();
            } catch (IOException e) {
                Log.w("FileStreamReader", "Error reading file:" + note.getUri());
                e.printStackTrace();
            }

            int transparent = ContextCompat.getColor(context, android.R.color.transparent);
            int selectedColor = ContextCompat.getColor(context, R.color.colorAccent);
            int light_primary_text = ContextCompat.getColor(context, android.R.color.primary_text_dark);
            int dark_primary_text = ContextCompat.getColor(context, android.R.color.primary_text_light);

            if (isSelected(position)) {
                viewHolder.textView.setBackgroundColor(selectedColor);
                viewHolder.textView.setTextColor(dark_primary_text);
            } else {
                viewHolder.textView.setBackgroundColor(transparent);
                viewHolder.textView.setTextColor(light_primary_text);
            }
        }
    }

    /**
     * Configure a image note ViewHolder
     *
     * @param viewHolder the image note ViewHolder to configure
     * @param position   the position of the View in the RecyclerView
     */
    private void configureImageViewHolder(ImageViewHolder viewHolder, int position) {

        Note note = notes.get(position);
        if (note != null) {
            Bitmap bitmap = getThumbnail(note.getUri().getPath());
            viewHolder.getImageView().setImageBitmap(bitmap);

            viewHolder.imageView.setImageTintList(ContextCompat.getColorStateList(context, R.color.note_color_list));
            if (isSelected(position)) {
                viewHolder.imageView.setImageTintMode(PorterDuff.Mode.MULTIPLY);
            } else {
                viewHolder.imageView.setImageTintMode(PorterDuff.Mode.DST);
            }
        }
    }

    /**
     * Get a bitmap thumbnail to display in the ImageView for an image note
     *
     * @param path        the path to the image file to make a thumbnail for
     * @return a bitmap thumbnail based on the image provided
     */
    private Bitmap getThumbnail(String path) {
        // get the image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);

        // keep increasing the scale by the power of 2 until the limit sizes are met
        int thumbWidth = o.outWidth, thumbHeight = o.outHeight;
        double scale = 1;
        while (thumbWidth > NotesListAdapter.THUMB_WIDTH_LIMIT || thumbHeight > NotesListAdapter.THUMB_HEIGHT_LIMIT) {
            thumbWidth /= 2;
            thumbHeight /= 2;
            scale *= 2;
        }

        // return a bitmap at (or smaller than) the limit sizes
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = (int)scale;
        return BitmapFactory.decodeFile(path, o2);
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
        if (selectedNotes.get(position, false)) {
            selectedNotes.delete(position);
        } else {
            selectedNotes.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelections() {
        List<Integer> selectedIndexes = getSelectedIndexes();
        selectedNotes.clear();
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
        return selectedNotes.size();
    }

    /**
     * Indicates the list of selected items
     *
     * @return list of selected items ids
     */
    public List<Integer> getSelectedIndexes() {
        List<Integer> notes = new ArrayList<>(selectedNotes.size());
        for (int i = 0; i < selectedNotes.size(); ++i) {
            notes.add(selectedNotes.keyAt(i));
        }
        return notes;
    }

    /**
     * A custom ViewHolder for text notes
     */
    private class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        TextViewHolder(final View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.textNotes);

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

        void setText(CharSequence text) {
            textView.setText(text);
        }
    }

    /**
     * A custom ViewHolder for image notes
     */
    private class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        ImageViewHolder(final View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.notesImage);

            // setup the click listener
            imageView.setOnClickListener(new View.OnClickListener() {
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
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
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

        ImageView getImageView() {
            return imageView;
        }
    }

    private OnNoteClickListener clickListener;
    private OnNoteLongClickListener longClickListener;

    /**
     * Click listener interface that is used in this adapter
     */
    public interface OnNoteClickListener {
        void onClick(int position);
    }

    /**
     * Long click listener interface that is used in this adapter
     */
    public interface OnNoteLongClickListener {
        void onLongClick(int position);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnNoteLongClickListener(OnNoteLongClickListener listener) {
        this.longClickListener = listener;
    }
}
