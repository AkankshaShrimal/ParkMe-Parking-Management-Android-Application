package com.android.parkme.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "query_table")
public class Query {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "qid")
    private int qid;

    @NonNull
    @ColumnInfo(name = "from_name")
    private String fromName;

    @NonNull
    @ColumnInfo(name = "from_id")
    private int fromId;

    @NonNull
    @ColumnInfo(name = "to_name")
    private String toName;

    @NonNull
    @ColumnInfo(name = "to_id")
    private int toId;

    @NonNull
    @ColumnInfo(name = "status")
    private String status;

    @NonNull
    @ColumnInfo(name = "create_time")
    private long createTime;

    @NonNull
    @ColumnInfo(name = "close_time")
    private long closeTime;

    @NonNull
    @ColumnInfo(name = "rating")
    private float rating;

    @NonNull
    @ColumnInfo(name = "message")
    private String msg;

    @NonNull
    @ColumnInfo(name = "vid")
    private String vid;

    public Query() {
    }

    @Ignore
    public Query(int qid, String status, String fromName, int fromId, String toName, int toId, long createTime, String msg, String vid) {
        this.qid = qid;
        this.status = status;
        this.fromName = fromName;
        this.fromId = fromId;
        this.toName = toName;
        this.toId = toId;
        this.createTime = createTime;
        this.msg = msg;
        this.vid = vid;
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    @NonNull
    public String getFromName() {
        return fromName;
    }

    public void setFromName(@NonNull String fromName) {
        this.fromName = fromName;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    @NonNull
    public String getToName() {
        return toName;
    }

    public void setToName(@NonNull String toName) {
        this.toName = toName;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @NonNull
    public String getMsg() {
        return msg;
    }

    public void setMsg(@NonNull String msg) {
        this.msg = msg;
    }

    @NonNull
    public String getVid() {
        return vid;
    }

    public void setVid(@NonNull String vid) {
        this.vid = vid;
    }
}
