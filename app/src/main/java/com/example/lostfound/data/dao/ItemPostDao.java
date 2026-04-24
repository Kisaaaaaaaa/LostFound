package com.example.lostfound.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lostfound.data.entity.ItemPost;

import java.util.List;

@Dao
public interface ItemPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ItemPost itemPost);

    @Update
    int update(ItemPost itemPost);

    @Delete
    int delete(ItemPost itemPost);

    @Query("SELECT * FROM item_posts WHERE postId = :postId LIMIT 1")
    LiveData<ItemPost> getPostById(int postId);

    @Query("SELECT * FROM item_posts WHERE postId = :postId LIMIT 1")
    ItemPost getPostByIdSync(int postId);

    @Query("SELECT * FROM item_posts WHERE type = :type AND status = 1 AND isResolved = 0 ORDER BY timestamp DESC")
    LiveData<List<ItemPost>> getPostsByType(int type);

    @Query("SELECT * FROM item_posts WHERE type = :type AND category = :category AND status = 1 AND isResolved = 0 ORDER BY timestamp DESC")
    LiveData<List<ItemPost>> getPostsByTypeAndCategory(int type, String category);

    @Query("SELECT * FROM item_posts WHERE publisherId = :publisherId ORDER BY timestamp DESC")
    LiveData<List<ItemPost>> getPostsByPublisherId(int publisherId);

    @Query("SELECT * FROM item_posts ORDER BY timestamp DESC")
    LiveData<List<ItemPost>> getAllPostsAdmin();

    @Query("UPDATE item_posts SET status = :status WHERE postId = :postId")
    void updatePostStatus(int postId, int status);

    @Query("SELECT COUNT(*) FROM item_posts WHERE type = 0")
    LiveData<Integer> getTotalLostCount();

    @Query("SELECT COUNT(*) FROM item_posts WHERE type = 1")
    LiveData<Integer> getTotalFoundCount();

    @Query("SELECT COUNT(*) FROM item_posts WHERE isResolved = 1")
    LiveData<Integer> getTotalResolvedCount();
    
    // 新增：统计待审核数量
    @Query("SELECT COUNT(*) FROM item_posts WHERE status = 0")
    LiveData<Integer> getPendingAuditCount();

    @Query("SELECT * FROM item_posts WHERE (title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%') AND status = 1 AND isResolved = 0 ORDER BY timestamp DESC")
    LiveData<List<ItemPost>> searchPosts(String keyword);

    @Query("SELECT * FROM item_posts WHERE (title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%') AND timestamp > :sinceTime AND status = 1 AND isResolved = 0")
    List<ItemPost> searchNewPostsByKeywordSync(String keyword, long sinceTime);

    @Query("SELECT * FROM item_posts WHERE timestamp > :sinceTime AND status = 1 AND isResolved = 0 ORDER BY timestamp ASC")
    List<ItemPost> getPostsAfter(long sinceTime);

    @Query("UPDATE item_posts SET isResolved = :resolved WHERE postId = :postId")
    int updateResolvedState(int postId, boolean resolved);

    @Query("DELETE FROM item_posts WHERE postId = :postId")
    int deleteById(int postId);
}
