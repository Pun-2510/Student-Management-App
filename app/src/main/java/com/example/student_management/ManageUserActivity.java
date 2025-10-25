package com.example.student_management;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide; // from github


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class ManageUserActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = getIntent().getStringExtra("uid");

        // Gắn Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Thêm tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Management");
        }

        // Gán icon Logout làm navigation icon (bên trái)
        toolbar.setNavigationIcon(R.drawable.logout_svgrepo_com);

        // Khi bấm icon navigation (Logout)
        toolbar.setNavigationOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    // Quay về MainActivity
                    Intent intent = new Intent(ManageUserActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // kết thúc ManageUserActivity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_user_menu, menu);

        MenuItem profileItem = menu.findItem(R.id.action_profile);
        View actionView = profileItem.getActionView();
        ImageView ivProfileMenu = actionView.findViewById(R.id.iv_profile_menu);

        // Load ảnh từ Firestore
        loadProfilePicture(uid, ivProfileMenu);

        // Optional: click vào avatar mở ProfileActivity
        actionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void loadProfilePicture(String uid, ImageView ivProfile) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
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
                                    .error(R.drawable.reshot_icon_user_f3n5jxhbeg)
                                    .circleCrop()
                                    .into(ivProfile);
                        }
                    }
                });
    }

}