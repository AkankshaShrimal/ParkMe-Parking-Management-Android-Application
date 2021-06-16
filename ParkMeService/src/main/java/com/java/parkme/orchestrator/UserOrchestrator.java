package com.java.parkme.orchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.JsonObject;
import com.java.parkme.config.PasswordUtils;
import com.java.parkme.dao.app.ChatEntity;
import com.java.parkme.dao.app.QueryDetailsEntity;
import com.java.parkme.dao.app.SlotDetailsEntity;
import com.java.parkme.dao.app.UserEntity;
import com.java.parkme.dto.ChatDTO;
import com.java.parkme.dto.ConfirmPasswordDTO;
import com.java.parkme.dto.HTMLMail;
import com.java.parkme.dto.RaiseQueryDTO;
import com.java.parkme.dto.SlotDTO;
import com.java.parkme.dto.UserDTO;
import com.java.parkme.pushnotification.AndroidPushNotificationsService;
import com.java.parkme.repository.ChatRepository;
import com.java.parkme.repository.QueryRepository;
import com.java.parkme.repository.SlotRepository;
import com.java.parkme.repository.UserRepository;
import com.java.parkme.service.MailSenderService;
import com.java.parkme.service.UserService;

@Component
public class UserOrchestrator {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserService service;

	@Autowired
	private MailSenderService senderService;

	@Autowired
	AndroidPushNotificationsService androidPushNotificationsService;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private QueryRepository queryRepository;

	@Autowired
	private ChatRepository chatRepository;

	public static String salt = PasswordUtils.getSalt();

	public synchronized int add(UserDTO dto) throws Exception {
		UserEntity c = userRepository.findByEmail(dto.getEmail().toLowerCase());
		if (c != null)
			throw new Exception("##errorCode:211,errorMessage:This email is already registered##");
		UserEntity ent = new UserEntity();
		ent.setNumber(dto.getNumber());
		ent.setFullname(dto.getFullname().toLowerCase());
		ent.setPassword(PasswordUtils.generateSecurePassword(dto.getPassword(), salt));
		ent.setAddress(dto.getAddress().toLowerCase());
		ent.setEmail(dto.getEmail().toLowerCase());
		ent.setVid(dto.getVid().toLowerCase().replaceAll(" ", ""));
		int id = userRepository.save(ent).getId();
		return id;
	}

	public Iterable<UserDTO> getAll() {
		Iterable<UserEntity> list = userRepository.findAll();
		List<UserDTO> outList = new ArrayList<UserDTO>();
		UserDTO dto = null;
		for (UserEntity ent : list) {
			dto = new UserDTO();
			dto.setId(ent.getId());
			dto.setNumber(ent.getNumber());
			dto.setFullname(ent.getFullname());
			dto.setAddress(ent.getAddress());
			dto.setEmail(ent.getEmail());
			dto.setVid(ent.getVid());
			outList.add(dto);
		}
		return outList;
	}

	public synchronized int addSlot(SlotDetailsEntity ent) throws Exception {
		SlotDetailsEntity s = slotRepository.findById(ent.getId());
		if (s != null)
			throw new Exception("##errorCode:211,errorMessage:This slot is already present##");
		SlotDetailsEntity sNew = new SlotDetailsEntity();
		sNew.setFromUser(ent.getFromUser());
		sNew.setSlotReleaseTime(ent.getSlotReleaseTime());
		sNew.setSlotStartTime(ent.getSlotStartTime());
		sNew.setSlotAvailability(ent.getSlotAvailability());
		int id = slotRepository.save(sNew).getId();
		return id;
	}
	
	public Iterable<SlotDetailsEntity> getAllSlots(UserEntity from) throws Exception{
		Iterable<SlotDetailsEntity> list = slotRepository.findAll();
		List<SlotDetailsEntity> outList = new ArrayList<SlotDetailsEntity>();
		SlotDetailsEntity s_ent = null;
		for (SlotDetailsEntity ent : list) {
			s_ent = new SlotDetailsEntity();
			s_ent.setId(ent.getId());
			s_ent.setFromUser(ent.getFromUser());
			s_ent.setSlotAvailability(ent.getSlotAvailability());
			s_ent.setSlotStartTime(ent.getSlotStartTime());
			s_ent.setSlotReleaseTime(ent.getSlotReleaseTime());
			outList.add(s_ent);
		}
		
		return outList;
	}

	public String updateSlotStatus(UserEntity from, SlotDTO slotDTO) throws Exception {
		SlotDetailsEntity slotEntity = slotRepository.findById(slotDTO.getId());
		if (slotDTO.getStatus().equals("BOOK")) {
			slotRepository.bookSlot("Occupied", slotDTO.getSlotStartTime(), slotDTO.getFromUser(), slotDTO.getId());
			return slotBooked(slotEntity);
		} else {
			slotRepository.releaseSlot(slotEntity.getId());
			return slotReleased(slotEntity, slotDTO.getSlotReleaseTime());
		}
	}

