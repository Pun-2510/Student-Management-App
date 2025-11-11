package com.example.student_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditCertificateActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String studentId, certificateId;

    private TextInputEditText edtCertName, edtIssuedBy, edtIssueDate, edtExpiryDate, edtScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_certificate);

        Toolbar toolbar = findViewById(R.id.toolbar_edit_cert);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_arrow_back_24);

        firestore = FirebaseHelper.getFirestore();

        studentId = getIntent().getStringExtra("student_id");
        certificateId = getIntent().getStringExtra("certificate_id");

        if (studentId == null || studentId.isEmpty() || certificateId == null || certificateId.isEmpty()) {
            Toast.makeText(this, "Student or Certificate ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtCertName = findViewById(R.id.edt_cert_name);
        edtIssuedBy = findViewById(R.id.edt_issued_by);
        edtIssueDate = findViewById(R.id.edt_issue_date);
        edtExpiryDate = findViewById(R.id.edt_expiry_date);
        edtScore = findViewById(R.id.edt_score);

        loadCertificateDetails();

        edtIssueDate.setOnClickListener(v -> showDatePicker(edtIssueDate));
        edtExpiryDate.setOnClickListener(v -> showDatePicker(edtExpiryDate));

        Button btnUpdate = findViewById(R.id.btn_update_certificate);
        btnUpdate.setOnClickListener(v -> updateCertificate());
    }

    private void showDatePicker(final TextInputEditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                        editText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
                year, month, day);
        dialog.show();
    }

    private void loadCertificateDetails() {
        firestore.collection("student").document(studentId).collection("certificate").document(certificateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        edtCertName.setText(documentSnapshot.getString("certificate_name"));
                        edtIssuedBy.setText(documentSnapshot.getString("issued_by"));
                        edtIssueDate.setText(documentSnapshot.getString("issue_date"));
                        edtExpiryDate.setText(documentSnapshot.getString("expiry_date"));
                        edtScore.setText(String.valueOf(documentSnapshot.get("score")));
                    }
                });
    }

    private void updateCertificate() {
        String certName = edtCertName.getText().toString().trim();
        String issuedBy = edtIssuedBy.getText().toString().trim();
        String issueDate = edtIssueDate.getText().toString().trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String score = edtScore.getText().toString().trim();

        if (TextUtils.isEmpty(certName) || TextUtils.isEmpty(issuedBy) || TextUtils.isEmpty(score)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> certificate = new HashMap<>();
        certificate.put("certificate_name", certName);
        certificate.put("issued_by", issuedBy);
        certificate.put("issue_date", issueDate);
        certificate.put("expiry_date", expiryDate);
        certificate.put("score", score);

        firestore.collection("student").document(studentId).collection("certificate").document(certificateId)
                .update(certificate)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Certificate updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update certificate", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
