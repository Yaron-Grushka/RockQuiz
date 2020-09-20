package com.quiz.rockquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.net.URI;

public class ProfileActivity extends BottomNavigationBarManager {

    // Avatar - related variables:
    private static final int CHOOSE_IMAGE = 201;
    private ImageView avatar;
    private Uri uriAvatar;
    private String avatarURL;
    private float density;

    private EditText displayNameEditText;

    // Firebase variables:
    private FirebaseAuth mAuth;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        density = getResources().getDisplayMetrics().density;
        avatar = (ImageView) findViewById(R.id.avatar);

        TextView changeAvatar = (TextView) findViewById(R.id.change_avatar);
        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        displayNameEditText = (EditText) findViewById(R.id.display_name_edit_text);
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        TextView signOut = (TextView) findViewById(R.id.sign_out_button);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(ProfileActivity.this, RegisterActivity.class));
            }
        });

        loadUserInformation();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, RegisterActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Change profile avatar:
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriAvatar = data.getData();
            // Set image...
            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT >= 29) {
                ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(), uriAvatar);
                try {
                    bitmap = ImageDecoder.decodeBitmap(source);
                    avatar.setImageBitmap(bitmap);
                    cropAndSetImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uriAvatar);
                    avatar.setImageBitmap(bitmap);
                    cropAndSetImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            uploadImageToFirebaseStorage();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_profile;
    }

    int getBottomNavigationMenuItemId() {
        return R.id.profile;
    }

    private void loadUserInformation() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl().toString()).into(avatar);
                cropAndSetImage();
            } else {
                avatar.setImageResource(R.drawable.ic_baseline_person_outline_100);
            }

            if (user.getDisplayName() != null) {
                displayNameEditText.setText(user.getDisplayName());
            }
        }
    }

    private void saveUserInformation() {
        String displayName = displayNameEditText.getText().toString();
        if (displayName.isEmpty()) {
            displayNameEditText.setError("Please enter a name");
            displayNameEditText.requestFocus();
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && avatarURL != null) {
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(avatarURL))
                    .build();

            user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose image"), CHOOSE_IMAGE);
    }

    private void cropAndSetImage() {
        android.view.ViewGroup.LayoutParams layoutParams = avatar.getLayoutParams();
        layoutParams.width = Math.round((float) 100 * density);
        layoutParams.height = Math.round((float) 100 * density);
        avatar.setLayoutParams(layoutParams);

    }

    private void uploadImageToFirebaseStorage() {
        StorageReference avatarReference = FirebaseStorage.getInstance().getReference(
                "avatars/" + System.currentTimeMillis() + ".jpg");
        if (uriAvatar != null) {
            avatarReference.putFile(uriAvatar)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            avatarURL = uri.toString();
                                        }
                                    });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                        }
                    });
        }
    }
}