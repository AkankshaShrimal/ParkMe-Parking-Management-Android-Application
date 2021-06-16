package com.android.parkme.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ParkMeDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Chat chat);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(com.android.parkme.database.Query query);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Announcement announcement);

    @Query("SELECT * FROM chat_table where qid=:qid ORDER BY time")
    List<Chat> getChatForQueryID(int qid);

    @Query("SELECT * FROM query_table where qid=:qid")
    com.android.parkme.database.Query getQuery(int qid);

    @Query("SELECT * FROM query_table where from_id=:id")
    List<com.android.parkme.database.Query> raisedByMe(int id);

    @Query("SELECT * FROM query_table where to_id=:id")
    List<com.android.parkme.database.Query> raisedAgainstMe(int id);

    @Query("UPDATE query_table SET status =:status, close_time =:closeTime where qid=:qid")
    void updateCancelRequest(String status, long closeTime, int qid);

    @Query("UPDATE query_table SET status =:status, close_time =:closeTime, rating =:rating where qid=:qid")
    void updateCloseRequest(String status, long closeTime, int qid, float rating);

    @Query("Select * FROM ANNOUNCEMENT_TABLE")
    List<Announcement> getAll();
}