package com.example.student_management;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.PopupMenu;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentRecyclerView> {

    private final Context context;
    private final List<Student> studentList;

    private final OnStudentMenuClickListener listener;

    public interface OnStudentMenuClickListener {
        void onEdit(Student student);
        void onDelete(Student student);
        void onViewDetail(Student student);
    }

    public StudentAdapter(Context context, List<Student> studentList, OnStudentMenuClickListener listener) {
        this.context = context;
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentRecyclerView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_view, parent, false);
        return new StudentRecyclerView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentRecyclerView holder, int position) {
        Student student = studentList.get(position);

        holder.txt_fullname.setText(
                HtmlCompat.fromHtml("Fullname: <b>" + student.getFullname() + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        holder.txt_dob.setText(
                HtmlCompat.fromHtml("DOB: <b>" + student.getDob() + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        holder.txt_gender.setText(
                HtmlCompat.fromHtml("Gender: " + student.getGender(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        holder.txt_student_id.setText(
                HtmlCompat.fromHtml("Student ID: <b>" + student.getStudent_id() + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        holder.txt_class_id.setText(
                HtmlCompat.fromHtml("Class: <b>" + student.getClass_id() + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        holder.txt_department.setText(
                HtmlCompat.fromHtml("Department: " + student.getDepartment(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
        holder.txt_intake.setText(
                HtmlCompat.fromHtml("Intake: " + student.getIntake(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        );

        holder.ic_menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.ic_menu);
            popup.inflate(R.menu.student_popup_menu);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit) {
                    if(listener != null) listener.onEdit(student);
                    return true;
                } else if (id == R.id.menu_delete) {
                    if(listener != null) listener.onDelete(student);
                    return true;
                } else if (id == R.id.menu_view_detail) {
                    if(listener != null) listener.onViewDetail(student);
                    return true;
                }
                else {
                    return false;
                }
            });
            popup.show();
        });

    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }
}
