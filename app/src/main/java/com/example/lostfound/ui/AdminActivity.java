package com.example.lostfound.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.data.entity.Message;
import com.example.lostfound.data.entity.Subscription;
import com.example.lostfound.databinding.ActivityAdminBinding;
import com.example.lostfound.ui.adapter.AdminAuditAdapter;
import com.example.lostfound.ui.adapter.AdminUserAdapter;
import com.example.lostfound.ui.post.PostDetailActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.stream.Collectors;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";
    private ActivityAdminBinding binding;
    private AdminAuditAdapter auditAdapter;
    private AdminUserAdapter userAdapter;
    private AppDatabase db;
    
    // 0:审核中, 1:显示中, 2:已解决, 3:已下架
    private int currentTabPos = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            binding = ActivityAdminBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("管理后台");
            }

            db = AppDatabase.getDatabase(this);
            
            setupDashboard();
            setupRecyclerViews();
            setupTabs();

            // 默认加载第一个Tab
            loadDataByTab(0);
            
        } catch (Exception e) {
            Log.e(TAG, "AdminActivity 初始化失败: " + e.getMessage());
            finish();
        }
    }

    private void setupDashboard() {
        db.itemPostDao().getTotalLostCount().observe(this, count -> 
                binding.tvTotalLost.setText(String.valueOf(count != null ? count : 0)));
        db.itemPostDao().getTotalFoundCount().observe(this, count -> 
                binding.tvTotalFound.setText(String.valueOf(count != null ? count : 0)));
        db.itemPostDao().getTotalResolvedCount().observe(this, count -> 
                binding.tvTotalResolved.setText(String.valueOf(count != null ? count : 0)));
    }

    private void setupRecyclerViews() {
        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(this));
        
        auditAdapter = new AdminAuditAdapter(new AdminAuditAdapter.OnAuditClickListener() {
            @Override
            public void onApprove(ItemPost post) {
                new Thread(() -> {
                    db.itemPostDao().updatePostStatus(post.postId, 1);
                    checkSubscriptionsForPost(post);
                    runOnUiThread(() -> {
                        Toast.makeText(AdminActivity.this, "已通过", Toast.LENGTH_SHORT).show();
                        loadDataByTab(currentTabPos); 
                    });
                }).start();
            }

            @Override
            public void onReject(ItemPost post) {
                new Thread(() -> {
                    db.itemPostDao().updatePostStatus(post.postId, 2);
                    runOnUiThread(() -> {
                        Toast.makeText(AdminActivity.this, "已下架", Toast.LENGTH_SHORT).show();
                        loadDataByTab(currentTabPos);
                    });
                }).start();
            }

            @Override
            public void onItemClick(ItemPost post) {
                Intent intent = new Intent(AdminActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                startActivity(intent);
            }
        });

        userAdapter = new AdminUserAdapter(user -> {
            new Thread(() -> {
                boolean newStatus = !user.isBanned;
                db.userDao().updateUserBanStatus(user.id, newStatus);
                runOnUiThread(() -> {
                    Toast.makeText(AdminActivity.this, newStatus ? "已封禁" : "已解封", Toast.LENGTH_SHORT).show();
                    loadDataByTab(currentTabPos);
                });
            }).start();
        });
    }

    private void checkSubscriptionsForPost(ItemPost post) {
        List<Subscription> allSubs = db.subscriptionDao().getAllSubscriptionsSync();
        for (Subscription sub : allSubs) {
            if (sub.userId == post.publisherId) continue;
            String keyword = sub.keyword.toLowerCase();
            if (post.title.toLowerCase().contains(keyword) || post.description.toLowerCase().contains(keyword)) {
                String content = "订阅提醒：匹配到新发布的物品 [" + post.title + "]，请前往大厅查看。";
                Message msg = new Message(0, sub.userId, content, System.currentTimeMillis());
                db.messageDao().insert(msg);
            }
        }
    }

    private void setupTabs() {
        binding.tabLayout.removeAllTabs();
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("审核中"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("显示中"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("已解决"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("已下架"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("用户管理"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPos = tab.getPosition();
                loadDataByTab(currentTabPos);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadDataByTab(int position) {
        if (position < 4) {
            binding.rvAdminList.setAdapter(auditAdapter);
            db.itemPostDao().getAllPostsAdmin().observe(this, posts -> {
                if (posts != null) {
                    List<ItemPost> filtered;
                    switch (position) {
                        case 0: // 审核中
                            filtered = posts.stream().filter(p -> p.status == 0).collect(Collectors.toList());
                            break;
                        case 1: // 显示中 (已通过且未解决)
                            filtered = posts.stream().filter(p -> p.status == 1 && !p.isResolved).collect(Collectors.toList());
                            break;
                        case 2: // 已解决
                            filtered = posts.stream().filter(p -> p.isResolved).collect(Collectors.toList());
                            break;
                        case 3: // 已下架
                            filtered = posts.stream().filter(p -> p.status == 2).collect(Collectors.toList());
                            break;
                        default:
                            filtered = posts;
                    }
                    auditAdapter.setPosts(filtered);
                }
            });
        } else {
            // 用户管理
            binding.rvAdminList.setAdapter(userAdapter);
            db.userDao().getAllUsersAdmin().observe(this, users -> {
                if (users != null) userAdapter.setUsers(users);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
