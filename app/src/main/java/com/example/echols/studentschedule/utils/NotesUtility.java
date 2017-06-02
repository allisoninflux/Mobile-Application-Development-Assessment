package com.example.echols.studentschedule.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.example.echols.studentschedule.R;
import com.example.echols.studentschedule.dialogs.NoteTypeDialogFragment;
import com.example.echols.studentschedule.models.Assessment;
import com.example.echols.studentschedule.models.Course;
import com.example.echols.studentschedule.models.NavigationItem;
import com.example.echols.studentschedule.models.Note;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A utility class to help with the management of notes in courses and assessments
 */
public class NotesUtility {

    private final Context context;
    private final OnAddNoteListener listener;
    public final static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    /**
     * Create a new NotesUtility object
     *
     * @param context  the context in which this utility is being created
     * @param listener the add note listener for this utility
     */
    public NotesUtility(Context context, OnAddNoteListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Asks the user which type of note to be added
     */
    public void getNoteTypeToAdd() {
        NoteTypeDialogFragment noteTypeDialogFragment = NoteTypeDialogFragment.newInstance();
        noteTypeDialogFragment.setOnSelectNoteTypeListener(new NoteTypeDialogFragment.OnSelectNoteTypeListener() {
            @Override
            public void onSelectNoteType(int selection) {
                switch (selection) {
                    case Note.TEXT:
                        listener.addTextNote();
                        break;
                    case Note.IMAGE:
                        listener.addImageNote();
                        break;
                }
            }
        });
        noteTypeDialogFragment.show(((Activity)context).getFragmentManager(), "select_note_type_dialog");
    }

    /**
     * Get the 'Share Notes' Intent based on a list of notes
     * @param notes the list of notes to include in the share intent
     * @return an intent that allows the user to share a set of notes
     */
    public Intent getShareNotesIntent(List<Note> notes) {
        ArrayList<Uri> notesToShare = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();

        for (Note note : notes) {
            File file = new File(note.getUri().getPath());
            switch (note.getType()) {
                case Note.TEXT:
                    textBuilder.append(note.getFileName()).append(":\n");
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            textBuilder.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        Log.e("ReadFileError", e.getMessage());
                    }
                    break;
                case Note.IMAGE:
                    Uri contentUri = FileProvider.getUriForFile(context, "com.example.echols.studentschedule.provider", file);
                    notesToShare.add(contentUri);
                    break;
            }
            if (!textBuilder.toString().isEmpty()) {
                textBuilder.append("\n\n");
            }
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putExtra(Intent.EXTRA_TEXT, textBuilder.toString());
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, notesToShare);
        shareIntent.setType("image/*");

        return shareIntent;
    }

    /**
     * Get the 'Show Image' Intent for an image file
     * @param file the image file to show
     * @return an intent that will show an image in an app selected by the user (or the default image viewing app)
     */
    public Intent getShowImageIntent(File file) {
        Uri imageUri = FileProvider.getUriForFile(context, "com.example.echols.studentschedule.provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(imageUri, "image/*");
        return intent;
    }

    /**
     * Copy a file from one location to another
     * @param source the source Uri that contains the data to be copied
     * @param destination the destination Uri to copy the data to
     */
    public void copyFile(Uri source, Uri destination) {
        String sourcePath = "";
        try (Cursor cursor = context.getContentResolver().query(source, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                sourcePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
        }

        try (InputStream in = new FileInputStream(sourcePath);
             OutputStream out = new FileOutputStream(destination.getPath())) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.e("CopyFileError", e.getMessage());
        }
    }

    /**
     * Create a new text file
     * @param item the parent item that this text file will belong to
     * @param textToWrite the text data to write to the file
     * @return the text file created
     */
    public File createTextFile(NavigationItem item, String textToWrite) {
        File destination = createFile(item, "txt");

        try (BufferedWriter writer = new BufferedWriter(new PrintWriter(destination))) {
            writer.write(textToWrite);
        } catch (IOException e) {
            Log.e("WriteFileError", e.getMessage());
        }
        return destination;
    }

    /**
     * Create a new image file
     * @param item the parent item that this image file will belong to
     * @return the image file created
     */
    private File createImageFile(NavigationItem item) {
        return createFile(item, "jpg");
    }

    /**
     * Create a file that belongs 'in' a parent item (i.e. course or assessment)
     * @param item the parent item that this file will belong to
     * @param ext the extension for the new file
     * @return the newly created file
     */
    private File createFile(NavigationItem item, String ext) {
        // make sure that the extension includes the separator
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        // get the type which will become the folder that this file will be created in
        String type = "unknown";
        if (item instanceof Course) {
            type = context.getString(R.string.notes_course_directory);
        } else if (item instanceof Assessment) {
            type = context.getString(R.string.notes_assessments_directory);
        }

        // get the folder that the file will be saved to based on the id of the parent and the type of file
        File mainFolder = Environment.getExternalStorageDirectory();
        File folder = new File(mainFolder, "Student Schedule/notes/" + type + "/" + item.getId());

        // if the folder doesn't exist, the folder will be created
        if (!folder.exists() && !folder.mkdirs()) {
            Log.w("ExternalStorage", "Directory could not be created.");
        }

        // create a unique file name based on the date and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "NOTE_" + timeStamp + "_";

        File file = null;
        try {
            file = File.createTempFile(imageFileName, ext, folder);
        } catch (IOException ex) {
            Log.w("ExternalStorage", "Error writing " + imageFileName, ex);
        }

        return file;
    }

    /**
     * Get the Uri and Intent for an Image Chooser Activity
     * @param item the parent item that the image will belong to
     * @return the uri of the new (or copied) image
     */
    public UriAndIntent getImageChooserIntent(NavigationItem item) {

        Uri uri = Uri.fromFile(createImageFile(item));

        // initialize the camera intent
        List<Intent> cameraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = context.getPackageManager().queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            String packageName = res.activityInfo.packageName;
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraIntents.add(intent);
        }

        // select image from gallery intent
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // chooser intent
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // add additional camera intent option
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        return new UriAndIntent(uri, chooserIntent);
    }

    /**
     * A custom class for convenience to return a Uri and Intent together
     */
    public class UriAndIntent {
        private final Uri uri;
        private final Intent intent;

        UriAndIntent(Uri uri, Intent intent) {
            this.uri = uri;
            this.intent = intent;
        }

        public Intent getIntent() {
            return intent;
        }

        public Uri getUri() {
            return uri;
        }
    }

    /**
     * An interface that will allow the addition of notes to be handled by the parent activity
     */
    public interface OnAddNoteListener {
        void addTextNote();

        void addImageNote();
    }

}
