package com.example.lostfound.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lostfound.R;
import com.example.lostfound.data.entity.Message;
import com.example.lostfound.data.entity.User;
import com.example.lostfound.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Message> lastMessages = new ArrayList<>();
    private final int currentUserId;
    private OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(User contact);
    }

    public ContactAdapter(int currentUserId, OnContactClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setContacts(List<Message> lastMessages) {
        this.lastMessages = lastMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Message lastMsg = lastMessages.get(position);
        int contactId = (lastMsg.senderId == currentUserId) ? lastMsg.receiverId : lastMsg.senderId;
        
        if (contactId == 0) {
            // 处理系统消息展示
            holder.tvNickname.setText("系统消息");
            holder.tvLastMessage.setText(lastMsg.content);
            holder.ivAvatar.setImageResource(android.R.drawable.ic_dialog_info);
            
            // 系统消息的未读数逻辑
            new Thread(() -> {
                int unreadCount = AppDatabase.getDatabase(holder.itemView.getContext()).messageDao().getUnreadCountFromSenderSync(currentUserId, 0);
                holder.itemView.post(() -> {
                    if (unreadCount > 0) {
                        holder.tvUnreadCount.setVisibility(View.VISIBLE);
                        holder.tvUnreadCount.setText(String.valueOf(unreadCount));
                    } else {
                        holder.tvUnreadCount.setVisibility(View.GONE);
                    }
                });
            }).start();

            holder.itemView.setOnClickListener(v -> {
                User sysUser = new User("系统消息", "");
                sysUser.id = 0;
                sysUser.nickname = "系统消息";
                listener.onContactClick(sysUser);
            });
            return;
        }

        // 异步加载联系人信息和未读数
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            User contact = db.userDao().getUserById(contactId);
            int unreadCount = db.messageDao().getUnreadCountFromSenderSync(currentUserId, contactId);

            if (contact != null) {
                holder.itemView.post(() -> {
                    holder.tvNickname.setText(contact.nickname != null ? contact.nickname : contact.username);
                    holder.tvLastMessage.setText(lastMsg.content);
                    
                    if (unreadCount > 0) {
                        holder.tvUnreadCount.setVisibility(View.VISIBLE);
                        holder.tvUnreadCount.setText(String.valueOf(unreadCount));
                    } else {
                        holder.tvUnreadCount.setVisibility(View.GONE);
                    }

                    Glide.with(holder.itemView.getContext())
                            .load(contact.avatarUri)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .into(holder.ivAvatar);
                    
                    holder.itemView.setOnClickListener(v -> listener.onContactClick(contact));
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return lastMessages.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvNickname, tvLastMessage, tvUnreadCount;

        ContactViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
