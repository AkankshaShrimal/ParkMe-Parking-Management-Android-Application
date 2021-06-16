package com.android.parkme.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "announcement_table")
public class Announcement {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ann_id")
    private long id;

    @NonNull
    @ColumnInfo(name = "sent_time")
    private long time;

    @NonNull
    @ColumnInfo(name = "msg")
    private String message;

    public Announcement() {
    }

    @Ignore
    public Announcement(long time, String message) {
        this.time = time;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NonNull String message) {
        this.message = message;
    }
}
