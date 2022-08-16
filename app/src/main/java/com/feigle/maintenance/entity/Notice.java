package com.feigle.maintenance.entity;

import java.io.Serializable;
import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Notice implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int nid;

    public String info;

    @ColumnInfo(name = "create_time")
    public String createTime;

    public boolean read;
}
