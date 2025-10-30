package com.example.student_management;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LoginHistoryRecyclerView extends RecyclerView.ViewHolder {
    TextView txt_login_time, txt_device, txt_system;
    public LoginHistoryRecyclerView(@NonNull View itemView) {
        super(itemView);

        txt_login_time = itemView.findViewById(R.id.txt_login_time);
        txt_device = itemView.findViewById(R.id.txt_device);
        txt_system = itemView.findViewById(R.id.txt_system);
    }
}
