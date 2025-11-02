package com.example.student_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import java.util.Calendar;

public class AddStudentActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private TextInputEditText edtFullname, edtDob, edtStudentId, edtClassId;
    private AutoCompleteTextView dropdownGender, dropdownDepartment, dropdownIntake;
    private Button btnAddStudent, btnResetStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseHelper.getFirestore();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Add Student");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ
        edtFullname = findViewById(R.id.edt_fullname);
        edtDob = findViewById(R.id.edt_dob);
        edtStudentId = findViewById(R.id.edt_student_id);
        edtClassId = findViewById(R.id.edt_class_id);

        dropdownGender = findViewById(R.id.dropdown_gender);
        dropdownDepartment = findViewById(R.id.dropdown_department);
        dropdownIntake = findViewById(R.id.dropdown_intake);

        btnAddStudent = findViewById(R.id.btn_add_student);
        btnResetStudent = findViewById(R.id.btn_reset_student);

        // Calendar picker cho DOB
        edtDob.setInputType(InputType.TYPE_NULL);
        edtDob.setOnClickListener(v -> showDatePicker());

        // Set Adapter cho dropdowns
        dropdownGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.genders)));

        dropdownDepartment.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.departments)));

        dropdownIntake.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.intakes)));

        // Button Add
        btnAddStudent.setOnClickListener(v -> addStudent());

        // Button Reset
        btnResetStudent.setOnClickListener(v -> resetFields());
    }

    private void showDatePicker(){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                        edtDob.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth+1, selectedYear)),
                year, month, day);
        dialog.show();
    }

    private void addStudent(){
        String fullname = edtFullname.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String classId = edtClassId.getText().toString().trim();
        String gender = dropdownGender.getText().toString().trim();
        String department = dropdownDepartment.getText().toString().trim();
        String intake = dropdownIntake.getText().toString().trim();

        if(fullname.isEmpty() || dob.isEmpty() || studentId.isEmpty()){
            Toast.makeText(this, "⚠️ Please, enter completely required information!", Toast.LENGTH_SHORT).show();
            return;
        }

        saveStudentToFirestore(fullname, dob, studentId, classId, gender, department, intake);
    }

    private void saveStudentToFirestore(String fullname, String dob, String studentId,
                                        String classId, String gender, String department, String intake) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> student = new HashMap<>();
        student.put("fullname", fullname);
        student.put("dob", dob);
        student.put("student_id", studentId);
        student.put("class", classId);
        student.put("gender", gender);
        student.put("department", department);
        student.put("intake", intake);

        // Lưu vào collection "student" với document id là studentId
        db.collection("student").document(studentId)
                .set(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Student saved to Firestore!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Failed to save student: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void resetFields(){
        edtFullname.setText("");
        edtDob.setText("");
        edtStudentId.setText("");
        edtClassId.setText("");
        dropdownGender.setText("");
        dropdownDepartment.setText("");
        dropdownIntake.setText("");
    }
}
