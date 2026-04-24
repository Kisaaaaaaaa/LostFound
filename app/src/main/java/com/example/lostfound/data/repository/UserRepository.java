package com.example.lostfound.data.repository;

import android.app.Application;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.dao.UserDao;
import com.example.lostfound.data.entity.User;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    public void insert(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
    }

    public User login(String username, String password) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> userDao.login(username, password));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByUsername(String username) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> userDao.getUserByUsername(username));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
