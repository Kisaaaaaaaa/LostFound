package com.example.lostfound.ui.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.data.entity.User;
import com.example.lostfound.databinding.ActivityPostDetailBinding;
import com.example.lostfound.ui.ChatActivity;
import com.example.lostfound.util.SharedPrefsManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    private ActivityPostDetailBinding binding;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private int currentUserId;
    private ItemPost currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = SharedPrefsManager.getInstance(this).getUserId();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        int postId = getIntent().getIntExtra("post_id", -1);
        if (postId != -1) {
            loadPostDetails(postId);
        }

        binding.fabChat.setOnClickListener(v -> {
            if (currentPost != null) {
                if (currentPost.publisherId == currentUserId) {
                    Toast.makeText(this, "这是您自己发布的物品", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                new Thread(() -> {
                    User publisher = AppDatabase.getDatabase(this).userDao().getUserById(currentPost.publisherId);
                    if (publisher != null) {
                        runOnUiThread(() -> {
                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.putExtra("receiver_id", publisher.id);
                            intent.putExtra("receiver_name", publisher.nickname != null ? publisher.nickname : publisher.username);
                            startActivity(intent);
                        });
                    }
                }).start();
            }
        });

        // 编辑按钮
        binding.btnEditPost.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("edit_post_id", currentPost.postId);
            startActivity(intent);
        });

        // 更新状态按钮
        binding.btnUpdateStatus.setOnClickListener(v -> {
            showUpdateStatusDialog();
        });
    }

    private void showUpdateStatusDialog() {
        String[] options = {"标记为已解决", "标记为未解决"};
        new AlertDialog.Builder(this)
                .setTitle("更新状态")
                .setItems(options, (dialog, which) -> {
                    boolean isResolved = (which == 0);
                    updatePostResolvedStatus(isResolved);
                })
                .show();
    }

    private void updatePostResolvedStatus(boolean isResolved) {
        new Thread(() -> {
            int result = AppDatabase.getDatabase(this).itemPostDao().updateResolvedState(currentPost.postId, isResolved);
            if (result > 0) {
                runOnUiThread(() -> {
                    currentPost.isResolved = isResolved;
                    Toast.makeText(this, "状态更新成功", Toast.LENGTH_SHORT).show();
                    updateStatusButtonText(isResolved);
                });
            }
        }).start();
    }

    private void updateStatusButtonText(boolean isResolved) {
        binding.btnUpdateStatus.setText(isResolved ? "标记为未解决" : "标记为已解决");
    }

    private void loadPostDetails(int postId) {
        new Thread(() -> {
            currentPost = AppDatabase.getDatabase(this).itemPostDao().getPostByIdSync(postId);
            if (currentPost != null) {
                runOnUiThread(() -> populateViews(currentPost));
            }
        }).start();
    }

    private void populateViews(ItemPost post) {
        binding.tvDetailTitle.setText(post.title);
        binding.tvDetailCategory.setText(post.category);
        binding.tvDetailTime.setText(dateFormat.format(new Date(post.timestamp)));
        binding.tvDetailDescription.setText(post.description);
        binding.tvDetailLocation.setText(post.locationName);

        Glide.with(this)
                .load(post.imageUri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivDetailImage);
        
        binding.collapsingToolbar.setTitle(post.type == 0 ? "寻物详情" : "招领详情");
        
        // 如果是自己发布的，显示编辑/状态按钮，隐藏私聊按钮
        if (post.publisherId == currentUserId) {
            binding.fabChat.setVisibility(View.GONE);
            binding.layoutOwnerActions.setVisibility(View.VISIBLE);
            updateStatusButtonText(post.isResolved);
        } else {
            binding.fabChat.setVisibility(View.VISIBLE);
            binding.layoutOwnerActions.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
