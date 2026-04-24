package com.example.lostfound.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscriptions")
public class Subscription {
    @PrimaryKey(autoGenerate = true)
    public int subId;

    public int userId;
    public String keyword;
    public long lastCheckTime;

    public Subscription(int userId, String keyword, long lastCheckTime) {
        this.userId = userId;
        this.keyword = keyword;
        this.lastCheckTime = lastCheckTime;
    }
}
