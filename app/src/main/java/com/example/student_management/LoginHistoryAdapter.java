package com.example.student_management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryRecyclerView> {

    Context context;
    List<LoginHistory> login_history_list;

    public LoginHistoryAdapter(Context context, List<LoginHistory> login_history_list) {
        this.context = context;
        this.login_history_list = login_history_list;
    }

    @NonNull
    @Override
    public LoginHistoryRecyclerView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.login_history_view, parent, false);
        return new LoginHistoryRecyclerView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoginHistoryRecyclerView holder, int position) {
            LoginHistory login_history = login_history_list.get(position);
            holder.txt_login_time.setText(login_history.getDatetime());
            holder.txt_device.setText(login_history.getDevice());
            holder.txt_system.setText(login_history.getSystem());
    }

    @Override
    public int getItemCount() {
        return login_history_list.size();
    }
}
