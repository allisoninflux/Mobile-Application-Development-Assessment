package com.example.echols.studentschedule.utils;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.echols.studentschedule.R;

/**
 * A custom action mode callback that handle the 'delete' action mode
 */
public class ActionModeCallback implements ActionMode.Callback {

    private ActionModeListener listener;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                listener.onActionClick();
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    public interface ActionModeListener {
        void onActionClick();
    }

    public void setOnActionModeListener(ActionModeListener listener) {
        this.listener = listener;
    }

}
