package com.android.parkme.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.parkme.R;
import com.android.parkme.main.HomeFragment;
import com.android.parkme.utils.APIs;
import com.android.parkme.utils.ErrorHandler;
import com.android.parkme.utils.ErrorResponse;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindSlotsFragment extends Fragment {
    private static final String TAG = "FindSlotsFragment";
    RecyclerView recyclerView;
    List<SlotInfo> mslots;
    private ProgressBar pgsBar;
    private SharedPreferences sharedPreferences;
    private RequestQueue queue = null;
    private RecyclerAdaptor recyclerAdaptor;
    private int id;
    private boolean isBookedByMe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_slots, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        pgsBar = view.findViewById(R.id.pBar);

        mslots = new ArrayList<>();

        sharedPreferences = getActivity().getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);

        recyclerAdaptor = new RecyclerAdaptor(mslots);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerAdaptor);
        queue = Volley.newRequestQueue(getActivity());
        id = sharedPreferences.getInt(Globals.ID, 0);
        getAllRequest();
        return view;
    }

    private void handleError(VolleyError error) {
        pgsBar.setVisibility(View.GONE);
        ErrorResponse errorResponse = ErrorHandler.parseAndGetError(error);
        if (errorResponse.getStatusCode() <= 5000)
            Toast.makeText(getActivity(), errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_SHORT).show();
    }

    private void finish() {
        Functions.setCurrentFragment(getActivity(), new HomeFragment());
    }

    private void getAllRequest() {
        String url = getResources().getString(R.string.url).concat(APIs.getSlots);
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                Log.i(TAG, response);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                JSONArray jsonArray = new JSONArray(response);
                int sid, fromUser;
                String availability;
                long slotStart, slotEnd;
                for (int i = 0; i < jsonArray.length(); i++) {
                    sid = jsonArray.getJSONObject(i).getInt("id");
                    fromUser = jsonArray.getJSONObject(i).getInt("fromUser");
                    availability = jsonArray.getJSONObject(i).getString("slotAvailability");
                    slotStart = jsonArray.getJSONObject(i).getLong("slotStartTime");
                    slotEnd = jsonArray.getJSONObject(i).getLong("slotReleaseTime");
                    mslots.add(new SlotInfo(sid, fromUser, availability, slotStart, slotEnd));
                    if (id == jsonArray.getJSONObject(i).getInt("fromUser")) {
                        Log.i(TAG, "from user " + fromUser);
                        isBookedByMe = true;
                    }
                }
                recyclerAdaptor.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(Globals.SESSION_ID, sharedPreferences.getString(Globals.SESSION_KEY, ""));
                return params;
            }
        };
        queue.add(request);
    }

    private void updateRequest(int slotNumber, String status) {
        pgsBar.setVisibility(View.VISIBLE);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("id", slotNumber);
            requestObject.put("fromUser", id);
            requestObject.put("status", status);
            if (status == "BOOK")
                requestObject.put("slotStartTime", new Date().getTime());
            else
                requestObject.put("slotReleaseTime", new Date().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.url).concat(APIs.updateSlot);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestObject, response -> {
            try {
                isBookedByMe = true;

                pgsBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_LONG).show();
                final Runnable r = () -> finish();
                new Handler().postDelayed(r, 2000);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(Globals.SESSION_ID, sharedPreferences.getString(Globals.SESSION_KEY, ""));
                return params;
            }
        };
        queue.add(request);
    }

    public class RecyclerAdaptor extends RecyclerView.Adapter<ViewHolderClass> {
        List<SlotInfo> mSlots;

        public RecyclerAdaptor(List<SlotInfo> slots) {
            this.mSlots = slots;
        }

        @NonNull
        @Override
        public ViewHolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolderClass(LayoutInflater.from(parent.getContext()).inflate(R.layout.slots_details, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FindSlotsFragment.ViewHolderClass holder, int position) {
            holder.bind(this.mSlots.get(position));
        }


        @Override
        public int getItemCount() {
            return mSlots.size();
        }

    }

    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView slotNumberTextView, slotAvailabilityTextView;
        SlotInfo slot;

        public ViewHolderClass(View itemView) {
            super(itemView);
            slotNumberTextView = itemView.findViewById(R.id.slotNumber);
            slotAvailabilityTextView = itemView.findViewById(R.id.slotAvailability);
        }

        public void bind(SlotInfo slot) {
            this.slot = slot;
            slotNumberTextView.setText("Slot " + String.valueOf(slot.getId()));
            slotAvailabilityTextView.setText(slot.getSlotAvailability());
            if (slot.getSlotAvailability().toLowerCase().equals("occupied")) {
                slotAvailabilityTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
                if (id != slot.getFromUser())
                    slotAvailabilityTextView.setEnabled(false);
                else {
                    slotAvailabilityTextView.setOnClickListener(view -> {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Continue with Releasing the booked slot?");
                        builder.setPositiveButton("Confirm Release", (dialog, which) -> {
                            updateRequest(slot.getId(), "RELEASE");
                        });
                        builder.setNegativeButton("Cancel", (dialog, which) -> {
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }
            } else {
                slotAvailabilityTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue));
                slotAvailabilityTextView.setEnabled(true);
                slotAvailabilityTextView.setOnClickListener(view -> {
                    Log.i(TAG, "" + isBookedByMe);
                    if (isBookedByMe) {
                        Toast.makeText(getActivity(), "Please release booked slot first", Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Continue with Booking?");
                        builder.setPositiveButton("Confirm Booking", (dialog, which) -> {
                            updateRequest(slot.getId(), "BOOK");
                        });
                        builder.setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(getActivity(), "Booking Canceled", Toast.LENGTH_LONG).show();
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        }
    }

}