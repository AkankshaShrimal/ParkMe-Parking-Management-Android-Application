package com.android.parkme.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.parkme.R;
import com.android.parkme.utils.APIs;
import com.android.parkme.utils.ErrorHandler;
import com.android.parkme.utils.ErrorResponse;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private Button submit;
    private EditText email;
    private RequestQueue queue = null;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        submit = findViewById(R.id.submit_button);
        email = findViewById(R.id.fpassword_email_value);
        submit.setOnClickListener(v -> passwordModule());
        sharedpreferences = getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);
    }

    private void passwordModule() {
        if (Functions.networkCheck(getApplicationContext())) {
            if (email.getText().toString().equals("")) {
                email.setError("This is a mandatory field.");
                return;
            }
            JSONObject obj = new JSONObject();
            try {
                obj.put(Globals.EMAIL, email.getText().toString());
                String url = getResources().getString(R.string.url).concat(APIs.forgotPassword);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, obj, response -> {
                    Functions.exit(getApplicationContext(), sharedpreferences, "Password sent to " + email.getText().toString() + ". Please login again.");
                    finish();
                }, this::handleError);
                queue.add(request);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(VolleyError error) {
        ErrorResponse errorResponse = ErrorHandler.parseAndGetError(error);
        if (errorResponse.getStatusCode() <= 5000)
            Toast.makeText(getApplicationContext(), errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
        else
            email.setError(errorResponse.getErrorMessage());
    }

}