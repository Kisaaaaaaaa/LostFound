package com.example.lostfound.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.Message;
import com.example.lostfound.databinding.ActivityChatBinding;
import com.example.lostfound.ui.adapter.MessageAdapter;
import com.example.lostfound.util.SharedPrefsManager;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private MessageAdapter adapter;
    private int currentUserId;
    private int receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = SharedPrefsManager.getInstance(this).getUserId();
        receiverId = getIntent().getIntExtra("receiver_id", -1);
        String receiverName = getIntent().getStringExtra("receiver_name");

        if (receiverId == -1) {
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(receiverName != null ? receiverName : "聊天中");
        }

        setupRecyclerView();
        observeMessages();
        markMessagesAsRead();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(currentUserId);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(adapter);
    }

    private void observeMessages() {
        AppDatabase.getDatabase(this).messageDao().getChatHistory(currentUserId, receiverId)
                .observe(this, messages -> {
                    adapter.setMessages(messages);
                    if (messages.size() > 0) {
                        binding.rvMessages.scrollToPosition(messages.size() - 1);
                        // 如果在聊天界面收到新消息，也标记为已读
                        markMessagesAsRead();
                    }
                });
    }

    private void markMessagesAsRead() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).messageDao().markAsRead(currentUserId, receiverId);
        });
    }

    private void sendMessage() {
        String content = binding.etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        Message message = new Message(currentUserId, receiverId, content, System.currentTimeMillis());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).messageDao().insert(message);
            runOnUiThread(() -> binding.etMessage.setText(""));
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
