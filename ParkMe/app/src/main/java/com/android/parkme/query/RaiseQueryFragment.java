package com.android.parkme.query;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.parkme.R;
import com.android.parkme.database.DatabaseClient;
import com.android.parkme.database.Query;
import com.android.parkme.main.HomeFragment;
import com.android.parkme.utils.APIs;
import com.android.parkme.utils.DataPart;
import com.android.parkme.utils.ErrorHandler;
import com.android.parkme.utils.ErrorResponse;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.android.parkme.utils.VolleyImageRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaiseQueryFragment extends Fragment {
    private static final String TAG = "RaiseQueryFragment";
    private ArrayAdapter<String> queryTypeAdaptor;
    private Spinner queryTypeDropdown;
    private EditText dateText, messageText, vehicleNumber;
    private MaterialTextView addImageError;
    private ImageView clickedImage;
    private FloatingActionButton addImage;
    private Button resetBtn, sendBtn;
    private Bitmap bitmap;
    private byte[] bArray;
    private RequestQueue queue = null;
    private SharedPreferences sharedPreferences;
    private View view;
    private JSONObject requestObject, responseObject;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_raise_query, container, false);
        sharedPreferences = getActivity().getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong("dropDown", 0).commit();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        queryTypeDropdown.setSelection((int) sharedPreferences.getLong("dropDown", 0));
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.edit().putLong("dropDown", queryTypeDropdown.getSelectedItemPosition()).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queryTypeAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.query_types_array));
        queryTypeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        queryTypeDropdown = view.findViewById(R.id.dropdown_query_types);
        messageText = view.findViewById(R.id.message_text);
        vehicleNumber = view.findViewById(R.id.number_value);
        clickedImage = view.findViewById(R.id.clicked_image);
        sendBtn = view.findViewById(R.id.send_button);
        addImage = view.findViewById(R.id.add_image_button);
        dateText = view.findViewById(R.id.date_value);
        resetBtn = view.findViewById(R.id.reset_button);
        addImageError = view.findViewById(R.id.add_image_error);

        queryTypeDropdown.setAdapter(queryTypeAdaptor);
        dateText.setText(new SimpleDateFormat("YYYY-MM-dd HH:mm").format(new Date()));

        addImage.setOnClickListener(v -> {
            addImageError.setVisibility(View.GONE);
            if (Functions.checkAndRequestPermissions(getActivity())) {
                CropImage.activity().start(getContext(), RaiseQueryFragment.this);
            }
        });
        sendBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Are you sure you want to Raise the Query?");
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                raiseQuery();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(getActivity(), "Query not Raised!", Toast.LENGTH_LONG).show();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        resetBtn.setOnClickListener(v -> {
            String compareValue = "--Select Query Type--";
            int spinnerPosition = queryTypeAdaptor.getPosition(compareValue);
            queryTypeDropdown.setSelection(spinnerPosition);
            messageText.getText().clear();
            vehicleNumber.getText().clear();
            clickedImage.setVisibility(View.GONE);
        });
    }

    public boolean checkValidation() {
        if (queryTypeDropdown.getSelectedItem().toString().equals("--Select Query Type--")) {
            TextView errorText = (TextView) queryTypeDropdown.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText("Please Select a Query Type");//changes the selected item text to this
            queryTypeDropdown.requestFocus();
            return false;
        } else if (messageText.length() <= 0) {
            messageText.requestFocus();
            messageText.setError("Enter Message");
            return false;
        } else if (clickedImage.getDrawable() == null) {
            addImageError.setTextColor(Color.RED);
            addImageError.setText("Please Click Vehicle Number Plate Image");
            addImageError.setVisibility(View.VISIBLE);
            return false;
        } else if (vehicleNumber.length() <= 0) {
            vehicleNumber.requestFocus();
            vehicleNumber.setError("Enter vehicleNumber if not Extracted correctly by Image");
            return false;
        } else {
            return true;
        }
    }

    private void raiseQuery() {
        if (checkValidation()) {
            if (Functions.networkCheck(getContext())) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bArray = bos.toByteArray();
                    String base64 = Base64.encodeToString(bArray, Base64.DEFAULT);
                    String url = getResources().getString(R.string.url).concat(APIs.raiseQuery);
                    Log.i(TAG, "Raising Query " + url);
                    requestObject = new JSONObject();
                    requestObject.put(Globals.QUERY_TYPE, queryTypeDropdown.getSelectedItem().toString());
                    requestObject.put(Globals.STATUS, Globals.QUERY_DEFAULT_STATUS);
                    requestObject.put(Globals.MESSAGE, messageText.getText().toString());
                    requestObject.put(Globals.QUERY_CREATE_DATE, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
                    requestObject.put(Globals.VEHICLE_REGISTRATION_NUMBER, vehicleNumber.getText().toString());

                    JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, requestObject, response -> {
                        responseObject = response;
                        Log.i(TAG, "Query Raised Successfully");
                        try {
                            uploadBitmap(bitmap, responseObject.getString(Globals.QID));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                        handleError(error);
                        Functions.showToast(getActivity(), "An error occurred");
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put(Globals.SESSION_ID, sharedPreferences.getString(Globals.SESSION_KEY, ""));
                            return params;
                        }
                    };
                    queue.add(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Functions.showToast(getActivity(), "Please connect to the Internet");
        }
    }

    private void handleError(VolleyError error) {
        ErrorResponse errorResponse = ErrorHandler.parseAndGetError(error);
        Toast.makeText(getActivity(), errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    private void onSuccess() {
        try {
            Query query = new Query(Integer.parseInt(responseObject.getString(Globals.QID)),
                    Globals.QUERY_DEFAULT_STATUS,
                    sharedPreferences.getString(Globals.NAME, ""),
                    sharedPreferences.getInt(Globals.ID, 0),
                    responseObject.getString(Globals.TO_USER_NAME),
                    responseObject.getInt(Globals.TO_USER_ID),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(requestObject.getString(Globals.QUERY_CREATE_DATE)).getTime(),
                    requestObject.getString(Globals.MESSAGE),
                    requestObject.getString(Globals.VEHICLE_REGISTRATION_NUMBER));
            new QuerySave().execute(query);
            Toast.makeText(getActivity(), "Query Raised Successfully!", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void uploadBitmap(final Bitmap bitmap, String qid) {
        VolleyImageRequest volleyMultipartRequest = new VolleyImageRequest(qid, Request.Method.POST, getResources().getString(R.string.url).concat(APIs.doQueryImage),
                response -> {
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        Toast.makeText(getActivity(), "Image uploaded", Toast.LENGTH_SHORT).show();
                        Functions.saveImage(getActivity(), responseObject.getString(Globals.QID), bitmap);
                        onSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("GotError", "" + error.getMessage());
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        Volley.newRequestQueue(getActivity()).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "value of result code: " + resultCode);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {
                clickedImage.setVisibility(View.VISIBLE);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), result.getUri());
                    int width = bitmap.getWidth(), height = bitmap.getHeight(), size = bitmap.getRowBytes() * bitmap.getHeight();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                    bitmap.copyPixelsToBuffer(byteBuffer);
                    byte[] byteArray = byteBuffer.array();
                    new BitmapTask(byteArray, width, height, bitmap.getConfig().name()).execute();
                    clickedImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Functions.showToast(getActivity(), "Click image again");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                Functions.showToast(getActivity(), "No App available for Cropping");
        }
    }

    private void runTextRecognition(Bitmap x) {
        TextRecognition.getClient().process(InputImage.fromBitmap(x, 0))
                .addOnSuccessListener(texts -> processTextRecognitionResult(texts))
                .addOnFailureListener(e -> {
                    Functions.showToast(getActivity(), "Please enter manually");
                    e.printStackTrace();
                });
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Functions.showToast(getActivity(), "Please enter manually");
            return;
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++)
                    str.append(elements.get(k).getText());
            }
        }
        vehicleNumber.setText(str);
    }

    private class BitmapTask extends AsyncTask<Void, Void, Void> {
        private final Bitmap bitmapTmp;

        public BitmapTask(byte[] byteArray, int width, int height, String conf) {
            Bitmap.Config configBmp = Bitmap.Config.valueOf(conf);
            Bitmap bitmapTmp = Bitmap.createBitmap(width, height, configBmp);
            ByteBuffer buffer = ByteBuffer.wrap(byteArray);
            bitmapTmp.copyPixelsFromBuffer(buffer);
            this.bitmapTmp = bitmapTmp;
        }

        @Override
        protected Void doInBackground(Void... params) {
            runTextRecognition(this.bitmapTmp);
            return null;
        }

    }

    private class QuerySave extends AsyncTask<Query, Void, Void> {

        @Override
        protected Void doInBackground(Query... params) {
            DatabaseClient.getInstance(getContext()).getAppDatabase().parkMeDao().insert(params[0]);
            getActivity().runOnUiThread(() -> Functions.setCurrentFragment(getActivity(), new HomeFragment()));
            return null;
        }
    }

}