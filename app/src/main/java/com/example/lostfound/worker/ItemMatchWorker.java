package com.example.lostfound.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.data.entity.Message;
import com.example.lostfound.data.entity.Subscription;
import com.example.lostfound.util.NotificationHelper;

import java.util.List;

public class ItemMatchWorker extends Worker {
    private static final String TAG = "ItemMatchWorker";
    private final AppDatabase db;

    public ItemMatchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "正在检查物品匹配和订阅...");
        List<Subscription> subscriptions = db.subscriptionDao().getAllSubscriptionsSync();
        if (subscriptions.isEmpty()) return Result.success();

        for (Subscription sub : subscriptions) {
            // 检查自上次以来是否有新帖
            long checkFrom = sub.lastCheckTime == 0 ? 
                    (System.currentTimeMillis() - 24 * 60 * 60 * 1000) : sub.lastCheckTime;

            List<ItemPost> newPosts = db.itemPostDao().getPostsAfter(checkFrom);
            
            for (ItemPost post : newPosts) {
                // 排除自己发的
                if (post.publisherId == sub.userId) continue;

                String keyword = sub.keyword.toLowerCase();
                if (post.title.toLowerCase().contains(keyword) || post.description.toLowerCase().contains(keyword)) {
                    
                    // 1. 在应用内消息框产生一条系统记录
                    String content = "订阅提醒：为您发现匹配物品 [" + post.title + "]，点击顶部通知可直接进入详情。";
                    Message sysMsg = new Message(0, sub.userId, content, System.currentTimeMillis());
                    db.messageDao().insert(sysMsg);

                    // 2. 发送手机顶端悬浮通知，并支持精准跳转到该帖子详情页
                    NotificationHelper.showPostNotification(
                            getApplicationContext(),
                            "校园失物招领订阅提醒",
                            "发现匹配物品: " + post.title,
                            post.postId // 传递 postId 以实现精准跳转
                    );
                }
            }
            // 更新最后检查时间，防止重复通知
            db.subscriptionDao().updateLastCheckTime(sub.subId, System.currentTimeMillis());
        }

        return Result.success();
    }
}
