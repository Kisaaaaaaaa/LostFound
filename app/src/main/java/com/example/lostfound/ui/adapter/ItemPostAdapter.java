package com.example.lostfound.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lostfound.R;
import com.example.lostfound.data.entity.ItemPost;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemPostAdapter extends RecyclerView.Adapter<ItemPostAdapter.PostViewHolder> {

    private List<ItemPost> posts = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemPost post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<ItemPost> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        ItemPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvTitle, tvStatus, tvCategory, tvTime;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTime = itemView.findViewById(R.id.tvTime);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(posts.get(position));
                }
            });
        }

        public void bind(ItemPost post) {
            tvTitle.setText(post.title);
            tvCategory.setText(post.category);
            tvTime.setText(dateFormat.format(new Date(post.timestamp)));
            
            // 美化 Tag 逻辑：根据状态设置不同的背景色和文字色
            if (post.status == 0) {
                tvStatus.setText("审核中");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.brand_accent));
                tvStatus.setBackgroundResource(R.drawable.bg_status_tag_pending);
            } else if (post.status == 2) {
                tvStatus.setText("已驳回");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.brand_accent));
                tvStatus.setBackgroundResource(R.drawable.bg_status_tag_error);
            } else if (post.isResolved) {
                tvStatus.setText("已解决");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
                tvStatus.setBackgroundResource(R.drawable.bg_status_tag_default);
            } else {
                tvStatus.setText(post.type == 0 ? "寻物中" : "待领中");
                int color = post.type == 0 ? R.color.brand_primary : R.color.brand_secondary;
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), color));
                tvStatus.setBackgroundResource(post.type == 0 ? R.drawable.bg_status_tag_primary : R.drawable.bg_status_tag_secondary);
            }

            Glide.with(itemView.getContext())
                    .load(post.imageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivImage);
        }
    }
}
