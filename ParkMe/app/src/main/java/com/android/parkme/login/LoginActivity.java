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
import com.android.parkme.common.ForgotPasswordActivity;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Button login, loginUsingPhone;
    private RequestQueue queue = null;
    private TextView forgotPassword;
    private TextInputEditText emailInput, passwordInput;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        queue = Volley.newRequestQueue(this);
        sharedpreferences = getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        login = findViewById(R.id.login_button);
        loginUsingPhone = findViewById(R.id.login_sign_in_with_the_phone_number);
        forgotPassword = findViewById(R.id.f_password_text);
        emailInput = findViewById(R.id.login_email_value);
        passwordInput = findViewById(R.id.login_password_value);

        emailInput.setText("shivam20121@iiitd.ac.in");
        passwordInput.setText("xxxxaaaaX1!");
        login.setOnClickListener(v -> {
            loginRequest();
        });
        forgotPassword.setOnClickListener(v -> goToPassword());
        loginUsingPhone.setOnClickListener(v -> goToPhoneLogin());
    }

    private void goToPassword() {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void goToPhoneLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginPhoneActivity.class);
        startActivity(intent);
    }

    private void loginRequest() {
        if (Functions.networkCheck(getApplicationContext())) {
            Log.i(TAG, "Authenticating login at " + getResources().getString(R.string.url).toString().concat(APIs.doLogin));
            JSONObject loginObject = new JSONObject();
            try {
                loginObject.put(Globals.EMAIL, emailInput.getText().toString());
                loginObject.put(Globals.PASSWORD, passwordInput.getText().toString());
                loginObject.put(Globals.TOKEN, sharedpreferences.getString(Globals.TOKEN, null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(
                    getResources().getString(R.string.url).toString().concat(APIs.doLogin),
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
            emailInput.setError(errorResponse.getErrorMessage());
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
            editor.putString(Globals.ADDRESS, response.getString(Globals.ADDRESS));
            editor.putLong(Globals.NUMBER, Long.parseLong(response.getString(Globals.NUMBER)));
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSuccess() {
        Log.i(TAG, "Login Successful... redirecting to the main page");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

}