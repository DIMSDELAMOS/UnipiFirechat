package com.example.unipifirechat;

import androidx.multidex.MultiDexApplication;
import android.content.Context;

// Κληρονομεί από MultiDexApplication
public class BaseApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Το Multidex αρχικοποιείται εδώ, διασφαλίζοντας ότι όλες οι βιβλιοθήκες είναι διαθέσιμες.
    }
}