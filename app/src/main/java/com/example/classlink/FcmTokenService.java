package com.example.classlink;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FcmTokenService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM", "Refreshed token: " + token);
        // Now save the new token to the database
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference database = FirebaseDatabase
                    .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference();
            database.child("users").child(userId).child("fcmToken").setValue(token);
        }
    }
}