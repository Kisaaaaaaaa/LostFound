package com.example.lostfound.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.dao.ItemPostDao;
import com.example.lostfound.data.entity.ItemPost;

import java.util.List;

public class ItemPostRepository {
    private final ItemPostDao itemPostDao;

    public ItemPostRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        itemPostDao = db.itemPostDao();
    }

    public void insert(ItemPost itemPost) {
        AppDatabase.databaseWriteExecutor.execute(() -> itemPostDao.insert(itemPost));
    }

    public void update(ItemPost itemPost) {
        AppDatabase.databaseWriteExecutor.execute(() -> itemPostDao.update(itemPost));
    }

    public void delete(ItemPost itemPost) {
        AppDatabase.databaseWriteExecutor.execute(() -> itemPostDao.delete(itemPost));
    }

    public LiveData<List<ItemPost>> getPostsByType(int type) {
        return itemPostDao.getPostsByType(type);
    }

    public LiveData<List<ItemPost>> getPostsByTypeAndCategory(int type, String category) {
        return itemPostDao.getPostsByTypeAndCategory(type, category);
    }

    public LiveData<List<ItemPost>> getPostsByPublisherId(int publisherId) {
        return itemPostDao.getPostsByPublisherId(publisherId);
    }

    public LiveData<List<ItemPost>> searchPosts(String keyword) {
        return itemPostDao.searchPosts(keyword);
    }

    public void updateResolvedState(int postId, boolean resolved) {
        AppDatabase.databaseWriteExecutor.execute(() -> itemPostDao.updateResolvedState(postId, resolved));
    }
}
