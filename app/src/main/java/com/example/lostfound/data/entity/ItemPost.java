package com.example.lostfound.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "item_posts")
public class ItemPost {
    @PrimaryKey(autoGenerate = true)
    public int postId;

    public int type; // 0 for Lost, 1 for Found
    public String title;
    public String category;
    public String description;
    public String imageUri;
    public double latitude;
    public double longitude;
    public String locationName;
    public long timestamp;
    public int publisherId;
    public boolean isResolved;
    
    // 管理员功能新增字段
    public int status; // 0:待审核, 1:显示中, 2:被驳回/下架

    public ItemPost(int type, String title, String category, String description, String imageUri, 
                    double latitude, double longitude, String locationName, long timestamp, 
                    int publisherId, boolean isResolved) {
        this.type = type;
        this.title = title;
        this.category = category;
        this.description = description;
        this.imageUri = imageUri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.timestamp = timestamp;
        this.publisherId = publisherId;
        this.isResolved = isResolved;
        this.status = 0; // 默认待审核
    }

    // Getter 方法
    public int getPostId() { return postId; }
    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImageUri() { return imageUri; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocationName() { return locationName; }
    public long getTimestamp() { return timestamp; }
    public int getPublisherId() { return publisherId; }
    public boolean isResolved() { return isResolved; }
    public int getStatus() { return status; }
}
