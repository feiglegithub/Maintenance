package com.feigle.maintenance.db;

import android.content.Context;

import com.feigle.maintenance.dao.NoticeDao;
import com.feigle.maintenance.entity.Notice;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Notice.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context){
        if (instance == null)
            instance = Room.databaseBuilder(context,AppDatabase.class,"maintenace").build();
        return instance;
    }

    public abstract NoticeDao noticeDao();
}
