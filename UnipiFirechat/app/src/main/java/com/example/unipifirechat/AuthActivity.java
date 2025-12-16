package com.example.unipifirechat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 1. Αρχικοποίηση Views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // 2. Αρχικοποίηση Firebase Instances
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 3. Έλεγχος: Αν ο χρήστης είναι ήδη συνδεδεμένος
        if (mAuth.getCurrentUser() != null) {
            goToMainActivity();
            return;
        }

        // 4. Set Listeners
        buttonSignUp.setOnClickListener(v -> signUpUser());
        buttonLogin.setOnClickListener(v -> loginUser());
    }

    private void signUpUser() {
        final String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Παρακαλώ συμπληρώστε όλα τα πεδία.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Ο κωδικός πρέπει να είναι τουλάχιστον 6 χαρακτήρες.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            // Περιμένουμε την αποθήκευση του username να ολοκληρωθεί
                            saveUserToDatabase(user.getUid(), username, email)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(AuthActivity.this, "Εγγραφή επιτυχής και δεδομένα αποθηκεύτηκαν!", Toast.LENGTH_LONG).show();
                                            goToMainActivity();
                                        } else {
                                            // Αν αποτύχει η εγγραφή (π.χ. λόγω Rules), εμφανίζουμε το σφάλμα.
                                            Toast.makeText(AuthActivity.this, "Σφάλμα Αποθήκευσης Username: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            // Προχωράμε, αλλά ο χρήστης δεν θα βρεθεί στην αναζήτηση
                                            goToMainActivity();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(AuthActivity.this, "Σφάλμα Εγγραφής: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Η μέθοδος τώρα επιστρέφει ένα Task, επιτρέποντας στην signUpUser να περιμένει
    private Task<Void> saveUserToDatabase(String uid, String username, String email) {
        User user = new User(uid, username, email);
        return mDatabase.child("users").child(uid).setValue(user);
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Συμπληρώστε Email και Password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Σύνδεση επιτυχής!", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Toast.makeText(AuthActivity.this, "Σφάλμα Σύνδεσης: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}