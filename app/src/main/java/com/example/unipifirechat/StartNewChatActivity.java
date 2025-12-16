package com.example.unipifirechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class StartNewChatActivity extends AppCompatActivity {

    private EditText editTextTargetUsername;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new_chat);

        // Αρχικοποίηση Views (ΧΡΗΣΙΜΟΠΟΙΟΥΜΕ ΤΑ ΣΩΣΤΑ IDS)
        editTextTargetUsername = findViewById(R.id.editTextTargetUsername);
        Button buttonSearchUser = findViewById(R.id.buttonSearchUser);

        // Ασφαλής λήψη του currentUserId
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Σφάλμα: Η συνεδρία έληξε.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        buttonSearchUser.setOnClickListener(v -> searchUserAndStartChat());
    }

    private void searchUserAndStartChat() {
        final String targetUsername = editTextTargetUsername.getText().toString().trim();

        if (targetUsername.isEmpty()) {
            Toast.makeText(this, "Παρακαλώ συμπληρώστε το Username.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Query στη βάση δεδομένων
        Query query = mDatabase.orderByChild("username").equalTo(targetUsername);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User targetUser = userSnapshot.getValue(User.class);
                        if (targetUser != null) {

                            // Αποτροπή συνομιλίας με τον εαυτό μας
                            if (targetUser.getUid().equals(currentUserId)) {
                                Toast.makeText(StartNewChatActivity.this, "Δεν μπορείτε να ξεκινήσετε συνομιλία με τον εαυτό σας.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Μετάβαση στην Οθόνη Συνομιλίας
                            goToChatActivity(targetUser.getUid(), targetUser.getUsername());
                            return;
                        }
                    }
                }
                Toast.makeText(StartNewChatActivity.this, "Ο χρήστης '" + targetUsername + "' δεν βρέθηκε.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StartNewChatActivity.this, "Σφάλμα βάσης δεδομένων: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToChatActivity(String targetUid, String targetUsername) {
        Intent intent = new Intent(StartNewChatActivity.this, ChatActivity.class);
        intent.putExtra("TARGET_UID", targetUid);
        intent.putExtra("TARGET_USERNAME", targetUsername);
        startActivity(intent);
        finish();
    }
}