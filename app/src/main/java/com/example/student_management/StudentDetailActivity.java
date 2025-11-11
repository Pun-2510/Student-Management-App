package com.example.student_management;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String studentId;
    private LinearLayout certificatesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.tool_bar_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_arrow_back_24);

        firestore = FirebaseHelper.getFirestore();
        studentId = getIntent().getStringExtra("student_id");
        certificatesLayout = findViewById(R.id.ll_certificates);

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadStudentDetails();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCertificates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.menu_add_certificate) {
            showAddCertificateDialog();
            return true;
        } else if (itemId == R.id.menu_delete_selected) {
            deleteSelectedCertificates();
            return true;
        } else if (itemId == R.id.menu_delete_all) {
            deleteAllCertificates();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker(final EditText editText) {
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

    private void showAddCertificateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_certificate, null);
        builder.setView(dialogView);

        EditText edtCertName = dialogView.findViewById(R.id.edt_cert_name);
        EditText edtIssuedBy = dialogView.findViewById(R.id.edt_issued_by);
        EditText edtIssueDate = dialogView.findViewById(R.id.edt_issue_date);
        EditText edtExpiryDate = dialogView.findViewById(R.id.edt_expiry_date);
        EditText edtScore = dialogView.findViewById(R.id.edt_score);
        Button btnAdd = dialogView.findViewById(R.id.btn_add_certificate);

        edtIssueDate.setOnClickListener(v -> showDatePicker(edtIssueDate));
        edtExpiryDate.setOnClickListener(v -> showDatePicker(edtExpiryDate));

        AlertDialog dialog = builder.create();

        btnAdd.setOnClickListener(v -> {
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

            firestore.collection("student").document(studentId).collection("certificate")
                    .add(certificate)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Certificate added successfully", Toast.LENGTH_SHORT).show();
                        loadCertificates(); // Refresh the list
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add certificate", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void loadStudentDetails() {
        firestore.collection("student").document(studentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ((TextView) findViewById(R.id.tv_fullname)).setText(documentSnapshot.getString("fullname"));
                        ((TextView) findViewById(R.id.tv_student_id)).setText(documentSnapshot.getString("student_id"));
                        ((TextView) findViewById(R.id.tv_dob)).setText(documentSnapshot.getString("dob"));
                        ((TextView) findViewById(R.id.tv_gender)).setText(documentSnapshot.getString("gender"));
                        ((TextView) findViewById(R.id.tv_class)).setText(documentSnapshot.getString("class"));
                        ((TextView) findViewById(R.id.tv_department)).setText(documentSnapshot.getString("department"));
                        ((TextView) findViewById(R.id.tv_intake)).setText(documentSnapshot.getString("intake"));
                    } else {
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load student details", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCertificates() {
        certificatesLayout.removeAllViews(); // Clear before loading
        firestore.collection("student").document(studentId).collection("certificate").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        LinearLayout certificateView = (LinearLayout) inflater.inflate(R.layout.certificate_item, certificatesLayout, false);

                        TextView tvCertName = certificateView.findViewById(R.id.tv_cert_name);
                        TextView tvIssuedBy = certificateView.findViewById(R.id.tv_issued_by);
                        TextView tvScore = certificateView.findViewById(R.id.tv_score);
                        ImageButton btnEdit = certificateView.findViewById(R.id.btn_edit_certificate);
                        ImageButton btnDelete = certificateView.findViewById(R.id.btn_delete_certificate);
                        CheckBox checkBox = certificateView.findViewById(R.id.checkbox_delete);

                        // Store document id in a tag for later use
                        checkBox.setTag(document.getId());

                        tvCertName.setText(document.getString("certificate_name"));
                        tvIssuedBy.setText("Issued by: " + document.getString("issued_by"));
                        tvScore.setText("Score: " + document.get("score").toString());

                        btnEdit.setOnClickListener(v -> {
                            Intent intent = new Intent(StudentDetailActivity.this, EditCertificateActivity.class);
                            intent.putExtra("student_id", studentId);
                            intent.putExtra("certificate_id", document.getId());
                            startActivity(intent);
                        });

                        btnDelete.setOnClickListener(v -> {
                            new AlertDialog.Builder(this)
                                    .setTitle("Delete Certificate")
                                    .setMessage("Are you sure you want to delete this certificate?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        firestore.collection("student").document(studentId)
                                                .collection("certificate").document(document.getId())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "Certificate deleted", Toast.LENGTH_SHORT).show();
                                                    loadCertificates();
                                                });
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        });

                        certificatesLayout.addView(certificateView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load certificates", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSelectedCertificates() {
        List<String> selectedIds = new ArrayList<>();
        for (int i = 0; i < certificatesLayout.getChildCount(); i++) {
            View child = certificatesLayout.getChildAt(i);
            CheckBox checkBox = child.findViewById(R.id.checkbox_delete);
            if (checkBox.isChecked()) {
                selectedIds.add((String) checkBox.getTag());
            }
        }

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "No certificates selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Certificates")
                .setMessage("Are you sure you want to delete the selected certificates?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    WriteBatch batch = firestore.batch();
                    for (String id : selectedIds) {
                        batch.delete(firestore.collection("student").document(studentId).collection("certificate").document(id));
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Selected certificates deleted", Toast.LENGTH_SHORT).show();
                        loadCertificates();
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAllCertificates() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Certificates")
                .setMessage("Are you sure you want to delete all certificates for this student?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firestore.collection("student").document(studentId).collection("certificate").get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                WriteBatch batch = firestore.batch();
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    batch.delete(doc.getReference());
                                }
                                batch.commit().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(StudentDetailActivity.this, "All certificates deleted", Toast.LENGTH_SHORT).show();
                                    loadCertificates();
                                });
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
