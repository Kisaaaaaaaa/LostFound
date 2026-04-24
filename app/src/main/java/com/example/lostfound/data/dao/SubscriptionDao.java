package com.example.lostfound.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lostfound.data.entity.Subscription;

import java.util.List;

@Dao
public interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Subscription subscription);

    @Delete
    void delete(Subscription subscription);

    @Update
    void update(Subscription subscription);

    @Query("SELECT * FROM subscriptions WHERE userId = :userId")
    LiveData<List<Subscription>> getSubscriptionsByUser(int userId);

    @Query("SELECT * FROM subscriptions")
    List<Subscription> getAllSubscriptionsSync();

    @Query("UPDATE subscriptions SET lastCheckTime = :lastCheckTime WHERE subId = :subId")
    void updateLastCheckTime(int subId, long lastCheckTime);
}
