package com.android.parkme.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.parkme.R;
import com.android.parkme.main.MainActivity;
import com.android.parkme.utils.APIs;
import com.android.parkme.utils.ErrorHandler;
import com.android.parkme.utils.ErrorResponse;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPhoneActivity extends AppCompatActivity {
    private static final String TAG = "LoginPhoneActivity";
    private Button login;
    private RequestQueue queue = null;
    private SharedPreferences sharedpreferences;
    private TextView phoneInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);
        queue = Volley.newRequestQueue(this);
        sharedpreferences = getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        login = findViewById(R.id.login_button);
        phoneInput = findViewById(R.id.login_phone_value);
        passwordInput = findViewById(R.id.login_password_value);
        login.setOnClickListener(v -> loginRequest());
    }

    private void loginRequest() {
        if (Functions.networkCheck(getApplicationContext())) {
            Log.i(TAG, "Authenticating login at " + getResources().getString(R.string.url).concat(APIs.doLoginPhone));
            JSONObject loginObject = new JSONObject();
            try {
                loginObject.put(Globals.NUMBER, phoneInput.getText().toString());
                loginObject.put(Globals.PASSWORD, passwordInput.getText().toString());
                loginObject.put(Globals.TOKEN, sharedpreferences.getString(Globals.TOKEN, null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(
                    getResources().getString(R.string.url).concat(APIs.doLoginPhone),
                    loginObject,
                    response -> {
                        Log.i(TAG, "Authentication Success");
                        if (null != response) {
                            storeFields(response);
                            onSuccess();
                        }
                    }, this::handleError);
            queue.add(request);
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(VolleyError error) {
        ErrorResponse errorResponse = ErrorHandler.parseAndGetError(error);
        if (errorResponse.getStatusCode() == 0 || errorResponse.getStatusCode() == 5000)
            Toast.makeText(getApplicationContext(), errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
        else if (errorResponse.getStatusCode() < 6000)
            phoneInput.setError(errorResponse.getErrorMessage());
        else
            passwordInput.setError(errorResponse.getErrorMessage());
    }

    private void storeFields(JSONObject response) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        try {
            editor.putString(Globals.SESSION_KEY, response.getString(Globals.SESSION_KEY));
            editor.putInt(Globals.ID, Integer.parseInt(response.getString(Globals.ID)));
            editor.putString(Globals.NAME, response.getString(Globals.NAME));
            editor.putString(Globals.EMAIL, response.getString(Globals.EMAIL));
            editor.putLong(Globals.NUMBER, Long.parseLong(response.getString(Globals.NUMBER)));
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSuccess() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }
}