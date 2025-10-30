package com.example.student_management;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private final Random random = new Random();
    private final Locale vnLocale = new Locale("vi", "VN");

    // Layout elements
    private TextInputEditText edt_gmail, edt_pass;
    private MaterialButton btn_login;
    private TextInputLayout layout_password;
    private int currentIndex = 0;

    // Test user list
    private final String[][] users = {
            {"admin@gmail.com", "123456", "Admin User", "admin"},
            {"manager@gmail.com", "123456", "Manager User", "manager"},
            {"pinkhair@gmail.com", "123456", "Momo", "employee"},
            {"greensky@gmail.com", "123456", "Midori", "employee"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore + Auth
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        edt_gmail = findViewById(R.id.edt_gmail);
        edt_pass = findViewById(R.id.edt_pass);
        btn_login = findViewById(R.id.btn_login);
        layout_password = findViewById(R.id.layout_password);

        edt_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        layout_password.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        // Login button
        btn_login.setOnClickListener(v -> handleLogin());

        // Auto-create test users
        createNextUser();
    }

    private void handleLogin() {
        String email = edt_gmail.getText().toString().trim();
        String password = edt_pass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all required information!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) return;

                        String uid = user.getUid();

                        firestore.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(document -> {
                                    if (!document.exists()) {
                                        Toast.makeText(this, "User info not found!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String role = document.getString("role");
                                    if (role == null) {
                                        Toast.makeText(this, "Role not found!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    saveLoginHistory(uid);

                                    Toast.makeText(this, "Đăng nhập thành công: " + role, Toast.LENGTH_SHORT).show();

                                    Intent intent;
                                    switch (role.toLowerCase()) {
                                        case "admin":
                                            intent = new Intent(this, ManageUserActivity.class);
                                            break;
                                        case "manager":
                                        case "employee":
                                            intent = new Intent(this, ManageStudentActivity.class);
                                            break;
                                        default:
                                            Toast.makeText(this, "Vai trò không hợp lệ: " + role, Toast.LENGTH_SHORT).show();
                                            return;
                                    }

                                    intent.putExtra("uid", uid);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lỗi khi lấy dữ liệu user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNextUser() {
        if (currentIndex >= users.length) {
            Toast.makeText(this, "All test users have been created or already exist!", Toast.LENGTH_LONG).show();
            return;
        }

        String email = users[currentIndex][0];
        String password = users[currentIndex][1];
        String name = users[currentIndex][2];
        String role = users[currentIndex][3];

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), email, name, role);
                        } else {
                            currentIndex++;
                            createNextUser();
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            currentIndex++;
                            createNextUser();
                        } else {
                            currentIndex++;
                            createNextUser();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String name, String role) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("email", email);
        userDoc.put("role", role);
        userDoc.put("status", "Normal");

        firestore.collection("users").document(uid)
                .set(userDoc)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("age", 16 + random.nextInt(3));
                    profile.put("phone_number", String.format(vnLocale, "09%08d", random.nextInt(100000000)));
                    profile.put("picture", "");

                    firestore.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("info")
                            .set(profile)
                            .addOnSuccessListener(profileVoid -> {
                                mAuth.signOut();
                                currentIndex++;
                                createNextUser();
                            });
                })
                .addOnFailureListener(e -> {
                    mAuth.signOut();
                    currentIndex++;
                    createNextUser();
                });
    }

    private void saveLoginHistory(String uid) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", vnLocale);
        String timestamp = sdf.format(new Date());

        Map<String, Object> log = new HashMap<>();
        log.put("login_time", timestamp);
        log.put("device", android.os.Build.MODEL);
        log.put("system", "Android " + android.os.Build.VERSION.RELEASE);

        firestore.collection("users")
                .document(uid)
                .collection("login_history")
                .document(String.valueOf(System.currentTimeMillis()))
                .set(log);
    }
}