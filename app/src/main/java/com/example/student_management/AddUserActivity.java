package com.example.student_management;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AddUserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextInputEditText edtEmail, edtPassword, edtName, edtPhone, edtAge;
    private AutoCompleteTextView edtRole, edtStatus;
    private MaterialButton btnCreate;
    private final Random random = new Random();
    private final Locale vnLocale = new Locale("vi", "VN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Toolbar setup
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create New User");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 🔹 Firebase setup
        firestore = FirebaseHelper.getFirestore();
        mAuth = FirebaseHelper.getAuth();

        // 🔹 Ánh xạ View
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtName = findViewById(R.id.edt_name);
        edtPhone = findViewById(R.id.edt_phone);
        edtRole = findViewById(R.id.edt_role);
        btnCreate = findViewById(R.id.btn_create_user);
        edtAge = findViewById(R.id.edt_age);
        edtStatus = findViewById(R.id.edt_status);

        // Setup Role dropdown
        String[] roles = getResources().getStringArray(R.array.user_roles);
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        edtRole.setAdapter(roleAdapter);
        edtRole.setOnClickListener(v -> edtRole.showDropDown());

        // Setup Status dropdown
        String[] statuses = getResources().getStringArray(R.array.user_status);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );
        edtStatus.setAdapter(statusAdapter);
        edtStatus.setOnClickListener(v -> edtStatus.showDropDown());

        // 🔹 Xử lý nút Create
        btnCreate.setOnClickListener(v -> createUser());
    }

    private void createUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String age = edtAge.getText().toString().trim();
        String role = edtRole.getText().toString().trim().toLowerCase();
        String status = edtStatus.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, email, name, phone, age, role, status);
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String name, String phone, String age, String role, String status) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("email", email);
        userDoc.put("role", role);
        userDoc.put("status", status);

        firestore.collection("users").document(uid)
                .set(userDoc)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("phone_number", phone);
                    profile.put("age", age);
                    profile.put("picture", "");

                    firestore.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("info")
                            .set(profile)
                            .addOnSuccessListener(done -> {
                                firestore.collection("users")
                                        .document(uid)
                                        .collection("login_history")
                                        .document("init")
                                        .set(new HashMap<>())
                                        .addOnSuccessListener(history -> {
                                            Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to create login history: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
