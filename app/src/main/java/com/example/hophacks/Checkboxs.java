package com.example.hophacks;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Checkboxs {
    public boolean maskRequired;
    public boolean sanitizerAvailable;

    public Checkboxs() {

    }
    public Checkboxs(boolean maskRequired, boolean sanitizerAvailable) {
        this.maskRequired = maskRequired;
        this.sanitizerAvailable = sanitizerAvailable;
    }
}
