package com.example.unipifirechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewRecipientUsername;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private RecyclerView recyclerViewMessages;

    private String recipientId;
    private String recipientUsername;
    private String chatId;



    // Firebase instances
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Αρχικοποίηση Firebase & Έλεγχος Χρήστη
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (currentUser == null) {
            Toast.makeText(this, "Πρέπει να συνδεθείτε για συνομιλία.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Λήψη δεδομένων από το Intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("TARGET_UID");
        recipientUsername = intent.getStringExtra("TARGET_USERNAME");

        // 3. Αρχικοποίηση Views
        textViewRecipientUsername = findViewById(R.id.textViewRecipientUsername);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        // 4. Set Username στον τίτλο
        textViewRecipientUsername.setText(recipientUsername);

        // 5. ΔΗΜΙΟΥΡΓΙΑ ΜΟΝΑΔΙΚΟΥ CHAT ID (ΜΟΝΟ ΜΙΑ ΦΟΡΑ ΚΑΙ ΣΩΣΤΑ)
        chatId = createChatId(currentUser.getUid(), recipientId);

        // 6. Set Listener στο κουμπί αποστολής
        buttonSend.setOnClickListener(v -> sendMessage());

        // 7. Φόρτωση Ιστορικού Μηνυμάτων
        loadMessages();
    }

    // ** ΒΟΗΘΗΤΙΚΕΣ ΜΕΘΟΔΟΙ **

    // Δημιουργεί ένα σταθερό, μοναδικό ID για το chat (αλφαβητική ταξινόμηση UIDs)
    private String createChatId(String uid1, String uid2) {
        // Ελέγχουμε ποιο UID έρχεται πρώτο αλφαβητικά
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }

    private void sendMessage() {

        String messageText = editTextMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Δεν μπορείτε να στείλετε κενό μήνυμα.", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = currentUser.getUid();
        long timestamp = System.currentTimeMillis();

        Message message = new Message(senderId, recipientId, messageText, timestamp);

        mDatabase.child("chats").child(chatId).push().setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        editTextMessage.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this, "Σφάλμα αποστολής: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadMessages() {
        List<Message> messageList = new ArrayList<>();

        MessageAdapter messageAdapter = new MessageAdapter(ChatActivity.this, messageList);
        recyclerViewMessages.setAdapter(messageAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);

        mDatabase.child("chats").child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }

                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Σφάλμα φόρτωσης: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}