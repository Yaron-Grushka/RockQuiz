package com.quiz.rockquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity implements NavigationHost {

    private EditText emailEditText, userNameEditText, passwordEditText, confirmPasswordEditText;

    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference().child("users");

        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        userNameEditText = (EditText) findViewById(R.id.username_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirm_password_edit_text);

        Button signup = (Button) findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        TextView loginTextView = (TextView) findViewById(R.id.go_to_login);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTo(new LoginFragment(), true);
            }
        });

    }

    private void registerUser() {
        final String email = emailEditText.getText().toString().trim();
        final String username = userNameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter valid email");
            emailEditText.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            userNameEditText.setError("Please choose non-empty username");
            userNameEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            passwordEditText.setError("Passwords do not match");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password minimum length is 6");
            passwordEditText.requestFocus();
            return;
        }

        // Check if username already taken:
        db.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    Toast.makeText(getApplicationContext(), "Username taken",
                            Toast.LENGTH_SHORT).show();
                } else {
                    makeUser(email, password, username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                    // Send auth email:
                    if (firebaseUser != null) {
                        sendVerificationEmail(firebaseUser);
                    }

                    // Set display name:
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();
                    firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Error:" + task.getException()
                                        .getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    // Add user to database:
                    user = new User(email, username, 0);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(firebaseUser.getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Error:" + task.getException()
                                                .getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(getApplicationContext(), "User with this email already exists",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error:" + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
    }
    
    private void makeUser(final String email, final String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            // Send auth email:
                            if (firebaseUser != null) {
                                sendVerificationEmail(firebaseUser);
                            }

                            // Set display name:
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username).build();
                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Error:" + task.getException()
                                                .getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            // Add user to database:
                            user = new User(email, username, 0);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(firebaseUser.getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Error:" + task.getException()
                                                .getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getApplicationContext(), "User with this email already exists",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error:" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Remove text from fields:
                            emailEditText.setText("");
                            passwordEditText.setText("");
                            confirmPasswordEditText.setText("");
                            Toast.makeText(RegisterActivity.this, "Verification email sent",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            navigateTo(new LoginFragment(), true);
                        } else {
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                        }
                    }
                });
    }
    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.registerActivityLayout, fragment);
        if (addToBackstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}