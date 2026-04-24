package com.example.lostfound.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lostfound.data.entity.Subscription;
import com.example.lostfound.databinding.ActivitySubscriptionBinding;
import com.example.lostfound.ui.adapter.SubscriptionAdapter;
import com.example.lostfound.util.SharedPrefsManager;
import com.example.lostfound.viewmodel.SubscriptionViewModel;

public class SubscriptionActivity extends AppCompatActivity {
    private ActivitySubscriptionBinding binding;
    private SubscriptionViewModel viewModel;
    private SubscriptionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SubscriptionViewModel.class);
        int userId = SharedPrefsManager.getInstance(this).getUserId();

        setupRecyclerView();

        viewModel.getSubscriptionsByUser(userId).observe(this, subs -> adapter.setSubscriptions(subs));

        binding.btnAddSub.setOnClickListener(v -> {
            String keyword = binding.etKeyword.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Subscription sub = new Subscription(userId, keyword, System.currentTimeMillis());
                viewModel.insert(sub);
                binding.etKeyword.setText("");
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new SubscriptionAdapter();
        binding.rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubscriptions.setAdapter(adapter);
        adapter.setOnDeleteClickListener(sub -> viewModel.delete(sub));
    }
}
