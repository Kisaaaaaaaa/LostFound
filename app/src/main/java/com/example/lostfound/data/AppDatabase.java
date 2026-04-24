package com.example.lostfound.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.lostfound.data.dao.ItemPostDao;
import com.example.lostfound.data.dao.MessageDao;
import com.example.lostfound.data.dao.SubscriptionDao;
import com.example.lostfound.data.dao.UserDao;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.data.entity.Message;
import com.example.lostfound.data.entity.Subscription;
import com.example.lostfound.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, ItemPost.class, Subscription.class, Message.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ItemPostDao itemPostDao();
    public abstract SubscriptionDao subscriptionDao();
    public abstract MessageDao messageDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "lost_found_db")
                            .fallbackToDestructiveMigration() // 允许破坏性迁移
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
