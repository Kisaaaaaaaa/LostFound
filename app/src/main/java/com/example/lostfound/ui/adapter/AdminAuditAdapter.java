package com.example.lostfound.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfound.R;
import com.example.lostfound.data.entity.ItemPost;

import java.util.ArrayList;
import java.util.List;

public class AdminAuditAdapter extends RecyclerView.Adapter<AdminAuditAdapter.AuditViewHolder> {

    private List<ItemPost> posts = new ArrayList<>();
    private OnAuditClickListener listener;

    public interface OnAuditClickListener {
        void onApprove(ItemPost post);
        void onReject(ItemPost post);
        void onItemClick(ItemPost post);
    }

    public AdminAuditAdapter(OnAuditClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<ItemPost> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_audit, parent, false);
        return new AuditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
        ItemPost post = posts.get(position);
        holder.tvTitle.setText(post.title);
        
        // 1. 处理状态文字和颜色
        String statusStr;
        int color;
        if (post.isResolved) {
            statusStr = "已解决";
            color = 0xFF9E9E9E; // 灰色
        } else {
            switch (post.status) {
                case 0: statusStr = "待审核"; color = 0xFFFF9800; break;
                case 1: statusStr = "显示中"; color = 0xFF4CAF50; break;
                case 2: statusStr = "已下架"; color = 0xFFF44336; break;
                default: statusStr = "未知"; color = 0xFF9E9E9E;
            }
        }
        holder.tvStatus.setText(statusStr);
        holder.tvStatus.setTextColor(color);

        // 2. 核心逻辑：根据物品状态动态显示/隐藏按钮
        if (post.isResolved) {
            // 已解决：隐藏所有操作按钮
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            switch (post.status) {
                case 0: // 待审核：显示所有
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);
                    break;
                case 1: // 显示中：只保留“违规下架”
                    holder.btnApprove.setVisibility(View.GONE);
                    holder.btnReject.setVisibility(View.VISIBLE);
                    break;
                case 2: // 已下架：只保留“通过”
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.GONE);
                    break;
            }
        }

        // 3. 绑定点击事件
        holder.btnApprove.setOnClickListener(v -> listener.onApprove(post));
        holder.btnReject.setOnClickListener(v -> listener.onReject(post));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(post));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class AuditViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus;
        Button btnApprove, btnReject;

        AuditViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAuditTitle);
            tvStatus = itemView.findViewById(R.id.tvAuditStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
