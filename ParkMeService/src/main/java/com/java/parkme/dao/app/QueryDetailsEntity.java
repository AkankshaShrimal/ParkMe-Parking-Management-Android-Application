package com.java.parkme.dao.app;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "query_table")
public class QueryDetailsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int qid;
	private int fromUser;
	private int toUser;
	private String status;
	private String message;
	private float rating;
	private String queryType;
	private String vehicleRegistrationNumber;
	private Date queryCreateDate;
	private Date queryResolveDate;
	
	public int getQid() {
		return qid;
	}
	public void setQid(int qid) {
		this.qid = qid;
	}
	public int getFromUser() {
		return fromUser;
	}
	public void setFromUser(int fromUser) {
		this.fromUser = fromUser;
	}
	public int getToUser() {
		return toUser;
	}
	public void setToUser(int toUser) {
		this.toUser = toUser;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public float getRating() {
		return rating;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public String getVehicleRegistrationNumber() {
		return vehicleRegistrationNumber;
	}
	public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
		this.vehicleRegistrationNumber = vehicleRegistrationNumber;
	}
	public Date getQueryCreateDate() {
		return queryCreateDate;
	}
	public void setQueryCreateDate(Date queryCreateDate) {
		this.queryCreateDate = queryCreateDate;
	}
	public Date getQueryResolveDate() {
		return queryResolveDate;
	}
	public void setQueryResolveDate(Date queryResolveDate) {
		this.queryResolveDate = queryResolveDate;
	}

}
