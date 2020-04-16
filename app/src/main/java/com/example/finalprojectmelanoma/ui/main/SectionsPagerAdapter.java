package com.example.finalprojectmelanoma.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.finalprojectmelanoma.Fragments.HistoryFragment;
import com.example.finalprojectmelanoma.Fragments.InfoFragment;
import com.example.finalprojectmelanoma.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1,R.string.tab_text_2};
    private final Context mContext;

    private HistoryFragment hs;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = null;
        /*
        * 0 for information
        * 1 for run model
        * 2 for history page
        * */
        switch(position){
            case 0 : f = InfoFragment.newInstance("","");
                break;
            case 1 :
                if(hs != null){
                    return hs;
                }
                else{
                    hs = HistoryFragment.newInstance("","");
                    f = hs;
                }
                break;
        }
        return f;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    public HistoryFragment getHistoryFragment(){
        if( hs != null ){
            return hs;
        }
        else{
            hs = HistoryFragment.newInstance("","");
            return hs;
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}