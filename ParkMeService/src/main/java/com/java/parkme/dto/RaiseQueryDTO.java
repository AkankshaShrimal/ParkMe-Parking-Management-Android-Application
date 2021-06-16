package com.java.parkme.dto;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RaiseQueryDTO {
	private int qid;
	private String message;
	private String queryType;
	private MultipartFile file;
	private int fromUser;
	private int toUser;
	private float rating;
	private String status;
	private String check;
	private String vehicleRegistrationNumber;
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	private Date queryCreateDate;
	private Date queryResolveDate;

	public String getCheck() {
		return check;
	}
	public void setCheck(String check) {
		this.check = check;
	}
	
	public int getQid() {
		return qid;
	}
	public void setQid(int qid) {
		this.qid = qid;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public MultipartFile getFile() {
		return file;
	}
	public void setFile(MultipartFile file) {
		this.file = file;
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
	public float getRating() {
		return rating;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
