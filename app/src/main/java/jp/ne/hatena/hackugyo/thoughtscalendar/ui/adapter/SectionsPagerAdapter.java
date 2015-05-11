package jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter;

import java.util.ArrayList;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.PlaceholderFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.TokyoArtBeatFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    // Show 2 total pages.
    private static final int CALENDARS_COUNT = 2;
    ArrayList<String> mCalendarNames;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        mCalendarNames = CustomApplication.getStringArrayById(R.array.list_calendar_names);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 2) return TokyoArtBeatFragment.newInstance();
        if (position < mCalendarNames.size()) return PlaceholderFragment.newInstance(position + 1);
        return TokyoArtBeatFragment.newInstance();
    }

    @Override
    public int getCount() {
        return CALENDARS_COUNT + 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 2) return "TokyoArtBeat";
        return mCalendarNames.size() <= position ? null : mCalendarNames.get(position);
    }
}