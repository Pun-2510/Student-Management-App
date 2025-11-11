package com.example.student_management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private static final int PICK_IMAGE_REQUEST = 101;
    private Toolbar tool_bar;
    private ImageView ivProfile;
    private Uri selectedImageUri;
    private String uid;
    private String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseHelper.getFirestore();

        uid = getIntent().getStringExtra("uid");
        caller = getIntent().getStringExtra("caller");
        ivProfile = findViewById(R.id.iv_profile);
        MaterialButton btnChange = findViewById(R.id.btn_change_photo);

        tool_bar = findViewById(R.id.tool_bar);
        setSupportActionBar(tool_bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tool_bar.setNavigationIcon(R.drawable.logout_svgrepo_com);
        tool_bar.setNavigationOnClickListener(v -> {
            Intent intent;
            if ("ManageStudentActivity".equals(caller)) {
                intent = new Intent(EditProfileActivity.this, ManageStudentActivity.class);
            } else {
                intent = new Intent(EditProfileActivity.this, ManageUserActivity.class);
            }
            intent.putExtra("uid", uid);
            startActivity(intent);
            finish();
        });

        // Fetching current image from firestore
        loadProfilePicture(uid, ivProfile);

        // Handle btnChange triggering event
        btnChange.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivProfile.setImageURI(selectedImageUri);

            // 🔹 Upload ảnh mới lên Storage
            uploadProfilePicture(selectedImageUri);
        }
    }

    private void uploadProfilePicture(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Không có ảnh để tải lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Chỉ định đúng bucket thủ công (vì google-services.json sai domain)
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("user")
                .child(uid)
                .child("profile")
                .child("picture.jpg");

        Log.d("FIREBASE_UPLOAD", "Uploading to: " + storageRef.getPath());
        Log.d("FIREBASE_UPLOAD", "Bucket: " + storageRef.getBucket());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("FIREBASE_UPLOAD", "✅ Upload thành công!");

                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                FirebaseFirestore.getInstance()
                                        .collection("user")
                                        .document(uid)
                                        .collection("profile")
                                        .document("info")
                                        .update("picture", uri.toString())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
                                            Log.d("FIREBASE_UPLOAD", "✅ URL lưu Firestore: " + uri);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FIREBASE_UPLOAD", "❌ Lỗi lưu Firestore: " + e.getMessage());
                                            Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FIREBASE_UPLOAD", "❌ Không lấy được URL ảnh: " + e.getMessage());
                                Toast.makeText(this, "Không lấy được URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_UPLOAD", "❌ Upload thất bại: " + e.getMessage());
                    Toast.makeText(this, "Upload thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfilePicture(String uid, ImageView ivProfile) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("user")
                .document(uid)
                .collection("profile")
                .document("info")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String pictureUrl = document.getString("picture");
                        if (pictureUrl != null && !pictureUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(pictureUrl)
                                    .placeholder(R.drawable.reshot_icon_user_f3n5jxhbeg)
                                    .circleCrop()
                                    .into(ivProfile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không tải được ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE_LOAD", "❌ Lỗi tải ảnh: " + e.getMessage());
                });
    }
}