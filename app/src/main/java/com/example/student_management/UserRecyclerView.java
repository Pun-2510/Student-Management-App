package com.example.student_management;

import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

public class UserRecyclerView extends RecyclerView.ViewHolder {
    public ImageView avatar, ic_menu;
    public TextView fullname, email, role;
    public Switch switch_status;

    public UserRecyclerView(@NonNull View itemView) {
        super(itemView);

        View avatar_layout = itemView.findViewById(R.id.include_avatar);
        avatar = avatar_layout.findViewById(R.id.iv_profile_menu);

        fullname = itemView.findViewById(R.id.textView3);
        email = itemView.findViewById(R.id.textView4);
        role = itemView.findViewById(R.id.textView5);
        switch_status = itemView.findViewById(R.id.switch_status);
        ic_menu = itemView.findViewById(R.id.img_menu);
    }
}
