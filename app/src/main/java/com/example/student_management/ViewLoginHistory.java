package com.example.student_management;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewLoginHistory extends AppCompatActivity {

    private RecyclerView recycler_view;
    private LoginHistoryAdapter adapter;
    private List<LoginHistory> login_history_list;
    private FirebaseFirestore firestore;
    private String uid;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_login_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Receive uid
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID Not Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize firestore
        firestore = FirebaseFirestore.getInstance();

        // Setup for Toolbar
        toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle("Login History");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.outline_arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup for RecyclerView
        recycler_view = findViewById(R.id.login_history_recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        login_history_list = new ArrayList<>();
        adapter = new LoginHistoryAdapter(this, login_history_list);
        recycler_view.setAdapter(adapter);

        // Fetch and Load Data from Firestore
        loadLoginHistory();
    }

    private void loadLoginHistory() {
        firestore.collection("user")
                .document(uid)
                .collection("login_history")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    login_history_list.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String time = doc.getString("login_time");
                        String device = doc.getString("device");
                        String system = doc.getString("system");

                        login_history_list.add(new LoginHistory(time, device, system));
                    }
                    adapter.notifyDataSetChanged();

                    if (login_history_list.isEmpty()) {
                        Toast.makeText(this, "There aren't any login history available yet!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error occurred during fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
