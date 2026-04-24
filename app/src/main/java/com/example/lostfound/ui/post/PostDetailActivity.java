package com.example.lostfound.ui.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
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
                    android.widget.Toast.makeText(this, "这是您自己发布的物品", android.widget.Toast.LENGTH_SHORT).show();
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
        
        // 如果是自己发布的，隐藏私聊按钮
        if (post.publisherId == currentUserId) {
            binding.fabChat.setVisibility(View.GONE);
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
