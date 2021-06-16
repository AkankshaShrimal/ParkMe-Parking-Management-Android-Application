package com.java.parkme.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.java.parkme.dao.app.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
	public UserEntity findByEmail(String email);
	
	public UserEntity findByNumber(long number);

	public UserEntity findBySessionId(String uuid);

	@Modifying
	@Transactional
	@Query("update UserEntity u set u.sessionId = :uuid where u.email = :email")
	void updateSessionId(@Param("uuid") String uuid, @Param("email") String email);
	
	public UserEntity findByVid(String vid);
}