package com.example.lostfound.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.lostfound.data.entity.Subscription;
import com.example.lostfound.data.repository.SubscriptionRepository;

import java.util.List;

public class SubscriptionViewModel extends AndroidViewModel {
    private final SubscriptionRepository repository;

    public SubscriptionViewModel(@NonNull Application application) {
        super(application);
        repository = new SubscriptionRepository(application);
    }

    public void insert(Subscription subscription) {
        repository.insert(subscription);
    }

    public void delete(Subscription subscription) {
        repository.delete(subscription);
    }

    public LiveData<List<Subscription>> getSubscriptionsByUser(int userId) {
        return repository.getSubscriptionsByUser(userId);
    }
}
