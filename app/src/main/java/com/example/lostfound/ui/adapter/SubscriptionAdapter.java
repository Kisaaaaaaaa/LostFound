package com.example.lostfound.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfound.R;
import com.example.lostfound.data.entity.Subscription;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubViewHolder> {

    private List<Subscription> subscriptions = new ArrayList<>();
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Subscription subscription);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        // Using simple_list_item_2 for brevity, though a custom layout with a delete button would be better.
        // Let's create a quick custom layout instead for better UX.
        return new SubViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubViewHolder holder, int position) {
        Subscription sub = subscriptions.get(position);
        holder.tvKeyword.setText(sub.keyword);
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(sub);
        });
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    static class SubViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;
        Button btnDelete;

        public SubViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tvKeyword);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
