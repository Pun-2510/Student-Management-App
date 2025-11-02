package com.example.student_management;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ManageStudentActivity extends AppCompatActivity {
    private RecyclerView recycler_view;
    private StudentAdapter adapter;
    private List<Student> studentList, filteredList;
    private FirebaseFirestore firestore;
    private TextInputEditText edt_search;
    private AutoCompleteTextView dropdown_filter;
    private String current_search = "Fullname"; // default value
    private final List<String> selectedIntakes = new ArrayList<>();
    private final List<String> selectedDepartments = new ArrayList<>();
    private final List<String> selectedGenders = new ArrayList<>();
    private String selectedDob = "";
    private static final int PICK_FILE_REQUEST = 1;
    private static final int CREATE_FILE_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 1001;


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

        // Setup ToolBar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Students");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_arrow_back_24);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_hamburger));

        // Setup Firestore
        firestore = FirebaseHelper.getFirestore();

        // Setup RecyclerView and Adapter
        recycler_view = findViewById(R.id.student_recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new StudentAdapter(this, filteredList, new StudentAdapter.OnStudentMenuClickListener() {
            @Override
            public void onEdit(Student student) {
                Intent intent = new Intent(ManageStudentActivity.this, EditStudentActivity.class);
                intent.putExtra("student_data", student);
                startActivity(intent);
            }

            @Override
            public void onDelete(Student student) {
                new AlertDialog.Builder(ManageStudentActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete student: " + student.getFullname() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            firestore.collection("student")
                                    .document(student.getStudent_id())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ManageStudentActivity.this,
                                                "✅ Student deleted successfully", Toast.LENGTH_SHORT).show();
                                        // update interface
                                        studentList.remove(student);
                                        filteredList.remove(student);
                                        adapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ManageStudentActivity.this,
                                                "⚠️ Failed to delete student: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onViewDetail(Student student) {

            }

            @Override
            public void onViewCertificate(Student student) {

            }
        });
        recycler_view.setAdapter(adapter);

        edt_search = findViewById(R.id.edt_search);
        dropdown_filter = findViewById(R.id.dropdown_filter);

        setupDropdown();
        loadStudents();

        edt_search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchStudents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_student_menu, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);

            // đổi màu chữ menu item
            SpannableString s = new SpannableString(menuItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            menuItem.setTitle(s);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            return true;
        }
        if (item.getItemId() == R.id.ic_add_student) {
            Intent intent = new Intent(this, AddStudentActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.ic_setting) {
            showFilterDialog(); // Call Filter Dialog
            return true;
        }
        if (item.getItemId() == R.id.ic_import) {
            importStudentsFromFile();
            return true;
        }
        if (item.getItemId() == R.id.ic_export) {
            checkPermissionAndExport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        // Inflate layout
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        // Inflate layout từ XML
        android.view.View view = getLayoutInflater().inflate(R.layout.filter_dialog, null);
        builder.setView(view);

        // Ánh xạ các checkbox
        CheckBox chkIntake2125 = view.findViewById(R.id.check_intake_2021_2025);
        CheckBox chkIntake2327 = view.findViewById(R.id.check_intake_2023_2027);
        CheckBox chkIntake2428 = view.findViewById(R.id.check_intake_2024_2028);

        CheckBox chkDeptIT = view.findViewById(R.id.check_department_it);
        CheckBox chkDeptBiz = view.findViewById(R.id.check_department_business);
        CheckBox chkDeptDesign = view.findViewById(R.id.check_department_design);

        CheckBox chkMale = view.findViewById(R.id.check_gender_male);
        CheckBox chkFemale = view.findViewById(R.id.check_gender_female);

        EditText edtDob = view.findViewById(R.id.edt_dob);
        Button btnApply = view.findViewById(R.id.btn_apply_filter);
        Button btnReset = view.findViewById(R.id.btn_reset_filter);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Gán lại trạng thái cũ
        chkIntake2125.setChecked(selectedIntakes.contains("2021 - 2025"));
        chkIntake2327.setChecked(selectedIntakes.contains("2023 - 2027"));
        chkIntake2428.setChecked(selectedIntakes.contains("2024 - 2028"));

        chkDeptIT.setChecked(selectedDepartments.contains("Information Technology"));
        chkDeptBiz.setChecked(selectedDepartments.contains("Business Administration"));
        chkDeptDesign.setChecked(selectedDepartments.contains("Graphic Design"));

        chkMale.setChecked(selectedGenders.contains("Male"));
        chkFemale.setChecked(selectedGenders.contains("Female"));

        edtDob.setText(selectedDob);

        // Xử lý khi nhấn Apply
        btnApply.setOnClickListener(v -> {
            selectedIntakes.clear();
            selectedDepartments.clear();
            selectedGenders.clear();

            // Intake
            if (chkIntake2125.isChecked()) selectedIntakes.add("2021 - 2025");
            if (chkIntake2327.isChecked()) selectedIntakes.add("2023 - 2027");
            if (chkIntake2428.isChecked()) selectedIntakes.add("2024 - 2028");

            // Department
            if (chkDeptIT.isChecked()) selectedDepartments.add("Information Technology");
            if (chkDeptBiz.isChecked()) selectedDepartments.add("Business Administration");
            if (chkDeptDesign.isChecked()) selectedDepartments.add("Graphic Design");

            // Gender
            if (chkMale.isChecked()) selectedGenders.add("Male");
            if (chkFemale.isChecked()) selectedGenders.add("Female");

            // DOB
            selectedDob = edtDob.getText().toString().trim();

            applyFilters();
            dialog.dismiss();
        });

        // Reset filter
        btnReset.setOnClickListener(v -> {
            // Xóa chọn
            chkIntake2125.setChecked(false);
            chkIntake2327.setChecked(false);
            chkIntake2428.setChecked(false);
            chkDeptIT.setChecked(false);
            chkDeptBiz.setChecked(false);
            chkDeptDesign.setChecked(false);
            chkMale.setChecked(false);
            chkFemale.setChecked(false);
            edtDob.setText("");

            // Hiển thị lại toàn bộ sinh viên
            selectedIntakes.clear();
            selectedDepartments.clear();
            selectedGenders.clear();
            selectedDob = "";
            filteredList.clear();
            filteredList.addAll(studentList);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        filteredList.clear();

        for (Student s : studentList) {
            boolean match = true;

            // Intake
            if (!selectedIntakes.isEmpty() && !selectedIntakes.contains(s.getIntake()))
                match = false;

            // Department
            if (!selectedDepartments.isEmpty() && !selectedDepartments.contains(s.getDepartment()))
                match = false;

            // Gender
            if (!selectedGenders.isEmpty() && !selectedGenders.contains(s.getGender()))
                match = false;

            // DOB (chính xác hoặc bạn có thể mở rộng thành khoảng ngày)
            if (!selectedDob.isEmpty() && !s.getDob().equalsIgnoreCase(selectedDob))
                match = false;

            if (match) filteredList.add(s);
        }

        adapter.notifyDataSetChanged();

        Toast.makeText(this,
                "Filter applied (" + filteredList.size() + " results)",
                Toast.LENGTH_SHORT).show();
    }



    private void setupDropdown() {
        String[] filters = {"Fullname", "Student ID", "Class"};
        ArrayAdapter<String> adapterFilter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, filters);
        dropdown_filter.setAdapter(adapterFilter);
        dropdown_filter.setText(filters[0], false);
        dropdown_filter.setOnItemClickListener((parent, view, position, id) ->
                current_search = filters[position]);
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
                    filteredList.clear();
                    filteredList.addAll(studentList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "⚠️ Failed to load student data: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void searchStudents(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
            filteredList.addAll(studentList);
        } else {
            for (Student s : studentList) {
                switch (current_search) {
                    case "Fullname":
                        if (s.getFullname().toLowerCase().contains(keyword.toLowerCase()))
                            filteredList.add(s);
                        break;
                    case "Student ID":
                        if (s.getStudent_id().toLowerCase().contains(keyword.toLowerCase()))
                            filteredList.add(s);
                        break;
                    case "Class":
                        if (s.getClass_id().toLowerCase().contains(keyword.toLowerCase()))
                            filteredList.add(s);
                        break;
                }
            }
        }
        adapter.notifyDataSetChanged();
    }


    // ==============================
    //       IMPORT STUDENTS
    // ==============================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                readCSVAndUploadToFirestore(fileUri);
            }
        }

        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                writeStudentsToCSVLegacy(uri);
            }
        }
    }

    private void importStudentsFromFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*"); // chỉ file .csv
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_FILE_REQUEST);
    }

    private void readCSVAndUploadToFirestore(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            reader.readLine();

            List<Student> importList = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 7) {
                    String fullname = tokens[0].trim();
                    String dob = tokens[1].trim();
                    String student_id = tokens[2].trim();
                    String class_id = tokens[3].trim();
                    String gender = tokens[4].trim();
                    String department = tokens[5].trim();
                    String intake = tokens[6].trim();

                    Student s = new Student(fullname, dob, student_id, class_id, gender, department, intake);
                    importList.add(s);
                }
            }
            reader.close();

            if (importList.isEmpty()) {
                Toast.makeText(this, "⚠️ File CSV không có dữ liệu hợp lệ!", Toast.LENGTH_LONG).show();
                return;
            }

            // Ghi từng sinh viên một
            int successCount = 0;
            for (Student s : importList) {
                if (s.getStudent_id().isEmpty()) continue; // bỏ qua nếu student_id trống
                firestore.collection("student")
                        .document(s.getStudent_id())
                        .set(s)
                        .addOnSuccessListener(aVoid -> {
                            // Có thể log chi tiết
                            Log.d("ImportStudent", "Imported: " + s.getStudent_id());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ImportStudent", "Failed to import: " + s.getStudent_id(), e);
                        });
                successCount++;
            }

            Toast.makeText(this,
                    "✅ Import xong " + successCount + " sinh viên!",
                    Toast.LENGTH_LONG).show();

            loadStudents(); // cập nhật lại RecyclerView

        } catch (Exception e) {
            Toast.makeText(this, "⚠️ Lỗi khi import: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ImportStudent", "Error reading CSV", e);
        }
    }

    // ==============================
    //       EXPORT STUDENTS
    // ==============================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportStudentsToCSV();
            } else {
                Toast.makeText(this, "Permission denied. Cannot export file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPermissionAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // >= Android 10
            writeStudentsToCSVModern();
        } else {
            // <= Android 9
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                exportStudentsToCSV(); // cách cũ, lưu file trực tiếp
            }
        }
    }

    private void exportStudentsToCSV() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "students_export.csv");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    private void writeStudentsToCSVModern() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, "students_export.csv");
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, "⚠️ Failed to create file!", Toast.LENGTH_SHORT).show();
                return;
            }

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");

            // Thêm BOM để Excel nhận UTF-8
            writer.write('\uFEFF');
            writer.write("Fullname,DOB,StudentID,ClassID,Gender,Department,Intake\n");

            for (Student s : studentList) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                        s.getFullname(),
                        s.getDob(),
                        s.getStudent_id(),
                        s.getClass_id(),
                        s.getGender(),
                        s.getDepartment(),
                        s.getIntake()));
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "✅ File saved to Downloads!", Toast.LENGTH_LONG).show();

            // 👉 Mở file ngay sau khi export xong
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(uri, "text/csv");
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(openIntent, "Open CSV File"));

        } catch (Exception e) {
            Toast.makeText(this, "⚠️ Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeStudentsToCSVLegacy(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write('\uFEFF');
            writer.write("Fullname,DOB,StudentID,ClassID,Gender,Department,Intake\n");
            for (Student s : studentList) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                        s.getFullname(),
                        s.getDob(),
                        s.getStudent_id(),
                        s.getClass_id(),
                        s.getGender(),
                        s.getDepartment(),
                        s.getIntake()));
            }
            writer.flush();
            writer.close();
            Toast.makeText(this, "✅ File exported successfully!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "⚠️ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}