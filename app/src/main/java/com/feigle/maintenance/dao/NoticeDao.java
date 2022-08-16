package com.feigle.maintenance.dao;

import com.feigle.maintenance.entity.Notice;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NoticeDao {
    @Query("select * from notice order by nid desc")
    List<Notice> getAll();

    @Insert
    void insert(Notice notice);

    @Query("delete from notice where nid < (select max(nid) - :quantity from notice)")
    void delete(int quantity);

    @Query("update notice set read = 1")
    void updateAllRead();

    @Update
    void update(Notice notice);
}
