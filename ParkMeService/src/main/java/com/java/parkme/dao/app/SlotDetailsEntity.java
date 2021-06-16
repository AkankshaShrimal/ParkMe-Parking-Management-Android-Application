package com.java.parkme.dao.app;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "slots_table")
public class SlotDetailsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private int fromUser;
	private String slotAvailability;
	private long slotStartTime;
	private long slotReleaseTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getFromUser() {
		return fromUser;
	}
	public void setFromUser(int fromUser) {
		this.fromUser = fromUser;
	}
	public long getSlotReleaseTime() {
		return slotReleaseTime;
	}
	public void setSlotReleaseTime(long slotReleaseTime) {
		this.slotReleaseTime = slotReleaseTime;
	}
	public String getSlotAvailability() {
		return slotAvailability;
	}
	public void setSlotAvailability(String slotAvailability) {
		this.slotAvailability = slotAvailability;
	}
	public long getSlotStartTime() {
		return slotStartTime;
	}
	public void setSlotStartTime(long slotStartTime) {
		this.slotStartTime = slotStartTime;
	}
}
