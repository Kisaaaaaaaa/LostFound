package com.example.lostfound.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lostfound.MainActivity;
import com.example.lostfound.databinding.ActivityLoginBinding;
import com.example.lostfound.util.SharedPrefsManager;
import com.example.lostfound.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        if (SharedPrefsManager.getInstance(this).getUserId() != -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.login(username, password, user -> {
                runOnUiThread(() -> {
                    if (user != null) {
                        // 核心修复：检查封禁状态
                        if (user.isBanned) {
                            Toast.makeText(this, "您的账号已被封禁，请联系管理员", Toast.LENGTH_LONG).show();
                        } else {
                            SharedPrefsManager.getInstance(this).saveUserId(user.id);
                            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        binding.tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
