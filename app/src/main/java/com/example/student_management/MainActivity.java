package com.example.student_management;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private final String[][] user = {
            {"admin@gmail.com", "123456", "Admin User", "Admin"},
            {"manager@gmail.com", "123456", "Manager User", "Manager"},
            {"pinkhair@gmail.com", "123456", "Momo", "Employee"},
            {"greensky@gmail.com", "123456", "Midori", "Employee"},
    };

    private final String[][] students = {
            {"Sorasaki Hina", "2005-02-19", "Female", "SV2025001", "CTK46A", "Business Administration", "2021 - 2025"},
            {"Kyouyama Kazusa", "2007-08-05", "Female", "SV2025002", "CTK46B", "Graphic Design", "2023 - 2027"},
            {"Uzawa Reisa", "2007-05-31", "Female", "SV2025003", "CTK46C", "Software Engineering", "2023 - 2027"}
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
        firestore = FirebaseHelper.getFirestore();
        mAuth = FirebaseHelper.getAuth();

        // Check Firebase connection status
        //checkFirebaseConnection();

        // Initialize UI
        edt_gmail = findViewById(R.id.edt_gmail);
        edt_pass = findViewById(R.id.edt_pass);
        btn_login = findViewById(R.id.btn_login);
        layout_password = findViewById(R.id.layout_password);

        edt_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        layout_password.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        // Login button
        btn_login.setOnClickListener(v -> handleLogin());

        // Auto-create sample of users
        createSampleUsers();
        // Auto-create sample of students
        createSampleStudents();
    }

//    private void checkFirebaseConnection() {
//        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
//        connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Boolean connected = snapshot.getValue(Boolean.class);
//                if (connected != null && connected) {
//                    Toast.makeText(MainActivity.this, "✅ Connected to Firebase", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "❌ Disconnected from Firebase", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MainActivity.this, "⚠️ Connection listener was cancelled", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void handleLogin() {
        String email = edt_gmail.getText().toString().trim();
        String password = edt_pass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter all required information!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) return;

                        String uid = user.getUid();

                        firestore.collection("user")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(document -> {
                                    if (!document.exists()) {
                                        Toast.makeText(this, "⚠️ User info not found!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String role = document.getString("role");
                                    if (role == null) {
                                        Toast.makeText(this, "⚠️ Role not found!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    saveLoginHistory(uid);

                                    Toast.makeText(this, "✅ Login successfully: " + role, Toast.LENGTH_SHORT).show();

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
                                            Toast.makeText(this, "⚠️ Invalid Role: " + role, Toast.LENGTH_SHORT).show();
                                            return;
                                    }

                                    intent.putExtra("uid", uid);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "⚠️ Error occurred during fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "⚠️ Wrong email or password, Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createSampleUsers() {
        if (currentIndex >= user.length) {
            Toast.makeText(this, "✅ All test users have been created or already exist!", Toast.LENGTH_LONG).show();
            return;
        }

        String email = user[currentIndex][0];
        String password = user[currentIndex][1];
        String name = user[currentIndex][2];
        String role = user[currentIndex][3];

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), email, name, role);
                        } else {
                            currentIndex++;
                            createSampleUsers();
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            currentIndex++;
                            createSampleUsers();
                        } else {
                            currentIndex++;
                            createSampleUsers();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String name, String role) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("email", email);
        userDoc.put("role", role);
        userDoc.put("status", "Normal");

        firestore.collection("user").document(uid)
                .set(userDoc)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("age", 16 + random.nextInt(3));
                    profile.put("phone_number", String.format(vnLocale, "09%08d", random.nextInt(100000000)));
                    profile.put("picture", "");

                    firestore.collection("user")
                            .document(uid)
                            .collection("profile")
                            .document("info")
                            .set(profile)
                            .addOnSuccessListener(profileVoid -> {
                                currentIndex++;
                                createSampleUsers();
                            });
                })
                .addOnFailureListener(e -> {
                    currentIndex++;
                    createSampleUsers();
                });
    }

    private void saveLoginHistory(String uid) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", vnLocale);
        String timestamp = sdf.format(new Date());

        Map<String, Object> log = new HashMap<>();
        log.put("login_time", timestamp);
        log.put("device", android.os.Build.MODEL);
        log.put("system", "Android " + android.os.Build.VERSION.RELEASE);

        firestore.collection("user")
                .document(uid)
                .collection("login_history")
                .document(String.valueOf(System.currentTimeMillis()))
                .set(log);
    }

    private void createSampleStudents() {
        final int totalStudents = students.length;
        final int[] completed = {0}; // Counter for completed insertions

        for (String[] data : students) {
            String studentId = data[3]; // student_id

            firestore.collection("student")
                    .document(studentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            completed[0]++;
                            if (completed[0] == totalStudents) {
                                Toast.makeText(this,
                                        "✅ Sample data checked successfully!",
                                        Toast.LENGTH_LONG).show();
                            }
                            return;
                        }

                        Map<String, Object> student = new HashMap<>();
                        student.put("fullname", data[0]);
                        student.put("dob", data[1]);
                        student.put("gender", data[2]);
                        student.put("student_id", data[3]);
                        student.put("class", data[4]);
                        student.put("department", data[5]);
                        student.put("intake", data[6]);

                        firestore.collection("student")
                                .document(studentId)
                                .set(student)
                                .addOnSuccessListener(aVoid -> {
                                    Map<String, Object> certificate1 = new HashMap<>();
                                    certificate1.put("certificate_name", "IELTS");
                                    certificate1.put("issued_by", "British Council");
                                    certificate1.put("issue_date", "2023-08-15");
                                    double[] ieltsScores = {6.0, 6.5, 7.0, 7.5, 8.0};
                                    certificate1.put("score", ieltsScores[random.nextInt(ieltsScores.length)]);
                                    certificate1.put("expiry_date", "2025-08-15");

                                    Map<String, Object> certificate2 = new HashMap<>();
                                    certificate2.put("certificate_name", "TOEIC");
                                    certificate2.put("issued_by", "ETS");
                                    certificate2.put("issue_date", "2024-01-05");
                                    certificate2.put("score", 700 + random.nextInt(251));
                                    certificate2.put("expiry_date", "Forever");

                                    firestore.collection("student")
                                            .document(studentId)
                                            .collection("certificate")
                                            .document("cert1")
                                            .set(certificate1)
                                            .addOnSuccessListener(v -> firestore.collection("student")
                                                    .document(studentId)
                                                    .collection("certificate")
                                                    .document("cert2")
                                                    .set(certificate2)
                                                    .addOnSuccessListener(v2 -> {
                                                        completed[0]++;
                                                        if (completed[0] == totalStudents) {
                                                            Toast.makeText(this,
                                                                    "✅ Sample data created successfully for " + totalStudents + " students!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        completed[0]++;
                                                        if (completed[0] == totalStudents) {
                                                            Toast.makeText(this,
                                                                    "⚠️ Completed with some errors!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    })
                                            )
                                            .addOnFailureListener(e -> {
                                                completed[0]++;
                                                if (completed[0] == totalStudents) {
                                                    Toast.makeText(this,
                                                            "⚠️ Completed with some errors!",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    if (completed[0] == totalStudents) {
                                        Toast.makeText(this,
                                                "⚠️ Completed with some errors!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        completed[0]++;
                        if (completed[0] == totalStudents) {
                            Toast.makeText(this,
                                    "⚠️ Completed with some errors!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}