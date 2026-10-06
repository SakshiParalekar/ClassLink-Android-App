package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatroomActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendMessageButton;

    private String classId;
    private String currentUserId;
    private String currentUserEmail;

    private DatabaseReference chatroomRef;
    private DatabaseReference studentsRef;
    private FirebaseAuth mAuth;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // Get classId from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot access chatroom.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User not authenticated or email not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        currentUserEmail = currentUser.getEmail();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app");
        chatroomRef = db.getReference("classes").child(classId).child("chatroom");
        studentsRef = db.getReference("students");

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // Setup RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        chatRecyclerView.setAdapter(messageAdapter);

        // The user's email is already available, so we don't need a separate call to load it.

        // Listen for new messages
        setupChatroomListener();

        // Set up send button click listener
        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(currentUserId, currentUserEmail, messageText, ServerValue.TIMESTAMP);
        chatroomRef.push().setValue(message);
        messageEditText.setText("");
    }

    private void setupChatroomListener() {
        chatroomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(ChatroomActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Message Model Class ---
    public static class Message {
        public String senderId;
        public String senderEmail;
        public String messageText;
        public Object timestamp;

        public Message() {
        }

        public Message(String senderId, String senderEmail, String messageText, Object timestamp) {
            this.senderId = senderId;
            this.senderEmail = senderEmail;
            this.messageText = messageText;
            this.timestamp = timestamp;
        }
    }

    // --- RecyclerView Adapter ---
    private static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private List<Message> messages;
        private String currentUserId;

        private static final int VIEW_TYPE_SENT = 1;
        private static final int VIEW_TYPE_RECEIVED = 2;

        public MessageAdapter(List<Message> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
            if (message.senderId != null && message.senderId.equals(currentUserId)) {
                return VIEW_TYPE_SENT;
            } else {
                return VIEW_TYPE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SENT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
                return new MessageViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
                return new MessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = messages.get(position);
            if (getItemViewType(position) == VIEW_TYPE_SENT) {
                // Sent messages only need the message text.
                holder.messageTextView.setText(message.messageText);
            } else {
                // Received messages need both sender email and message text.
                holder.senderNameTextView.setText(message.senderEmail);
                holder.messageTextView.setText(message.messageText);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView senderNameTextView;
            TextView messageTextView;

            MessageViewHolder(View itemView) {
                super(itemView);
                senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
                messageTextView = itemView.findViewById(R.id.messageText);
            }
        }
    }
}





/*
working
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatroomActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendMessageButton;

    private String classId;
    private String currentUserId;
    // The "Anonymous" value is a temporary placeholder until the user's real name is loaded from the database.
    private String currentUserName = "Anonymous";

    private DatabaseReference chatroomRef;
    private DatabaseReference studentsRef;
    private FirebaseAuth mAuth;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // Get classId from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot access chatroom.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app");
        chatroomRef = db.getReference("classes").child(classId).child("chatroom");
        studentsRef = db.getReference("students");

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // Setup RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        chatRecyclerView.setAdapter(messageAdapter);

        // Load user name
        loadCurrentUserName();

        // Listen for new messages
        setupChatroomListener();

        // Set up send button click listener
        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private void loadCurrentUserName() {
        studentsRef.child(currentUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if name cannot be loaded
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(currentUserId, currentUserName, messageText, ServerValue.TIMESTAMP);
        chatroomRef.push().setValue(message);
        messageEditText.setText("");
    }

    private void setupChatroomListener() {
        chatroomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatroomActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Message Model Class ---
    public static class Message {
        public String senderId;
        public String senderName;
        public String messageText;
        public Object timestamp;

        public Message() {
        }

        public Message(String senderId, String senderName, String messageText, Object timestamp) {
            this.senderId = senderId;
            this.senderName = senderName;
            this.messageText = messageText;
            this.timestamp = timestamp;
        }
    }

    // --- RecyclerView Adapter ---
    private static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private List<Message> messages;
        private String currentUserId;

        private static final int VIEW_TYPE_SENT = 1;
        private static final int VIEW_TYPE_RECEIVED = 2;

        public MessageAdapter(List<Message> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
            if (message.senderId != null && message.senderId.equals(currentUserId)) {
                return VIEW_TYPE_SENT;
            } else {
                return VIEW_TYPE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SENT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
                return new MessageViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
                return new MessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = messages.get(position);
            // We now only set the message text for all messages, regardless of sender.
            holder.messageTextView.setText(message.messageText);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView senderNameTextView;
            TextView messageTextView;

            MessageViewHolder(View itemView) {
                super(itemView);
                senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
                messageTextView = itemView.findViewById(R.id.messageText);
            }
        }
    }
}*/





