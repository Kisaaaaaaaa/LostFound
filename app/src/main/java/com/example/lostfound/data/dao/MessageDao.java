package com.example.lostfound.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lostfound.data.entity.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    long insert(Message message);

    @Update
    void update(Message message);

    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    LiveData<List<Message>> getChatHistory(int user1, int user2);

    @Query("SELECT * FROM (SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC) GROUP BY CASE WHEN senderId = :userId THEN receiverId ELSE senderId END")
    LiveData<List<Message>> getContactList(int userId);

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    LiveData<Integer> getUnreadCount(int userId);

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND senderId = :senderId AND isRead = 0")
    int getUnreadCountFromSenderSync(int userId, int senderId);

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :receiverId AND senderId = :senderId")
    void markAsRead(int receiverId, int senderId);
}
