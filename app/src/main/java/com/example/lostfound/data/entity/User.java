package com.example.lostfound.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String username;
    public String password;
    public String avatarUri;
    public String nickname;
    
    // 管理员功能新增字段
    public String role; // "admin" 或 "user"
    public boolean isBanned; // 是否被封禁

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.nickname = username;
        this.role = "user"; // 默认为普通用户
        this.isBanned = false; // 默认未封禁
    }
}
