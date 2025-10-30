package com.example.student_management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserRecyclerView> {
    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onStatusChange(User user, boolean isChecked);
        void onMenuAction(User user, String action);
    }

    public UserAdapter(Context context, List<User> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserRecyclerView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_view, parent, false);
        return new UserRecyclerView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerView holder, int position) {
        User user = userList.get(position);
        holder.fullname.setText(user.getFullname());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());

        // Get value of key "status" from database to assign to switch_status
        boolean isNormal = "Normal".equalsIgnoreCase(user.getStatus());
        holder.switch_status.setChecked(isNormal);

        // Set Profile picture of that employee
        if (user.getPicture() != null) {
            Glide.with(context)
                    .load(user.getPicture())
                    .placeholder(R.drawable.reshot_icon_user_f3n5jxhbeg)
                    .error(R.drawable.reshot_icon_user_f3n5jxhbeg)
                    .circleCrop()
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.reshot_icon_user_f3n5jxhbeg);
        }

        holder.switch_status.setOnCheckedChangeListener(null);

        // Triggering event of button switch_status
        holder.switch_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onStatusChange(user, isChecked);
            }
        });

        // Triggering event of ImageView ic_menu
        holder.ic_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.ic_menu);
                popup.inflate(R.menu.user_popup_menu);

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_view_login_history) {
                        if (listener != null) listener.onMenuAction(user, "view");
                        return true;

                    } else if (item.getItemId() == R.id.action_edit_user) {
                        if (listener != null) listener.onMenuAction(user, "edit");
                        return true;

                    } else if (item.getItemId() == R.id.action_delete_user) {
                        if (listener != null) listener.onMenuAction(user, "delete");
                        return true;
                    }

                    return false;
                });

                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
