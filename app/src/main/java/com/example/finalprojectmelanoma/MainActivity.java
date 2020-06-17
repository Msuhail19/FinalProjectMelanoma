package com.example.finalprojectmelanoma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.finalprojectmelanoma.Fragments.HistoryFragment;
import com.example.finalprojectmelanoma.Fragments.InfoFragment;
import com.example.finalprojectmelanoma.MultipleRunActivity.NewRunActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.finalprojectmelanoma.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        HistoryFragment.OnFragmentInteractionListener,
        InfoFragment.OnFragmentInteractionListener{

    private static List<RunDetails> runDetailsList = new ArrayList<>();
    public static float threshold = 50;

    SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base);
        showDisclaimer();

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabs.setupWithViewPager(viewPager);

        final String[] option = {"  Single Image" , "  Multiple Images"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,option);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select mode to run model : ");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        openRunActivity();
                        break;
                    case 1:
                        openNewRunActivity();
                        break;
                }

            }
        });
        final  AlertDialog a = builder.create();


        /*
        * Set view to allow user to run TFlite Interpreter
        * */
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                a.show();
            }
        });
    }

    /* To deal with fragment communication */
    @Override
    public void onFragmentInteraction(Uri uri) {
        // Leave blank its fine

    }

    public void openRunActivity(){
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
        HistoryFragment hs = sectionsPagerAdapter.getHistoryFragment();
        hs.setmRecycler();
    }

    public void openNewRunActivity(){
        Intent intent = new Intent(this, NewRunActivity.class);
        startActivity(intent);
        HistoryFragment hs = sectionsPagerAdapter.getHistoryFragment();
        hs.setmRecycler();
    }

    public void showDisclaimer(){
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        prompt.setCancelable(false);
        prompt.setTitle("Disclaimer");
        prompt.setIcon(android.R.drawable.ic_dialog_alert);
        prompt.setMessage ("This application is to be used for research purposes only and is not to be taken as a substitute for legitimate medical advice.");
        prompt.setPositiveButton("I UNDERSTAND.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        prompt.show();
    }




}
