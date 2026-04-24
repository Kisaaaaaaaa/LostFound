package com.example.lostfound.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.data.entity.Subscription;
import com.example.lostfound.util.NotificationHelper;

import java.util.List;

public class SubscriptionWorker extends Worker {
    public SubscriptionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(context);

        // 1. 获取所有订阅
        List<Subscription> subscriptions = db.subscriptionDao().getAllSubscriptionsSync();
        
        for (Subscription sub : subscriptions) {
            // 2. 检查自上次检查以来是否有匹配关键词的新贴
            List<ItemPost> newMatches = db.itemPostDao().searchNewPostsByKeywordSync(sub.keyword, sub.lastCheckTime);
            
            if (newMatches != null && !newMatches.isEmpty()) {
                // 3. 发送通知
                String content = "为您发现了 " + newMatches.size() + " 条关于 \"" + sub.keyword + "\" 的新信息";
                NotificationHelper.showNotification(context, "失物招领订阅提醒", content);
                
                // 4. 更新上次检查时间
                sub.lastCheckTime = System.currentTimeMillis();
                db.subscriptionDao().update(sub);
            }
        }

        return Result.success();
    }
}
