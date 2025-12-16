// MainActivity.java

package com.example.unipifirechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private List<User> userList; // Λίστα όλων των χρηστών (για να βρούμε τα usernames)

    // Εδώ θα αποθηκεύσουμε τους χρήστες με τους οποίους έχουμε συνομιλήσει.
    private List<String> usersChattedWith;

    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Αρχικοποίηση Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Έλεγχος χρήστη
        if (currentUser == null) {
            goToAuthActivity();
            return;
        }

        // 3. Αρχικοποίηση Views
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        recyclerViewChats.setHasFixedSize(true);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabNewChat);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartNewChatActivity.class);
            startActivity(intent);
        });

        // 4. Αρχικοποίηση Λιστών
        userList = new ArrayList<>();
        usersChattedWith = new ArrayList<>();

        // 5. Φόρτωση Συνομιλιών
        loadChatUsers();
    }

    // ** ΜΕΘΟΔΟΙ **

    private void loadChatUsers() {
        // Διαβάζουμε το node 'chats' για να βρούμε με ποιους χρήστες έχουμε μιλήσει.
        mDatabase.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersChattedWith.clear();

                // Περνάμε από όλα τα chat IDs
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey(); // Το κλειδί είναι π.χ. "uid1_uid2"

                    // Παίρνουμε τα UIDs από το chatId
                    String[] uids = chatId.split("_");
                    String user1 = uids[0];
                    String user2 = uids[1];

                    // Προσθέτουμε τον 'άλλο' χρήστη στη λίστα
                    if (user1.equals(currentUser.getUid())) {
                        usersChattedWith.add(user2);
                    } else if (user2.equals(currentUser.getUid())) {
                        usersChattedWith.add(user1);
                    }
                }

                // Αφού βρούμε όλους τους συνομιλητές, φέρνουμε τα usernames τους
                readUsersForChatList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Σφάλμα φόρτωσης συνομιλιών: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readUsersForChatList() {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    // Ελέγχουμε αν αυτός ο χρήστης βρίσκεται στη λίστα των συνομιλητών μας
                    if (user != null && usersChattedWith.contains(user.getUid())) {
                        userList.add(user);
                    }
                }


                // Επειδή δεν έχουμε ακόμα ChatListAdapter, απλά δείχνουμε ένα Toast
                if (!userList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Βρέθηκαν " + userList.size() + " συνομιλητές!", Toast.LENGTH_SHORT).show();
                }
                // *** ΕΠΟΜΕΝΟ ΒΗΜΑ: Κλήση του Adapter ΕΔΩ ***
                ChatListAdapter chatListAdapter = new ChatListAdapter(MainActivity.this, userList);
                recyclerViewChats.setAdapter(chatListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void goToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}