package com.example.lostfound.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.lostfound.MainActivity;
import com.example.lostfound.ui.post.PostDetailActivity;

public class NotificationHelper {
    // 更改 Channel ID 以强制创建具有高优先级的新频道（解决系统缓存问题）
    private static final String CHANNEL_ID = "lost_found_reminder_v3";
    private static final String CHANNEL_NAME = "失物招领提醒服务";

    public static void showNotification(Context context, String title, String content) {
        showPostNotification(context, title, content, -1);
    }

    /**
     * 显示悬浮通知并支持精准跳转
     */
    public static void showPostNotification(Context context, String title, String content, int postId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 必须是 IMPORTANCE_HIGH 才能在顶部悬浮
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, 
                    CHANNEL_NAME, 
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        Intent intent;
        if (postId != -1) {
            intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post_id", postId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(context, MainActivity.class);
        }

        // 使用唯一的 requestCode (这里用时间戳) 确保多个通知不冲突
        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 优先级必须为 HIGH
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // 发送通知，使用不同的 ID 避免覆盖
        manager.notify(requestCode, builder.build());
    }

    public static void showMatchNotification(Context context, String title, String content) {
        showPostNotification(context, title, content, -1);
    }
}
