package com.android.parkme.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.parkme.R;
import com.android.parkme.database.Chat;
import com.android.parkme.database.DatabaseClient;
import com.android.parkme.service.MessagingService;
import com.android.parkme.utils.APIs;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.Disposable;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private int userId, toId, qid;
    private RecyclerView mcChatRecyclerView;
    private ChatAdapter mAdapter;
    private SharedPreferences sharedpreferences;
    private Button sendMessageButton;
    private EditText mMessage;
    private Disposable observer;
    private RequestQueue queue = null;
    private List<Chat> chats = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_recycler, container, false);

        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        mcChatRecyclerView = view.findViewById(R.id.chats_recycler_view);
        RelativeLayout rl = view.findViewById(R.id.layout_gchat_chatbox);
        sendMessageButton = view.findViewById(R.id.button_gchat_send);
        mMessage = view.findViewById(R.id.edit_gchat_message);

//
////         is This needed?
//        mcChatRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
//                updateRecyclerView();
//        });

        sharedpreferences = getActivity().getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        userId = sharedpreferences.getInt(Globals.ID, 0);
        qid = getArguments().getInt(Globals.QID);
        toId = getArguments().getInt(Globals.TO_USER_ID);

        if (!Globals.QUERY_DEFAULT_STATUS.equals(getArguments().getString(Globals.STATUS)))
            rl.setVisibility(View.INVISIBLE);
        sendMessageButton.setOnClickListener(view1 -> {
                String message = mMessage.getText().toString().trim();
                if (!message.equals("")) {
                    Chat chat = new Chat(qid, userId, toId, new Date().getTime(), message);
                    mMessage.getText().clear();
                    chats.add(chat);
                    getActivity().runOnUiThread(() -> {
                        updateUI();
                    });
                    pushChat(chat);
                }
        });

        new GetChats().execute();
        mcChatRecyclerView.setItemAnimator(null);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        mcChatRecyclerView.setLayoutManager(manager);
        mcChatRecyclerView.setAdapter(mAdapter);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);

        mAdapter = new ChatAdapter(chats);
        mcChatRecyclerView.scrollToPosition(0);
        initialSetup();
        return view;
    }

    public void updateUI() {
        mAdapter.notifyDataSetChanged();
        mcChatRecyclerView.scrollToPosition(0);
    }
    public void initialSetup() {

        observer = MessagingService.subject.subscribe(chat -> {
            if (((Chat) chat).getQid() == qid) {
                getActivity().runOnUiThread(() -> {
                    chats.add((Chat) chat);
                    updateUI();
                });
            }
        }, e -> handleRxJavaError(e));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (observer != null)
            observer.dispose();
    }

    private void handleError(VolleyError error) {
        Toast.makeText(getActivity(), "Server Down", Toast.LENGTH_SHORT).show();
    }

    private void handleRxJavaError(Throwable e) {
        Log.i(TAG, e.getLocalizedMessage());
    }

    private void pushChat(Chat chat) {
        if (Functions.networkCheck(getContext())) {
            String url = getResources().getString(R.string.url).concat(APIs.sendChat);
            Log.i(TAG, "Send Chat Query " + url);
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put(Globals.QID, qid);
                requestObject.put(Globals.TIME, chat.getTime());
                requestObject.put(Globals.FROM_USER_ID, userId);
                requestObject.put(Globals.TO_USER_ID, toId);
                requestObject.put(Globals.CHAT_MESSAGE, chat.getMsg());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, requestObject, response -> {
                try {
                    boolean status = Boolean.parseBoolean(response.getString(Globals.STATUS));
                    chat.setStatus(status ? 1 : -1);
                    new SaveChat().execute(chat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> this.handleError(error)) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put(Globals.SESSION_ID, sharedpreferences.getString(Globals.SESSION_KEY, ""));
                    return params;
                }
            };
            queue.add(request);
        } else {
            Toast.makeText(getActivity(), "Please connect to the Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Chat> mChats;

        public ChatAdapter(List<Chat> chats) {
            mChats = chats;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == Globals.VIEW_TYPE_SENDER) {
                View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_chat_right, parent, false);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) (itemView.findViewById(R.id.cardView)).getLayoutParams();

                layoutParams.leftMargin = (int) Math.floor(Functions.getScreenWidth() * 0.1);
                itemView.setLayoutParams(layoutParams);
                return new ChatHolderRight(itemView);
            } else {
                View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_chat_left, parent, false);
                ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                layoutParams.width = (int) Math.floor(Functions.getScreenWidth() * 0.7);
                itemView.setLayoutParams(layoutParams);
                return new ChatHolderLeft(itemView);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ChatHolderLeft)
                ((ChatHolderLeft) holder).bind(mChats.get(position));
            else
                ((ChatHolderRight) holder).bind(mChats.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            if (chats.get(position).getFrom() == userId)
                return Globals.VIEW_TYPE_SENDER;
            else
                return Globals.VIEW_TYPE_RECEIVER;
        }

        @Override
        public int getItemCount() {
            return mChats.size();
        }

    }

    private class ChatHolderRight extends RecyclerView.ViewHolder {
        private View v;
        private TextView mMessage;

        public ChatHolderRight(View itemView) {
            super(itemView);
            v = itemView;

            mMessage = itemView.findViewById(R.id.chat_message);
        }

        public void bind(Chat chat) {
            mMessage.setText(chat.getMsg());
        }

    }

    private class ChatHolderLeft extends RecyclerView.ViewHolder {
        private View v;
        private TextView mMessage;

        public ChatHolderLeft(View itemView) {
            super(itemView);
            v = itemView;
            mMessage = itemView.findViewById(R.id.chat_message);
        }

        public void bind(Chat chat) {
            mMessage.setText(chat.getMsg());
        }

    }

    private class GetChats extends AsyncTask<Void, Void, List<Chat>> {

        @Override
        protected List<Chat> doInBackground(Void... params) {
            return DatabaseClient.getInstance(getActivity()).getAppDatabase().parkMeDao().getChatForQueryID(qid);
        }

        @Override
        protected void onPostExecute(List<Chat> chatsRetrieved) {
            super.onPostExecute(chatsRetrieved);
            chats.addAll(chatsRetrieved);
            mAdapter.notifyDataSetChanged();
            updateUI();
        }
    }

    private class SaveChat extends AsyncTask<Chat, Void, Void> {

        @Override
        protected Void doInBackground(Chat... params) {
            DatabaseClient.getInstance(getActivity()).getAppDatabase().parkMeDao().insert(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
        }
    }
}
