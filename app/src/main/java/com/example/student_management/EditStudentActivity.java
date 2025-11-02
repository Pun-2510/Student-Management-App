package com.example.student_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class EditStudentActivity extends AppCompatActivity {

    private TextInputEditText edt_fullname, edt_dob, edt_student_id, edt_class_id;
    private AutoCompleteTextView dropdown_gender, dropdown_department, dropdown_intake;
    private Button btn_save_change, btn_reset;
    private Student originalStudent;
    private FirebaseFirestore firestore;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Modify Student Information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_arrow_back_24);

        edt_fullname = findViewById(R.id.edt_fullname);
        edt_dob = findViewById(R.id.edt_dob);
        edt_student_id = findViewById(R.id.edt_student_id);
        edt_class_id = findViewById(R.id.edt_class_id);
        dropdown_gender = findViewById(R.id.dropdown_gender);
        dropdown_department = findViewById(R.id.dropdown_department);
        dropdown_intake = findViewById(R.id.dropdown_intake);
        btn_save_change = findViewById(R.id.btn_save_change);
        btn_reset = findViewById(R.id.btn_reset);

        firestore = FirebaseFirestore.getInstance();

        originalStudent = (Student) getIntent().getSerializableExtra("student_data");

        if (originalStudent != null) {
            setFields(originalStudent);
        }

        // Calendar picker cho DOB
        edt_dob.setInputType(InputType.TYPE_NULL);
        edt_dob.setOnClickListener(v -> showDatePicker());

        // Set Adapter cho dropdowns
        dropdown_gender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.genders)));

        dropdown_department.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.departments)));

        dropdown_intake.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.intakes)));

        btn_save_change.setOnClickListener(v -> {
            if (originalStudent == null) return;

            Student updatedStudent = new Student(
                    edt_fullname.getText().toString().trim(),
                    edt_dob.getText().toString().trim(),
                    edt_student_id.getText().toString().trim(),
                    edt_class_id.getText().toString().trim(),
                    dropdown_gender.getText().toString().trim(),
                    dropdown_department.getText().toString().trim(),
                    dropdown_intake.getText().toString().trim()
            );

            firestore.collection("student")
                    .document(updatedStudent.getStudent_id())
                    .set(updatedStudent)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "✅ Student updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "⚠️ Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

            finish();
        });

        btn_reset.setOnClickListener(v -> {
            if (originalStudent != null) {
                setFields(originalStudent);
                Toast.makeText(this, "Fields reset to original values", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFields(Student student) {
        edt_fullname.setText(student.getFullname());
        edt_dob.setText(student.getDob());
        edt_student_id.setText(student.getStudent_id());
        edt_class_id.setText(student.getClass_id());
        dropdown_gender.setText(student.getGender(), false);
        dropdown_department.setText(student.getDepartment(), false);
        dropdown_intake.setText(student.getIntake(), false);
    }

    private void showDatePicker(){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                        edt_dob.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth+1, selectedYear)),
                year, month, day);
        dialog.show();
    }
}