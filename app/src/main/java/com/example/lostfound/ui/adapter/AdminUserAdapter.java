package com.example.lostfound.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lostfound.R;
import com.example.lostfound.data.entity.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnUserAdminClickListener listener;

    public interface OnUserAdminClickListener {
        void onBanToggle(User user);
    }

    public AdminUserAdapter(OnUserAdminClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvNickname.setText(user.nickname != null ? user.nickname : user.username);
        
        if (user.isBanned) {
            holder.tvStatus.setText("状态：已封禁");
            holder.tvStatus.setTextColor(0xFFF44336);
            holder.btnBan.setText("解封");
        } else {
            holder.tvStatus.setText("状态：正常");
            holder.tvStatus.setTextColor(0xFF4CAF50);
            holder.btnBan.setText("封禁");
        }

        Glide.with(holder.itemView.getContext())
                .load(user.avatarUri)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(holder.ivAvatar);

        holder.btnBan.setOnClickListener(v -> listener.onBanToggle(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvNickname, tvStatus;
        Button btnBan;

        UserViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvNickname = itemView.findViewById(R.id.tvUserNickname);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
            btnBan = itemView.findViewById(R.id.btnBanUser);
        }
    }
}
