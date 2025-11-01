package com.example.student_management;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageStudentActivity extends AppCompatActivity {
    private RecyclerView recycler_view;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        recycler_view = findViewById(R.id.student_recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList);
        recycler_view.setAdapter(adapter);

        loadStudents();
    }

    private void loadStudents() {
        firestore.collection("student")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    studentList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Student s = doc.toObject(Student.class);
                        if (s != null) studentList.add(s);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "⚠️ Failed to load student data: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}