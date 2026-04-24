package com.example.lostfound.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.User;
import com.example.lostfound.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void register(String username, String password, Callback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User existing = repository.getUserByUsername(username);
            if (existing == null) {
                User newUser = new User(username, password);
                
                // 特殊逻辑：如果用户名为 admin，则自动设为管理员
                if ("admin".equalsIgnoreCase(username)) {
                    newUser.role = "admin";
                }
                
                repository.insert(newUser);
                callback.onResult(true);
            } else {
                callback.onResult(false);
            }
        });
    }

    public void login(String username, String password, Callback<User> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = repository.login(username, password);
            callback.onResult(user);
        });
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