	private String slotBooked(SlotDetailsEntity s) {
		JSONObject obj = new JSONObject();
		obj.put("message", "Slot "+s.getId() +" booked successfully!");
		return obj.toString();
	}

	private String slotReleased(SlotDetailsEntity slotEntity, long slotReleaseTime) {
		JSONObject obj = new JSONObject();
		long duration  = slotReleaseTime - slotEntity.getSlotStartTime();
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		double d = (double)diffInMinutes/60*100;
		if (d != 0)
			obj.put("message", "Slot "+slotEntity.getId() +" Released.. Please pay Rs."+d);
		else
			obj.put("message", "Slot "+slotEntity.getId() +" Released."+d);
		return obj.toString();
	}

	public String authenticateUsingEmailAndPassword(String email, String password, String token) throws Exception {
		service.validateToken(token);
		service.validateEmailAndPassword(email, password);
		UserEntity c = userRepository.findByEmail(email.toLowerCase());
		if (c != null && PasswordUtils.verifyUserPassword(password, c.getPassword(), salt)) {
			c.setSessionId(UUID.randomUUID().toString());
			c.setToken(token);
			return loginReturn(userRepository.save(c));
		}
		throw new Exception("^5000:Please check email and password$", null);
	}

	public String authenticateUsingPhoneAndPassword(long number, String password, String token) throws Exception {
		service.validateToken(token);
		service.validatePhoneAndPassword(number, password);
		UserEntity c = userRepository.findByNumber(number);
		if (c != null && PasswordUtils.verifyUserPassword(password, c.getPassword(), salt)) {
			c.setSessionId(UUID.randomUUID().toString());
			c.setToken(token);
			return loginReturn(userRepository.save(c));
		}
		throw new Exception("^5000:Please check Phone number and password$", null);
	}

	private String loginReturn(UserEntity c) {
		JSONObject obj = new JSONObject();
		obj.put("id", c.getId());
		obj.put("fullname", c.getFullname());
		obj.put("email", c.getEmail());
		obj.put("number", c.getNumber());
		obj.put("address", c.getAddress());
		obj.put("sessionKey", c.getSessionId());
		return obj.toString();
	}

	public void delete() {
		userRepository.deleteAll();
	}

	public UserEntity validateSessionID(String value) throws Exception {
		service.validateSessionID(value);
		UserEntity c = userRepository.findBySessionId(value);
		log.info("Session id is "+value);
		if (c == null)
			throw new Exception("^5000:SessionID does not exist$");
		return c;
	}

	public String updatePassword(String email) throws Exception {
		service.emailValidation(email);
		UserEntity user = userRepository.findByEmail(email.toLowerCase());
		if (user == null)
			throw new Exception("^5000:This Email ID is not registered$");
		StringBuilder newPassword = new StringBuilder(UUID.randomUUID().toString().substring(0, 5));
		newPassword.append(new Random().nextInt(10));
		newPassword.append(Character.toString((char) (97 + new Random().nextInt(24))));
		newPassword.append(Character.toString((char) (65 + new Random().nextInt(24))));
		newPassword.append("'");
		user.setPassword(PasswordUtils.generateSecurePassword(newPassword.toString(), salt));
		user.setSessionId(null);
		userRepository.save(user);
		senderService.sendHTMLMail(new HTMLMail(user.getEmail()), user.getFullname(), newPassword.toString());
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("olla", "hoho");
		return jsonObject.toString();
	}

	public void validateConfirm(UserEntity c, ConfirmPasswordDTO dto) throws Exception {
		service.emailValidation(dto.getEmail());
		UserEntity user = userRepository.findByEmail(dto.getEmail().toLowerCase());
		if (user == null)
			throw new Exception("^5000:This Email ID is not registered$");
		service.validateConfirm(c, dto);
		c.setPassword(PasswordUtils.generateSecurePassword(dto.getNewPassword(), salt));
		userRepository.save(c);
	}

	/* Request payload
	  {
		  "message": "Hi, You have parked in wrong slot",
		  "queryType": "Parked in Wrong Slot",
		  "status": "Unresolved",
		  "vehicleRegistrationNumber": "DL9CA1518",
		  "queryCreateDate":"2021-05-09'T'02:15:30"
	  }
	*/
	/* notification response (to whom the query is raised against)
		fromUser : shivam sharma
	    status : Unresolved
	    msg : You have parked in my spot
	    qid : 11
	    time : 1620504354000
	    type : Unresolved
	    title : QUERY RAISED Vehicle No:dl9ca1518
	 */
	/* sync response (the person who raised the query)
		{"toUser":"shivam sharma","toId":2,"qid":"21"}
	 */
	public String insertQuery(UserEntity from, RaiseQueryDTO dto) throws Exception {
		dto.setVehicleRegistrationNumber(dto.getVehicleRegistrationNumber().toLowerCase().replaceAll(" ", ""));
		QueryDetailsEntity qent = new QueryDetailsEntity();
		qent.setStatus(dto.getStatus());
		qent.setMessage(dto.getMessage());
		qent.setQueryType(dto.getQueryType());
		qent.setFromUser(from.getId());
		qent.setQueryCreateDate(dto.getQueryCreateDate());
		UserEntity to = userRepository.findByVid(dto.getVehicleRegistrationNumber());
		if (to == null) {
			Optional<UserEntity> admin = userRepository.findById(1);
			to = admin.get();
		}
		qent.setToUser(to.getId());
		qent.setVehicleRegistrationNumber(dto.getVehicleRegistrationNumber());
		dto.setQid(queryRepository.save(qent).getQid());
		createRaiseQueryNotificationJsonAndPush(from, to, dto);
		return raiseQueryReturn(to, dto);
	}

