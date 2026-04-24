package com.example.lostfound.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.data.repository.ItemPostRepository;

import java.util.List;

public class ItemPostViewModel extends AndroidViewModel {
    private final ItemPostRepository repository;

    public ItemPostViewModel(@NonNull Application application) {
        super(application);
        repository = new ItemPostRepository(application);
    }

    public void insert(ItemPost itemPost) {
        repository.insert(itemPost);
    }

    public void update(ItemPost itemPost) {
        repository.update(itemPost);
    }

    public void delete(ItemPost itemPost) {
        repository.delete(itemPost);
    }

    public LiveData<List<ItemPost>> getPostsByType(int type) {
        return repository.getPostsByType(type);
    }

    public LiveData<List<ItemPost>> getPostsByTypeAndCategory(int type, String category) {
        return repository.getPostsByTypeAndCategory(type, category);
    }

    public LiveData<List<ItemPost>> getPostsByPublisherId(int publisherId) {
        return repository.getPostsByPublisherId(publisherId);
    }

    public LiveData<List<ItemPost>> searchPosts(String keyword) {
        return repository.searchPosts(keyword);
    }

    public void updateResolvedState(int postId, boolean resolved) {
        repository.updateResolvedState(postId, resolved);
    }
}
