package com.java.parkme.dto;

public class SlotDTO {
	private int id;
	private int fromUser;
	private String status;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getSlotStartTime() {
		return slotStartTime;
	}
	public void setSlotStartTime(long slotStartTime) {
		this.slotStartTime = slotStartTime;
	}
	public long getSlotReleaseTime() {
		return slotReleaseTime;
	}
	public void setSlotReleaseTime(long slotReleaseTime) {
		this.slotReleaseTime = slotReleaseTime;
	}
		
}