	@JsonPropertyOrder({ "qid", "toUser", "toId" })
	private String raiseQueryReturn(UserEntity to, RaiseQueryDTO dto) {
		JSONObject obj = new JSONObject();
		obj.put("qid", String.valueOf(dto.getQid()));
		obj.put("toUser", to.getFullname());
		obj.put("toId", to.getId());
		return obj.toString();
	}
	
	private boolean createRaiseQueryNotificationJsonAndPush(UserEntity from, UserEntity to, RaiseQueryDTO dto) throws Exception {
		JSONObject body = new JSONObject();
		if (to.getToken() == null || to.getToken() == "")
			throw new Exception("^7000:Admin user has not registered the token.$");
		body.put("to", to.getToken());
		JSONObject data = new JSONObject();
		data.put("fromUser", from.getFullname());
		data.put("fromId", from.getId());
		data.put("status", dto.getStatus());
		data.put("create_time", dto.getQueryCreateDate().getTime());
		data.put("qid", dto.getQid());
		data.put("vehicleRegistrationNumber", dto.getVehicleRegistrationNumber());
		data.put("title", "QUERY RAISED Vehicle No:" + to.getVid());
		data.put("type", dto.getStatus());
		data.put("msg", dto.getMessage());
		body.put("data", data);
		return androidPushNotificationsService.push(body);
	}

	public synchronized String updateQuery(UserEntity from, RaiseQueryDTO dto) throws Exception {
		QueryDetailsEntity q = queryRepository.findByQid(dto.getQid());
		if (dto.getStatus().toLowerCase().equals("cancelled"))
			queryRepository.updateCancelQuery(dto.getStatus(), dto.getQueryResolveDate(), dto.getQid());
		else
			queryRepository.updateCloseQuery(dto.getStatus(), dto.getQueryResolveDate(), dto.getQid(), dto.getRating());
		Optional<UserEntity> to = userRepository.findById(q.getToUser());
		createCancelAndCloseQueryNotificationJsonAndPush(from, to.get(), dto);
		return cancalAndCloseQueryReturn(dto);
	}
	
	private String cancalAndCloseQueryReturn(RaiseQueryDTO dto) {
		JSONObject obj = new JSONObject();
		obj.put("message", "Query "+dto.getQid()+" "+dto.getStatus());
		return obj.toString();
	}
	
	private boolean createCancelAndCloseQueryNotificationJsonAndPush(UserEntity from, UserEntity to, RaiseQueryDTO dto) throws Exception {
		JSONObject body = new JSONObject();
		if (to.getToken() == null || to.getToken() == "")
			throw new Exception("^7000:Admin user has not registered the token.$");
		body.put("to", to.getToken());
		JSONObject data = new JSONObject();
		data.put("status", dto.getStatus());
		data.put("close_time", dto.getQueryResolveDate().getTime());
		data.put("qid", dto.getQid());
		if (dto.getStatus().equals("Cancelled"))
			data.put("title", "Query Cancelled");
		else if (dto.getStatus().equals("Closed")) {
			data.put("rating", dto.getRating());
			data.put("title", "Query Closed");
		} else
			data.put("title", "QUERY RAISED Vehicle No:" + to.getVid());
		data.put("type", dto.getStatus());
		body.put("data", data);
		return androidPushNotificationsService.push(body);
	}

	public String saveAndPublishChat(ChatDTO chat) {
		JSONObject obj = new JSONObject();
		ChatEntity ch = chatRepository.save(new ChatEntity(chat.getTime(), chat.getQid(), chat.getFromId(),
				chat.getToId(), createChatJsonAndPush(chat), chat.getMsg()));
		obj.put("status", ch.getDeliveryStatus());
		return obj.toString();
	}

	private boolean createChatJsonAndPush(ChatDTO chat) {
		JSONObject body = new JSONObject();
		Optional<UserEntity> to = userRepository.findById(chat.getToId());
		body.put("to", to.get().getToken());
		JSONObject data = new JSONObject();
		data.put("fromId", chat.getFromId());
		data.put("time", chat.getTime());
		data.put("qid", chat.getQid());
		data.put("msg", chat.getMsg());
		data.put("type", "chat");
		body.put("data", data);
		return androidPushNotificationsService.push(body);
	}

}
