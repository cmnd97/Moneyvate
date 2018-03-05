package com.cmnd97.moneyvate;

/**
 * Created by cristi-mnd on 12.02.18.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


class UIFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    ProfileFragment profileFragment = null;
    ScanFragment scanFragment = null;
    TaskFragment taskFragment = null;

    UIFragmentPagerAdapter(FragmentManager fm, Context context, ProfileFragment profileFragment, ScanFragment scanFragment, TaskFragment taskFragment) {
        super(fm);
        this.context = context;
        this.profileFragment = profileFragment;
        this.scanFragment = scanFragment;
        this.taskFragment = taskFragment;
    }

    private int tabTitlesIds[] = new int[]{R.string.your_profile, R.string.scan_a_tag, R.string.your_tasks};

    @Override
    public Fragment getItem(int position) {
        Fragment result = null;
        switch (position) {
            case 0:
                result = profileFragment;
                break;
            case 1:
                result = scanFragment;
                break;
            case 2:
                result = taskFragment;
                break;
        }
        return result;

    }

    @Override
    public int getCount() {
        return 3;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(tabTitlesIds[position]);
    }


}
