package com.example.lostfound.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.dao.SubscriptionDao;
import com.example.lostfound.data.entity.Subscription;

import java.util.List;

public class SubscriptionRepository {
    private final SubscriptionDao subscriptionDao;

    public SubscriptionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        subscriptionDao = db.subscriptionDao();
    }

    public void insert(Subscription subscription) {
        AppDatabase.databaseWriteExecutor.execute(() -> subscriptionDao.insert(subscription));
    }

    public void delete(Subscription subscription) {
        AppDatabase.databaseWriteExecutor.execute(() -> subscriptionDao.delete(subscription));
    }

    public LiveData<List<Subscription>> getSubscriptionsByUser(int userId) {
        return subscriptionDao.getSubscriptionsByUser(userId);
    }
    
    public void updateLastCheckTime(int subId, long lastCheckTime) {
        AppDatabase.databaseWriteExecutor.execute(() -> subscriptionDao.updateLastCheckTime(subId, lastCheckTime));
    }
}
