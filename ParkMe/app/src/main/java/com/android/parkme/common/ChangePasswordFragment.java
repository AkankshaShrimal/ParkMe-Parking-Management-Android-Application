package com.android.parkme.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.parkme.R;
import com.android.parkme.utils.APIs;
import com.android.parkme.utils.ErrorHandler;
import com.android.parkme.utils.ErrorResponse;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ChangePasswordFragment extends Fragment {
    private static final String TAG = "ChangePasswordFragment";
    private RequestQueue queue = null;
    private Button cSubmit;
    private EditText emailText, oldPassword, newPassword, newConfirmPassword;
    private SharedPreferences sharedpreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        queue = Volley.newRequestQueue(getActivity());
        cSubmit = view.findViewById(R.id.cpassword_button);
        emailText = view.findViewById(R.id.cpassword_email_value);
        oldPassword = view.findViewById(R.id.cpassword_old_value);
        newPassword = view.findViewById(R.id.cpassword_new_value);
        newConfirmPassword = view.findViewById(R.id.cpassword_new_confirm_value);
        emailText.setEnabled(false);
        sharedpreferences = getActivity().getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        emailText.setText(sharedpreferences.getString(Globals.EMAIL, ""));
        cSubmit.setOnClickListener(v -> onSubmit());
        return view;
    }

    private void onSubmit() {
        if (Functions.networkCheck(getContext())) {
            if (oldPassword.getText().toString().equals("") || newPassword.getText().toString().equals("") || newConfirmPassword.getText().toString().equals("")) {
                Toast.makeText(getContext(), "All Fields are mandatory", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.getText().toString().equals(newConfirmPassword.getText().toString())) {
                Toast.makeText(getContext(), "Passwords Should Match", Toast.LENGTH_SHORT).show();
                return;
            }
            String url = getActivity().getResources().getString(R.string.url).concat(APIs.changePassword);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, getJsonObject(), response -> {
                Functions.exit(getActivity(), sharedpreferences, "Password updated successfully");
                getActivity().finish();
            }, this::handleError) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put(Globals.SESSION_ID, sharedpreferences.getString(Globals.SESSION_KEY, ""));
                    return params;
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        String jsonString = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                        JSONObject result = null;
                        if (jsonString != null && jsonString.length() > 0)
                            result = new JSONObject(jsonString);
                        return Response.success(result,
                                HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };
            queue.add(request);
        } else {
            Toast.makeText(getContext(), "Please connect to Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Globals.EMAIL, emailText.getText().toString());
            jsonObject.put(Globals.OLD_PASSWORD, oldPassword.getText().toString());
            jsonObject.put(Globals.NEW_PASSWORD, newPassword.getText().toString());
            jsonObject.put(Globals.NEW_CONFIRM_PASSWORD, newConfirmPassword.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void handleError(VolleyError error) {
        ErrorResponse errorResponse = ErrorHandler.parseAndGetError(error);
        if (errorResponse.getStatusCode() < 6000 || errorResponse.getStatusCode() == 6004)
            Toast.makeText(getActivity(), errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
        else if (errorResponse.getStatusCode() == 6003) {
            newPassword.setError(errorResponse.getErrorMessage());
            newConfirmPassword.setError(errorResponse.getErrorMessage());
        } else if (errorResponse.getStatusCode() == 6002)
            oldPassword.setError(errorResponse.getErrorMessage());
        else {
            Functions.exit(getActivity(), sharedpreferences, null);
            getActivity().finish();
        }
    }

}