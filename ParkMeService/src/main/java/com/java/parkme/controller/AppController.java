package com.java.parkme.controller;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.java.parkme.ParkmeApplication;
import com.java.parkme.dao.app.SlotDetailsEntity;
import com.java.parkme.dao.app.UserEntity;
import com.java.parkme.dto.AnnouncementDTO;
import com.java.parkme.dto.ChatDTO;
import com.java.parkme.dto.ConfirmPasswordDTO;
import com.java.parkme.dto.EmailAndPasswordDTO;
import com.java.parkme.dto.PhoneAndPasswordDTO;
import com.java.parkme.dto.RaiseQueryDTO;
import com.java.parkme.dto.SlotDTO;
import com.java.parkme.dto.UserDTO;
import com.java.parkme.orchestrator.UserOrchestrator;
import com.java.parkme.pushnotification.AndroidPushNotificationsService;

@RestController
@Validated
public class AppController {
	@Autowired
	private UserOrchestrator orchestrator;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AndroidPushNotificationsService androidPushNotificationsService;

	@GetMapping("/")
	public String test() {
		log.info("test called");
		return "UP";
	}

	@GetMapping("/getAll")
	public Iterable<UserDTO> getAllUsers() {
		log.info("getAllUsers called");
		return orchestrator.getAll();
	}
	
	@PostMapping("/addSlot")
	public int addSlot(@RequestBody SlotDetailsEntity ent) throws Exception {
		log.info("addSlot called");
		return orchestrator.addSlot(ent);
	}
	
	@PostMapping("/getAllSlots")
	public Iterable<SlotDetailsEntity> getAllSlots(@RequestHeader(name = "session-id", required = true) String value) throws Exception {
		log.info("getAllSlots called");
		return orchestrator.getAllSlots(orchestrator.validateSessionID(value));
	}
	
	@PostMapping("/updateSlot")
	public String updateSlot(@RequestHeader(name = "session-id", required = true) String value,
			@RequestBody SlotDTO slotDTO) throws Exception {
		log.info("updateSlot called");
		return orchestrator.updateSlotStatus(orchestrator.validateSessionID(value), slotDTO);
	}

	@GetMapping("/deleteAll")
	public void deleteAllUsers() {
		log.info("deleteAll called");
		orchestrator.delete();
	}

	@PostMapping("/add")
	public int addUser(@RequestBody UserDTO dto) throws Exception {
		log.info("addUser called");
		return orchestrator.add(dto);
	}

	@PostMapping("/login")
	public String loginUser(@RequestBody EmailAndPasswordDTO dto) throws Exception {
		log.info("login called " + dto.getEmail());
		return orchestrator.authenticateUsingEmailAndPassword(dto.getEmail(), dto.getPassword(), dto.getToken());
	}

	@PostMapping("/login-phone")
	public String loginUserPhone(@RequestBody PhoneAndPasswordDTO dto) throws Exception {
		log.info("login called " + dto.getNumber());
		return orchestrator.authenticateUsingPhoneAndPassword(dto.getNumber(), dto.getPassword(), dto.getToken());
	}

	@GetMapping("/getDetails")
	public UserEntity getDetails(@RequestHeader(name = "session-id", required = true) String value) throws Exception {
		log.info("getDetails called");
		return orchestrator.validateSessionID(value);
	}

	@PostMapping("/forgot-password")
	public String forgotPassword(@RequestBody Map<String, String> map) throws Exception {
		log.info("forgotPassword called");
		return orchestrator.updatePassword(map.get("email"));
	}

	@PostMapping("/confirm-password")
	public void confirmPassword(@RequestHeader(name = "session-id", required = true) String value,
			@RequestBody ConfirmPasswordDTO dto) throws Exception {
		log.info("confirmPassword called");
		orchestrator.validateConfirm(orchestrator.validateSessionID(value), dto);
	}

	@PostMapping("/raise-query")
	public String raiseQuery(@RequestHeader(name = "session-id", required = true) String value,
			@RequestBody RaiseQueryDTO dto) throws Exception {
		log.info("raiseQuery called");
		return orchestrator.insertQuery(orchestrator.validateSessionID(value), dto);
	}

	@PostMapping("/cancel-query")
	public String cancelQuery(@RequestHeader(name = "session-id", required = true) String value,
			@RequestBody RaiseQueryDTO dto) throws Exception {
		log.info("CancelQuery called");
		return orchestrator.updateQuery(orchestrator.validateSessionID(value), dto);
	}

	@PostMapping("/resolve-query")
	public String resolveQuery(@RequestHeader(name = "session-id", required = true) String value,
			@RequestBody RaiseQueryDTO dto) throws Exception {
		log.info("resolveQuery called");
		return orchestrator.updateQuery(orchestrator.validateSessionID(value), dto);
	}

	@PostMapping("/send-chat")
	public String sendChat(@RequestHeader(name = "session-id", required = true) String value, @RequestBody ChatDTO dto)
			throws Exception {
		log.info("sendChat called");
		return orchestrator.saveAndPublishChat(dto);
	}

	@PostMapping(value = "/publish")
	public ResponseEntity<String> send(@RequestBody AnnouncementDTO dto) throws JSONException {
		log.info("Publishing an announcement");
		JSONObject body = new JSONObject();
		body.put("to", "/topics/" + "announcement");
		JSONObject data = new JSONObject();
		data.put("time", new Date().getTime());
		data.put("message", dto.getMessage());
		data.put("type", "announcement");
		body.put("data", data);

		HttpEntity<String> request = new HttpEntity<>(body.toString());

		CompletableFuture<String> pushNotification = androidPushNotificationsService.send(request);
		CompletableFuture.allOf(pushNotification).join();

		try {
			String firebaseResponse = pushNotification.get();
			return new ResponseEntity<>(firebaseResponse, HttpStatus.OK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>("Push Notification ERROR!", HttpStatus.BAD_REQUEST);
	}

	@PostMapping("/restart")
	public void restart() {
		ParkmeApplication.restart();
	}
	//    
	//	@GetMapping(value = "/test", produces = "application/json")
	//	public ResponseEntity<String> testAPIsend() throws JSONException {
	//		Float x = q.getRating(2);
	//		return new ResponseEntity<>(String.valueOf(x), HttpStatus.OK);
	//	}

}
