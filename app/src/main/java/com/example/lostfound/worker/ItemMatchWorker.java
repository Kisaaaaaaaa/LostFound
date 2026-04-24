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
    private static final String TAG = "SubscriptionWorker";
    private final AppDatabase db;

    public ItemMatchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<Subscription> subscriptions = db.subscriptionDao().getAllSubscriptionsSync();
        if (subscriptions.isEmpty()) return Result.success();

        for (Subscription sub : subscriptions) {
            long checkFrom = sub.lastCheckTime == 0 ? 
                    (System.currentTimeMillis() - 24 * 60 * 60 * 1000) : sub.lastCheckTime;

            List<ItemPost> newPosts = db.itemPostDao().getPostsAfter(checkFrom);
            
            for (ItemPost post : newPosts) {
                if (post.publisherId == sub.userId) continue;

                String keyword = sub.keyword.toLowerCase();
                if (post.title.toLowerCase().contains(keyword) || post.description.toLowerCase().contains(keyword)) {
                    
                    // 核心逻辑：向数据库插入一条“系统消息” (senderId = 0)
                    String content = "订阅提醒：为您发现匹配物品 [" + post.title + "]，点击进入大厅查看。";
                    Message sysMsg = new Message(0, sub.userId, content, System.currentTimeMillis());
                    db.messageDao().insert(sysMsg);

                    // 同时保留通知栏提醒
                    NotificationHelper.showMatchNotification(
                            getApplicationContext(),
                            "发现匹配物品: " + sub.keyword,
                            "新信息：" + post.title
                    );
                }
            }
            db.subscriptionDao().updateLastCheckTime(sub.subId, System.currentTimeMillis());
        }

        return Result.success();
    }
}
