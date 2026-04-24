package com.example.lostfound.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.lostfound.R;
import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.User;
import com.example.lostfound.databinding.FragmentProfileBinding;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.ui.AdminActivity;
import com.example.lostfound.ui.SubscriptionActivity;
import com.example.lostfound.ui.adapter.ItemPostAdapter;
import com.example.lostfound.ui.auth.LoginActivity;
import com.example.lostfound.ui.post.PostActivity;
import com.example.lostfound.util.ImageUtils;
import com.example.lostfound.util.SharedPrefsManager;
import com.example.lostfound.viewmodel.ItemPostViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ItemPostViewModel viewModel;
    private ItemPostAdapter adapter;
    private User currentUser;
    private int userId;
    private List<ItemPost> allMyPosts = new ArrayList<>();
    private int currentFilterTab = 0; // 0:全部, 1:审核中, 2:已发布, 3:已解决, 4:已驳回

    private final ActivityResultLauncher<String> selectAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String savedUri = ImageUtils.saveImageToInternalStorage(requireContext(), uri);
                    if (savedUri != null) {
                        updateUserAvatar(savedUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ItemPostViewModel.class);
        userId = SharedPrefsManager.getInstance(getContext()).getUserId();

        setupRecyclerView();
        setupTabs();

        viewModel.getPostsByPublisherId(userId).observe(getViewLifecycleOwner(), posts -> {
            allMyPosts = posts;
            filterMyPosts(currentFilterTab);
        });

        binding.ivAvatar.setOnClickListener(v -> checkPermissionAndSelectAvatar());
        binding.layoutName.setOnClickListener(v -> showEditNicknameDialog());

        binding.btnLogout.setOnClickListener(v -> {
            SharedPrefsManager.getInstance(getContext()).clear();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        binding.btnSubscription.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SubscriptionActivity.class));
        });

        binding.btnAdmin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminActivity.class));
        });
        
        loadUserData();
        observePendingAudits();
    }

    private void setupTabs() {
        binding.tabLayoutMyPosts.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilterTab = tab.getPosition();
                filterMyPosts(currentFilterTab);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterMyPosts(int position) {
        if (allMyPosts == null) return;
        
        List<ItemPost> filteredList;
        switch (position) {
            case 1: // 审核中
                filteredList = allMyPosts.stream().filter(p -> p.status == 0).collect(Collectors.toList());
                break;
            case 2: // 已发布 (status=1 且未解决)
                filteredList = allMyPosts.stream().filter(p -> p.status == 1 && !p.isResolved).collect(Collectors.toList());
                break;
            case 3: // 已解决
                filteredList = allMyPosts.stream().filter(p -> p.isResolved).collect(Collectors.toList());
                break;
            case 4: // 已驳回
                filteredList = allMyPosts.stream().filter(p -> p.status == 2).collect(Collectors.toList());
                break;
            default: // 全部
                filteredList = allMyPosts;
                break;
        }
        adapter.setPosts(filteredList);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        new Thread(() -> {
            currentUser = AppDatabase.getDatabase(requireContext()).userDao().getUserById(userId);
            if (currentUser != null) {
                requireActivity().runOnUiThread(() -> {
                    binding.tvNickname.setText(currentUser.nickname != null ? currentUser.nickname : currentUser.username);
                    if (currentUser.avatarUri != null) {
                        Glide.with(this).load(currentUser.avatarUri).placeholder(android.R.drawable.sym_def_app_icon).into(binding.ivAvatar);
                    }
                    
                    if ("admin".equals(currentUser.role)) {
                        binding.btnAdmin.setVisibility(View.VISIBLE);
                    } else {
                        binding.btnAdmin.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void observePendingAudits() {
        AppDatabase.getDatabase(requireContext()).itemPostDao().getPendingAuditCount().observe(getViewLifecycleOwner(), count -> {
            if ("admin".equals(currentUser != null ? currentUser.role : "")) {
                if (count != null && count > 0) {
                    binding.btnAdmin.setText("管理后台 (" + count + ")");
                    binding.btnAdmin.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_accent));
                } else {
                    binding.btnAdmin.setText("管理后台");
                    binding.btnAdmin.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_primary));
                }
            }
        });
    }

    private void checkPermissionAndSelectAvatar() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? 
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            selectAvatarLauncher.launch("image/*");
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> { if (isGranted) selectAvatarLauncher.launch("image/*"); }
    );

    private void showEditNicknameDialog() {
        EditText etNickname = new EditText(requireContext());
        etNickname.setText(binding.tvNickname.getText());
        etNickname.setSelection(etNickname.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("修改昵称")
                .setView(etNickname)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newNickname = etNickname.getText().toString().trim();
                    if (!newNickname.isEmpty()) {
                        updateUserNickname(newNickname);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateUserAvatar(String uri) {
        new Thread(() -> {
            if (currentUser != null) {
                currentUser.avatarUri = uri;
                AppDatabase.getDatabase(requireContext()).userDao().update(currentUser);
                requireActivity().runOnUiThread(() -> Glide.with(this).load(uri).into(binding.ivAvatar));
            }
        }).start();
    }

    private void updateUserNickname(String nickname) {
        new Thread(() -> {
            if (currentUser != null) {
                currentUser.nickname = nickname;
                AppDatabase.getDatabase(requireContext()).userDao().update(currentUser);
                requireActivity().runOnUiThread(() -> binding.tvNickname.setText(nickname));
            }
        }).start();
    }

    private void setupRecyclerView() {
        adapter = new ItemPostAdapter();
        binding.rvMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMyPosts.setAdapter(adapter);
        adapter.setOnItemClickListener(post -> {
            showManageDialog(post);
        });
    }

    private void showManageDialog(ItemPost post) {
        String[] options = {"编辑信息", post.isResolved ? "标记为未解决" : "标记为已解决", "删除信息", "取消"};
        new AlertDialog.Builder(requireContext())
                .setTitle("管理我的发布")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(getContext(), PostActivity.class);
                        intent.putExtra("edit_post_id", post.postId);
                        startActivity(intent);
                    } else if (which == 1) {
                        viewModel.updateResolvedState(post.postId, !post.isResolved);
                    } else if (which == 2) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("确认删除")
                                .setPositiveButton("确定", (d, w) -> viewModel.delete(post))
                                .setNegativeButton("取消", null).show();
                    }
                }).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
