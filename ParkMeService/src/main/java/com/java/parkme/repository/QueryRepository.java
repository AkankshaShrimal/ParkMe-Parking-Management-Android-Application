package com.java.parkme.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.java.parkme.dao.app.QueryDetailsEntity;

public interface QueryRepository extends CrudRepository<QueryDetailsEntity, Integer> {
	
	public QueryDetailsEntity findByQid(int qid);

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("UPDATE QueryDetailsEntity q SET q.status = ?1, q.queryResolveDate = ?2 WHERE q.qid = ?3")
	public void updateCancelQuery(String status, Date date, int qid);
	
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("UPDATE QueryDetailsEntity q SET q.status = ?1, q.queryResolveDate = ?2, q.rating = ?4 WHERE q.qid = ?3")
	public void updateCloseQuery(String status, Date date, int qid, float rating);
	
	@Query("SELECT AVG(q.rating) FROM QueryDetailsEntity q WHERE q.status IS 'Closed' AND q.toUser = :toUser")
	public Float getRating(@Param("toUser") int toId);
}
