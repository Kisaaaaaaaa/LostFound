package com.example.lostfound.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lostfound.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    // 管理员功能
    @Query("SELECT * FROM users WHERE role = 'user' ORDER BY id DESC")
    LiveData<List<User>> getAllUsersAdmin();

    @Query("UPDATE users SET isBanned = :isBanned WHERE id = :userId")
    void updateUserBanStatus(int userId, boolean isBanned);
}
