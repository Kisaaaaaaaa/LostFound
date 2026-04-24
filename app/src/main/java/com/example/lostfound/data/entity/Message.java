package com.example.lostfound.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int senderId;
    public int receiverId;
    public String content;
    public long timestamp;
    public boolean isRead; // 新增已读/未读状态

    public Message(int senderId, int receiverId, String content, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = false; // 默认未读
    }
}
