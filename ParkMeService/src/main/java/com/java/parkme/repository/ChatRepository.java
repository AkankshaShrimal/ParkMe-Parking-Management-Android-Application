package com.java.parkme.repository;

import org.springframework.data.repository.CrudRepository;

import com.java.parkme.dao.app.ChatEntity;

public interface ChatRepository  extends CrudRepository<ChatEntity, Integer> {
	
}
