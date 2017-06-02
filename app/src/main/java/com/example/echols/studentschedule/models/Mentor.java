package com.example.echols.studentschedule.models;

import android.support.annotation.NonNull;

public class Mentor {
    private long id;
    private String name;
    private String number;
    private String email;

    public Mentor() {
    }

    public Mentor(@NonNull String name, @NonNull String number, @NonNull String email) {
        this.name = name;
        this.number = number;
        this.email = email;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "No mentor information";
        } else {
            return name;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Mentor)) {
            return false;
        } else {
            Mentor otherMentor = (Mentor)obj;
            return otherMentor.getName().equals(getName());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(@NonNull String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }
}
