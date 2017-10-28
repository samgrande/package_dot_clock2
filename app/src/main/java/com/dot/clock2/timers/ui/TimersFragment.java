/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dot.clock2.timers.ui;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dot.clock2.list.RecyclerViewFragment;
import com.dot.clock2.timers.EditTimerActivity;
import com.dot.clock2.timers.Timer;
import com.dot.clock2.timers.data.AsyncTimersTableUpdateHandler;
import com.dot.clock2.timers.data.TimerCursor;
import com.dot.clock2.timers.data.TimersListCursorLoader;
import com.dot.clock2.util.ConfigurationUtils;

import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

public class TimersFragment extends RecyclerViewFragment<Timer, TimerViewHolder, TimerCursor, TimersCursorAdapter> {
    // TODO: Different number of columns for different display densities, instead of landscape.
    // Use smallest width qualifiers. I can imagine 3 or 4 columns for a large enough tablet in landscape.
    private static final int LANDSCAPE_LAYOUT_COLUMNS = 2;

    public static final int REQUEST_CREATE_TIMER = 0;
    public static final String EXTRA_SCROLL_TO_TIMER_ID = "com.dot.clock2.timers.extra.SCROLL_TO_TIMER_ID";

    private AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncTimersTableUpdateHandler = new AsyncTimersTableUpdateHandler(getActivity(), this);

        long scrollToStableId = getActivity().getIntent().getLongExtra(EXTRA_SCROLL_TO_TIMER_ID, -1);
        if (scrollToStableId != -1) {
            setScrollToStableId(scrollToStableId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        final Resources r = getResources();
        if (ConfigurationUtils.getOrientation(r) == Configuration.ORIENTATION_LANDSCAPE) {
            RecyclerView list = ButterKnife.findById(view, com.dot.clock2.R.id.list);
            int cardViewMargin = r.getDimensionPixelSize(com.dot.clock2.R.dimen.cardview_margin);
            list.setPaddingRelative(cardViewMargin/*start*/, cardViewMargin/*top*/, 0, list.getPaddingBottom());
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        // TODO: From EditTimerActivity, pass back the Timer as a parcelable and
        // retrieve it here directly.
        int hour = data.getIntExtra(EditTimerActivity.EXTRA_HOUR, -1);
        int minute = data.getIntExtra(EditTimerActivity.EXTRA_MINUTE, -1);
        int second = data.getIntExtra(EditTimerActivity.EXTRA_SECOND, -1);
        String label = data.getStringExtra(EditTimerActivity.EXTRA_LABEL);
        boolean startTimer = data.getBooleanExtra(EditTimerActivity.EXTRA_START_TIMER, false);
        // TODO: Timer's group?

        Timer t = Timer.createWithLabel(hour, minute, second, label);
        if (startTimer) {
            t.start();
        }
        mAsyncTimersTableUpdateHandler.asyncInsert(t);
    }

    @Override
    public void onFabClick() {
        Intent intent = new Intent(getActivity(), EditTimerActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_TIMER);
    }

    @Override
    protected TimersCursorAdapter onCreateAdapter() {
        // Create a new adapter. This is called before we can initialize mAsyncTimersTableUpdateHandler,
        // so right now it is null. However, after super.onCreate() returns, it is initialized, and
        // the reference variable will be pointing to an actual object. This assignment "propagates"
        // to all references to mAsyncTimersTableUpdateHandler.
        return new TimersCursorAdapter(this, mAsyncTimersTableUpdateHandler);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        switch (ConfigurationUtils.getOrientation(getResources())) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return new GridLayoutManager(getActivity(), LANDSCAPE_LAYOUT_COLUMNS);
            default:
                return super.getLayoutManager();
        }
    }

    @Override
    protected int emptyMessage() {
        return com.dot.clock2.R.string.empty_timers_container;
    }

    @Override
    protected int emptyIcon() {
        return com.dot.clock2.R.drawable.ic_timer_96dp;
    }

    @Override
    public Loader<TimerCursor> onCreateLoader(int id, Bundle args) {
        return new TimersListCursorLoader(getActivity());
    }

    @Override
    public void onListItemClick(Timer item, int position) {

    }

    @Override
    public void onListItemDeleted(Timer item) {

    }

    @Override
    public void onListItemUpdate(Timer item, int position) {

    }

    @Override
    protected void onScrolledToStableId(long id, int position) {

    }
}
