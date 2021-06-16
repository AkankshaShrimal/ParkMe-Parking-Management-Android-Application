package com.android.parkme.common;

public class SlotInfo {
    private int id;
    private int fromUser;
    private String slotAvailability;
    private long slotStartTime;
    private long slotReleaseTime;

    public SlotInfo(int id, int fromUser, String slotAvailability, long slotStartTime, long slotReleaseTime) {
        this.id = id;
        this.fromUser = fromUser;
        this.slotAvailability = slotAvailability;
        this.slotStartTime = slotStartTime;
        this.slotReleaseTime = slotReleaseTime;
    }

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

    public long getSlotReleaseTime() {
        return slotReleaseTime;
    }

    public void setSlotReleaseTime(long slotReleaseTime) {
        this.slotReleaseTime = slotReleaseTime;
    }
}
