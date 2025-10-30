package com.example.student_management;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword, edtName, edtPhone, edtAge;
    private AutoCompleteTextView edtRole, edtStatus;
    private MaterialButton btnSave;
    private FirebaseFirestore firestore;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);

        MaterialToolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle("Edit User");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getColor(android.R.color.white));
        }

        // Xử lý insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        firestore = FirebaseFirestore.getInstance();
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "User Not Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ view
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtName = findViewById(R.id.edt_name);
        edtPhone = findViewById(R.id.edt_phone);
        edtAge = findViewById(R.id.edt_age);
        edtRole = findViewById(R.id.edt_role);
        edtStatus = findViewById(R.id.edt_status);
        btnSave = findViewById(R.id.btn_save_change);

        setupDropdowns();
        setReadOnlyFields();
        loadUserData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void setupDropdowns() {
        // Roles dropdown
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.user_roles)
        );
        edtRole.setAdapter(roleAdapter);
        edtRole.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) edtRole.showDropDown();
        });
        edtRole.setOnClickListener(v -> edtRole.showDropDown());

        // Status dropdown
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.user_status)
        );
        edtStatus.setAdapter(statusAdapter);
        edtStatus.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) edtStatus.showDropDown();
        });
        edtStatus.setOnClickListener(v -> edtStatus.showDropDown());
    }

    private void setReadOnlyFields() {
        edtEmail.setEnabled(false);
        edtPassword.setEnabled(false);
        edtEmail.setAlpha(0.6f);
        edtPassword.setAlpha(0.6f);
        edtPassword.setText("••••••••");
    }

    private void loadUserData() {
        // Lấy dữ liệu từ /users/{uid}
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        edtEmail.setText(document.getString("email"));
                        edtRole.setText(document.getString("role"), false);
                        edtStatus.setText(document.getString("status"), false);
                    } else {
                        Toast.makeText(this, "User Not Found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi tải user: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Lấy dữ liệu từ /users/{uid}/profile/info
        firestore.collection("users")
                .document(uid)
                .collection("profile")
                .document("info")
                .get()
                .addOnSuccessListener(profileDoc -> {
                    if (profileDoc.exists()) {
                        edtName.setText(profileDoc.getString("name"));
                        edtPhone.setText(profileDoc.getString("phone_number"));
                        Object ageObj = profileDoc.get("age");
                        edtAge.setText(ageObj != null ? ageObj.toString() : "");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error occurred during loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveChanges() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String age = edtAge.getText().toString().trim();
        String role = edtRole.getText().toString().trim();
        String status = edtStatus.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || age.isEmpty() || role.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật users/{uid}
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("role", role);
        userUpdate.put("status", status);

        firestore.collection("users")
                .document(uid)
                .update(userUpdate)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật profile
                    Map<String, Object> profileUpdate = new HashMap<>();
                    profileUpdate.put("name", name);
                    profileUpdate.put("phone_number", phone);
                    profileUpdate.put("age", age);

                    firestore.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("info")
                            .update(profileUpdate)
                            .addOnSuccessListener(done -> {
                                Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
