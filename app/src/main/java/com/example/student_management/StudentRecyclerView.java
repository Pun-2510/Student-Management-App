package com.example.student_management;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StudentRecyclerView extends RecyclerView.ViewHolder {

    TextView txt_fullname;
    TextView txt_dob;
    TextView txt_gender;
    TextView txt_student_id;
    TextView txt_class_id;
    TextView txt_department;
    TextView txt_intake;
    ImageView ic_menu;

    public StudentRecyclerView(@NonNull View itemView) {
        super(itemView);

        txt_fullname = itemView.findViewById(R.id.txt_fullname);
        txt_dob = itemView.findViewById(R.id.txt_dob);
        txt_gender = itemView.findViewById(R.id.txt_gender);
        txt_student_id = itemView.findViewById(R.id.txt_student_id);
        txt_class_id = itemView.findViewById(R.id.txt_class_id);
        txt_department = itemView.findViewById(R.id.txt_department);
        txt_intake = itemView.findViewById(R.id.txt_intake);
        ic_menu = itemView.findViewById(R.id.ic_menu);
    }
}
