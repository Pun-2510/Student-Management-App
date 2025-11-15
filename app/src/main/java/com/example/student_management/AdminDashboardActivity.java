package com.example.student_management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminDashboardActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = getIntent().getStringExtra("uid");

        Button btnManageUsers = findViewById(R.id.btn_manage_users);
        Button btnManageStudents = findViewById(R.id.btn_manage_students);

        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageUserActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });

        btnManageStudents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageStudentActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
    }
}