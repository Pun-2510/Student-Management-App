package com.example.student_management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private final Random random = new Random();
    private final Locale vnLocale = new Locale("vi", "VN");

    // Attrs of Layout
    private EditText edt_gmail, edt_pass;
    private MaterialButton btn_login;

    private int currentIndex = 0;
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

        edt_gmail = findViewById(R.id.edt_gmail);
        edt_pass = findViewById(R.id.edt_pass);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(v -> {
            String email = edt_gmail.getText().toString().trim();
            String password = edt_pass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter completely required information", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                // Lấy role trong Firestore
                                firestore.collection("users")
                                        .document(uid)
                                        .get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists()) {
                                                String role = document.getString("role");
                                                Toast.makeText(this, "Đăng nhập thành công: " + role, Toast.LENGTH_SHORT).show();

                                                if (role == null) {
                                                    Toast.makeText(this, "Không tìm thấy vai trò người dùng!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

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

                                            } else {
                                                Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this,
                                                "Lỗi khi lấy role: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show());
                            }
                        } else {
                            Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Start creating test users
        createNextUser();
    }

    private void createNextUser() {
        if (currentIndex >= users.length) {
            // Khi tất cả users đã xử lý
            Toast.makeText(this, "All test users have been created or already exist!", Toast.LENGTH_LONG).show();

            // Optional: hiển thị danh sách email nào đã tồn tại
            StringBuilder existed = new StringBuilder();
            for (String[] user : users) {
                existed.append(user[0]).append("\n");
            }
            Toast.makeText(this, "Users: \n" + existed, Toast.LENGTH_LONG).show();
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
                            String uid = firebaseUser.getUid();
                            saveUserToFirestore(uid, email, name, role);
                        } else {
                            currentIndex++;
                            createNextUser();
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            // Email đã tồn tại → chỉ log, không show toast nhiều
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
                    profile.put("status", "normal");
                    profile.put("picture", "");

                    firestore.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("info")
                            .set(profile)
                            .addOnSuccessListener(profileVoid -> {
                                firestore.collection("users")
                                        .document(uid)
                                        .collection("login_history")
                                        .document("init")
                                        .set(new HashMap<>())
                                        .addOnSuccessListener(loginVoid -> {
                                            // Không show toast cho từng user
                                            mAuth.signOut();
                                            currentIndex++;
                                            createNextUser();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    // Không show toast cho từng user
                    mAuth.signOut();
                    currentIndex++;
                    createNextUser();
                });
    }
}
