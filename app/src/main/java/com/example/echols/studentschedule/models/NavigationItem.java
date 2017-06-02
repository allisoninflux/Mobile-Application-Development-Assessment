package com.example.echols.studentschedule.models;

/**
 * A interface for an item that will be user by the ItemListFragment and ItemListAdapter
 */
public interface NavigationItem {
    long getId();
    String getTitle();
    String getDescription();
}
