package com.example.lostfound.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.databinding.FragmentLostFoundBinding;
import com.example.lostfound.ui.adapter.ItemPostAdapter;
import com.example.lostfound.ui.post.PostDetailActivity;
import com.example.lostfound.viewmodel.ItemPostViewModel;

import java.util.ArrayList;
import java.util.List;

public class FoundFragment extends Fragment {
    private FragmentLostFoundBinding binding;
    private ItemPostViewModel viewModel;
    private ItemPostAdapter adapter;
    private static final int TYPE_FOUND = 1;

    private List<ItemPost> allPosts = new ArrayList<>();
    private String currentCategory = "全部";
    private String currentKeyword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLostFoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(ItemPostViewModel.class);
        setupRecyclerView();
        setupSpinner();
        setupSearch();
        
        observePosts();
    }

    private void setupRecyclerView() {
        adapter = new ItemPostAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        // 设置点击事件跳转到详情页
        adapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(getContext(), PostDetailActivity.class);
            intent.putExtra("post_id", post.postId);
            startActivity(intent);
        });
    }

    private void setupSpinner() {
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = parent.getItemAtPosition(position).toString();
                filterPosts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim();
                filterPosts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observePosts() {
        viewModel.getPostsByType(TYPE_FOUND).observe(getViewLifecycleOwner(), posts -> {
            allPosts = posts;
            filterPosts();
        });
    }

    private void filterPosts() {
        List<ItemPost> filteredList = new ArrayList<>();
        for (ItemPost post : allPosts) {
            boolean matchesCategory = currentCategory.equals("全部") || post.getCategory().equals(currentCategory);
            boolean matchesKeyword = currentKeyword.isEmpty() || 
                    post.getTitle().toLowerCase().contains(currentKeyword.toLowerCase()) ||
                    post.getDescription().toLowerCase().contains(currentKeyword.toLowerCase());
            
            if (matchesCategory && matchesKeyword) {
                filteredList.add(post);
            }
        }
        adapter.setPosts(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
