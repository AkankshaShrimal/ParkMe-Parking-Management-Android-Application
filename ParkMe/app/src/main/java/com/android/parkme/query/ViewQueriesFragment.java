package com.android.parkme.query;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.parkme.R;
import com.android.parkme.chat.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class ViewQueriesFragment extends Fragment {
    private static final String TAG = "ViewQueriesFragment";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_view_pager, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = v.findViewById(R.id.tab);
        viewPager = v.findViewById(R.id.myviewpager);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewpagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        RaisedQueryFragment raisedFragment = new RaisedQueryFragment();
        ReceivedQueryFragment receivedFragment = new ReceivedQueryFragment();
        viewpagerAdapter.addFragment(raisedFragment, "Raised By Me");
        viewpagerAdapter.addFragment(receivedFragment, "Raised Against Me");
        viewPager.setAdapter(viewpagerAdapter);
    }

}
