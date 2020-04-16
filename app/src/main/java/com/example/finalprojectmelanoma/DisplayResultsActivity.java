package com.example.finalprojectmelanoma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class DisplayResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean isMultiple = intent.getBooleanExtra("multiple", false);
        if(isMultiple){
            /* Display data as if it is a multiple */
        }
        else{
            /* Display data as if it is a single image */
        }

    }

}
