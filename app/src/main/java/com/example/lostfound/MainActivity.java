package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.databinding.ActivityMainBinding;
import com.example.lostfound.ui.fragment.FoundFragment;
import com.example.lostfound.ui.fragment.LostFragment;
import com.example.lostfound.ui.fragment.MessageListFragment;
import com.example.lostfound.ui.fragment.ProfileFragment;
import com.example.lostfound.ui.post.PostActivity;
import com.example.lostfound.util.SharedPrefsManager;
import com.example.lostfound.worker.ItemMatchWorker;
import com.google.android.material.badge.BadgeDrawable;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Fragment lostFragment, foundFragment, messageListFragment, profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments();
        setupWorkManager();
        observeUnreadMessages();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_lost) {
                switchFragment(lostFragment);
                return true;
            } else if (itemId == R.id.nav_found) {
                switchFragment(foundFragment);
                return true;
            } else if (itemId == R.id.nav_messages) {
                switchFragment(messageListFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });

        switchFragment(lostFragment);

        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        });
    }

    private void observeUnreadMessages() {
        int userId = SharedPrefsManager.getInstance(this).getUserId();
        AppDatabase.getDatabase(this).messageDao().getUnreadCount(userId).observe(this, count -> {
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_messages);
            if (count != null && count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }
        });
    }

    private void initFragments() {
        lostFragment = new LostFragment();
        foundFragment = new FoundFragment();
        messageListFragment = new MessageListFragment();
        profileFragment = new ProfileFragment();
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    private void setupWorkManager() {
        // 1. 立即执行一次匹配检查 (用户登录/进入主页时)
        OneTimeWorkRequest immediateWorkRequest = new OneTimeWorkRequest.Builder(ItemMatchWorker.class).build();
        WorkManager.getInstance(this).enqueue(immediateWorkRequest);

        // 2. 配置每 15 分钟运行一次的定期匹配任务
        PeriodicWorkRequest matchWorkRequest = new PeriodicWorkRequest.Builder(
                ItemMatchWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ItemMatchWork",
                ExistingPeriodicWorkPolicy.KEEP,
                matchWorkRequest
        );
    }
}
