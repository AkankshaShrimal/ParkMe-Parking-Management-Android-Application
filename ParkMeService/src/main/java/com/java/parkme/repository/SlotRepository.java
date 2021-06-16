package com.java.parkme.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.java.parkme.dao.app.SlotDetailsEntity;

public interface SlotRepository extends CrudRepository<SlotDetailsEntity, Integer> {

	
	public SlotDetailsEntity findById(int id);
	
	@Modifying
	@Transactional
	@Query("update SlotDetailsEntity s set s.slotAvailability = ?1, s.slotStartTime = ?2, s.fromUser = ?3 where s.id = ?4")
	void bookSlot(@Param("slotAvailability") String slotAvailability, long slotStartTime, int fromUser, @Param("id") int id);

	@Modifying
	@Transactional
	@Query("update SlotDetailsEntity s set s.fromUser = 0, s.slotAvailability = 'available', s.slotStartTime = 0, s.slotReleaseTime = 0 where s.id = :id")
	void releaseSlot(@Param("id") int id);

}