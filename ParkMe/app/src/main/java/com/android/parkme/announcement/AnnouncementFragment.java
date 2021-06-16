package com.android.parkme.announcement;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.parkme.R;
import com.android.parkme.database.Announcement;
import com.android.parkme.database.DatabaseClient;
import com.android.parkme.utils.Functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import recycler.coverflow.RecyclerCoverFlow;

public class AnnouncementFragment extends Fragment {
    private static final String TAG = "AnnouncementFragment";
    private final DateFormat simple = new SimpleDateFormat("MMM dd");
    private View view;
    private RecyclerCoverFlow mList;
    private List<Announcement> list;
    private AnnouncementAdapter announcementAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_announcement, container, false);
        new AnnouncementTask().execute();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mList = view.findViewById(R.id.list);
        list = new ArrayList<>();
        announcementAdapter = new AnnouncementAdapter(list);
        mList.setAdapter(announcementAdapter);
        // mList.setOnItemSelectedListener(position -> ((TextView) view.findViewById(R.id.index)).setText((position + 1) + "/" + mList.getLayoutManager().getItemCount()));
    }

    class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementHolder> {

        private List<Announcement> mAnnouncement;

        public AnnouncementAdapter(List<Announcement> announcement) {
            mAnnouncement = announcement;
        }

        @Override
        public AnnouncementHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_announcement, parent, false);
            return new AnnouncementHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnnouncementHolder holder, int position) {
            if (position == 0) {
                holder.v.setPadding(0, 200, 0, 0);
            }
            Announcement announcement = mAnnouncement.get(position);
            holder.bind(announcement);
        }

        @Override
        public int getItemCount() {
            return mAnnouncement.size();
        }
    }

    class AnnouncementHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Announcement mAnnouncement;
        private View v;
        private TextView messageTextView, mDateTextView;

        public AnnouncementHolder(View itemView) {
            super(itemView);
            v = itemView;
            itemView.setOnClickListener(this);
            messageTextView = itemView.findViewById(R.id.message);
            mDateTextView = itemView.findViewById(R.id.query_date);
        }

        public void bind(Announcement announcement) {
            mAnnouncement = announcement;
            messageTextView.setText(announcement.getMessage());
            messageTextView.setPaintFlags(messageTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            mDateTextView.setText(Functions.parseDateText(simple.format(announcement.getTime())));
        }

        @Override
        public void onClick(View v) {

        }

    }

    private class AnnouncementTask extends AsyncTask<Void, Void, List<Announcement>> {

        @Override
        protected List<Announcement> doInBackground(Void... params) {
            List<Announcement> ann = DatabaseClient.getInstance(getActivity()).getAppDatabase().parkMeDao().getAll();
            return ann;
        }

        @Override
        protected void onPostExecute(List<Announcement> announcements) {
            super.onPostExecute(announcements);
            list.addAll(announcements);
            announcementAdapter.notifyDataSetChanged();
        }
    }

}
