package com.example.student_management;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageUserActivity extends AppCompatActivity {

    private String uid;
    private ImageView ivProfileMenu;
    private FirebaseFirestore firestore;
    FloatingActionButton fab_add_user;
    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<User> users = new ArrayList<>();

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

        // FireStorage setup
        firestore = FirebaseFirestore.getInstance();
        uid = getIntent().getStringExtra("uid");

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID Not Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // RecyclerView setup
        rvUsers = findViewById(R.id.recyclerView);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, users, new UserAdapter.OnUserActionListener() {
            @Override
            public void onStatusChange(User user, boolean isChecked) {
                String newStatus = isChecked ? "Normal" : "Locked";
                firestore.collection("users")
                        .document(user.getUid())
                        .update("status", newStatus)
                        .addOnSuccessListener(aVoid -> {
                            user.setStatus(newStatus);
                        });
            }

            @Override
            public void onMenuAction(User user, String action) {
                switch (action) {
                    case "view":
                        Intent view_login_history_intent = new Intent(ManageUserActivity.this, ViewLoginHistory.class);
                        view_login_history_intent.putExtra("uid", user.getUid());
                        startActivity(view_login_history_intent);
                        break;
                    case "edit":
                        Intent editIntent = new Intent(ManageUserActivity.this, EditUserActivity.class);
                        editIntent.putExtra("uid", user.getUid());
                        startActivity(editIntent);
                        break;
                    case "delete":
                        new AlertDialog.Builder(ManageUserActivity.this)
                                .setTitle("Confirm delete")
                                .setMessage("Delete user " + user.getFullname() + "?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    firestore.collection("users").document(user.getUid())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                users.remove(user);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(ManageUserActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                }
            }
        });

        rvUsers.setAdapter(adapter);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("User Management");

        toolbar.setNavigationIcon(R.drawable.logout_svgrepo_com);
        toolbar.setNavigationOnClickListener(v -> showLogoutDialog());

        // Triggering Event for fad_add_user
        fab_add_user = findViewById(R.id.fab_add_user);
        fab_add_user.setOnClickListener(v -> {
            Intent intent = new Intent(ManageUserActivity.this, AddUserActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        firestore.collection("users").get().addOnSuccessListener(query -> {
            users.clear();
            AtomicInteger loadedCount = new AtomicInteger(0);
            int totalDocs = query.size();

            for (QueryDocumentSnapshot doc : query) {
                String userId = doc.getId();
                String email = doc.getString("email");
                String role = doc.getString("role");
                String status = doc.getString("status");

                DocumentReference profileRef = firestore.collection("users")
                        .document(userId)
                        .collection("profile")
                        .document("info");

                profileRef.get().addOnSuccessListener(profileDoc -> {
                    String fullname = profileDoc.getString("name");
                    String picture = profileDoc.getString("picture");
                    if (picture == null || picture.isEmpty()) picture = null;

                    users.add(new User(userId, fullname, email, role, status, picture));

                    if (loadedCount.incrementAndGet() == totalDocs) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadProfilePicture(String uid, ImageView ivProfile) {
        firestore.collection("users").document(uid)
                .collection("profile").document("info")
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
                        } else {
                            ivProfile.setImageResource(R.drawable.reshot_icon_user_f3n5jxhbeg);
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_user_menu, menu);
        MenuItem profileItem = menu.findItem(R.id.action_profile);
        View actionView = profileItem.getActionView();
        ivProfileMenu = actionView.findViewById(R.id.iv_profile_menu);

        loadProfilePicture(uid, ivProfileMenu);

        actionView.setOnClickListener(v -> {
            Intent intent = new Intent(ManageUserActivity.this, EditProfileActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            loadProfilePicture(uid, ivProfileMenu);
        }
    }
}
