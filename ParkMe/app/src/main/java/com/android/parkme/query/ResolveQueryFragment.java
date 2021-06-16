package com.android.parkme.query;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.parkme.R;
import com.android.parkme.chat.ChatFragment;
import com.android.parkme.database.DatabaseClient;
import com.android.parkme.database.Query;
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
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResolveQueryFragment extends Fragment {
    private static final String TAG = "ResolveQueryFragment";
    RequestQueue queue = null;
    private TextView queryNumber, dateCreateText, messageText, vehicleNumber;
    private SimpleRatingBar ratingbar;
    private ImageView vehicleNumberImage;
    private Button resolveButton, chatButton;
    private SharedPreferences sharedpreferences;
    private Query mQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_query_details_resolve, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        sharedpreferences = getActivity().getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        queryNumber = getActivity().findViewById(R.id.query_number_qd);
        messageText = getActivity().findViewById(R.id.message_text_qd);
        dateCreateText = getActivity().findViewById(R.id.create_date_value_qd);
        vehicleNumberImage = getActivity().findViewById(R.id.clicked_image_qd);
        vehicleNumber = getActivity().findViewById(R.id.vehicle_number_qd);
        resolveButton = getActivity().findViewById(R.id.cancel_button);
        ratingbar = getActivity().findViewById(R.id.ratingBar);
        ratingbar.setBorderColor(getResources().getColor(R.color.orange));
        ratingbar.setFillColor(getResources().getColor(R.color.orange));
        chatButton = getActivity().findViewById(R.id.chat_button);

        queryNumber.setText(String.valueOf(getArguments().getInt(Globals.QID)));
        ratingbar.setOnRatingBarChangeListener((x, y, z) -> {
            Log.i(TAG, "" + ratingbar.getRating());
            if (y <= 2) {
                ratingbar.setBorderColor(getResources().getColor(R.color.red));
                ratingbar.setFillColor(getResources().getColor(R.color.red));
            } else if (y <= 4) {
                ratingbar.setBorderColor(getResources().getColor(R.color.orange));
                ratingbar.setFillColor(getResources().getColor(R.color.orange));
            } else {
                ratingbar.setBorderColor(getResources().getColor(R.color.golden_stars));
                ratingbar.setFillColor(getResources().getColor(R.color.golden_stars));
            }
        });
        resolveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Resolve Query")
                    .setMessage("Do you really want to give " + (ratingbar.getRating() % 1 == 0 ? new BigDecimal(ratingbar.getRating()).stripTrailingZeros() : ratingbar.getRating()) + "/5 rating?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> resolveQuery())
                    .setNegativeButton(android.R.string.no, null).show();
        });


        chatButton.setOnClickListener(e -> {
            Bundle bundle = new Bundle();
            ChatFragment chatFragment = new ChatFragment();
            bundle.putInt(Globals.QID, mQuery.getQid());
            bundle.putString(Globals.STATUS, getArguments().getString(Globals.STATUS));
            bundle.putInt(Globals.TO_USER_ID, getArguments().getInt(Globals.TO_USER_ID));
            chatFragment.setArguments(bundle);
            getActivity().runOnUiThread(() -> Functions.setCurrentFragment(getActivity(), chatFragment));
        });
        new RetrieveQuery().execute(getArguments().getInt(Globals.QID));
    }

    private void resolveQuery() {
        if (Functions.networkCheck(getActivity())) {
            String url = getResources().getString(R.string.url).concat(APIs.resolveQuery);
            Log.i(TAG, "Resolve Query " + url);
            JSONObject resolveQueryObject = new JSONObject();
            try {
                resolveQueryObject.put(Globals.STATUS, Globals.QUERY_CLOSE_STATUS);
                resolveQueryObject.put(Globals.QID, String.valueOf(getArguments().getInt(Globals.QID)));
                resolveQueryObject.put(Globals.QUERY_RESOLVE_DATE, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
                resolveQueryObject.put(Globals.RATING, ratingbar.getRating());

                JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, resolveQueryObject, response -> {
                    try {
                        Toast.makeText(getActivity(), response.getString(Globals.MESSAGE), Toast.LENGTH_SHORT).show();
                        mQuery.setStatus(Globals.QUERY_CLOSE_STATUS);
                        mQuery.setCloseTime(new Date().getTime());
                        mQuery.setRating(ratingbar.getRating());
                        new ResolveQuery().execute(mQuery);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, this::handleError) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("session-id", sharedpreferences.getString(Globals.SESSION_KEY, ""));
                        return params;
                    }
                };
                queue.add(request);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else
            Functions.showToast(getActivity(), "Please connect to Internet");
    }

    private void handleError(VolleyError error) {
        ErrorResponse errorResponse = ErrorHandler.parseAndGetError(error);
        if (errorResponse.getStatusCode() == 0 || errorResponse.getStatusCode() == 5000)
            Toast.makeText(getActivity(), errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
        else {
            Functions.exit(getActivity(), sharedpreferences, null);
            getActivity().finish();
        }
    }

    private void updateUI() {
        messageText.setText(mQuery.getMsg());
        vehicleNumber.setText(mQuery.getVid());
        dateCreateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(mQuery.getCreateTime())));
        Bitmap bitmap = Functions.loadResourceFromLocalStorage(getActivity(), mQuery.getQid());
        if (bitmap == null)
            Functions.getQidImage(getActivity(), mQuery.getQid(), vehicleNumberImage);
        else
            vehicleNumberImage.setImageBitmap(bitmap);
    }

    private class RetrieveQuery extends AsyncTask<Integer, Void, Query> {

        @Override
        protected Query doInBackground(Integer... params) {
            return DatabaseClient.getInstance(getContext()).getAppDatabase().parkMeDao().getQuery(params[0]);
        }

        @Override
        protected void onPostExecute(Query query) {
            super.onPostExecute(query);
            mQuery = query;
            updateUI();
        }
    }

    private class ResolveQuery extends AsyncTask<Query, Void, Void> {

        @Override
        protected Void doInBackground(Query... params) {
            DatabaseClient.getInstance(getContext()).getAppDatabase().parkMeDao().updateCloseRequest(params[0].getStatus(), params[0].getCloseTime(), params[0].getQid(), params[0].getRating());
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Functions.setCurrentFragment(getActivity(), new HomeFragment());
        }
    }
}
