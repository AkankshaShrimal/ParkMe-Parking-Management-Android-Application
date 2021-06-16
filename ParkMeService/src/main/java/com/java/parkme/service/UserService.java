package com.java.parkme.service;

import org.springframework.stereotype.Service;

import com.java.parkme.config.PasswordUtils;
import com.java.parkme.dao.app.UserEntity;
import com.java.parkme.dto.ConfirmPasswordDTO;

// email errors 500x
// password errors 600x
@Service
public class UserService {
	static String salt = PasswordUtils.getSalt();

	public void validateEmailAndPassword(String email, String password) throws Exception {
		this.emailValidation(email);
		this.passwordValidation(password);
	}

	public void emailValidation(String email) throws Exception {
		if (email.equals("") || !email.matches(".*@.*\\..*"))
			throw new Exception("^5001:Invalid Email ID$");
	}
	
	public void phoneValidation(long number) throws Exception {
		if (number < 1000000000l || number > 9999999999l)
			throw new Exception("^5001:Invalid Phone Number$");
	}

	private void passwordValidation(String password) throws Exception {
		if (password.equals("") || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,' ~?]).+$"))
			throw new Exception(
					"^6001:Password must contain at least 1 lowercase letter, digit, special character and uppercase letter$",
					null);
	}

	public void validateSessionID(String value) throws Exception {
		if (value.toString().length() != 36)
			throw new Exception("^5000:Invalid sessionID$", null);
	}

	public void validateConfirm(UserEntity c, ConfirmPasswordDTO dto) throws Exception {
		if (!PasswordUtils.verifyUserPassword(dto.getOldPassword(), c.getPassword(), salt))
			throw new Exception("^6002:Invalid Password$");
		if (!dto.getNewPassword().equals(dto.getNewConfirmPassword()))
			throw new Exception("^6003:New Password And Confirm Password should be same$");
		this.passwordValidation(dto.getNewConfirmPassword());
		if (PasswordUtils.verifyUserPassword(dto.getOldPassword(), dto.getNewPassword(), salt))
			throw new Exception("^6004:old Password is same as new password$");
	}

	public void validateToken(String token) throws Exception {
		if (token == null || token == "")
			throw new Exception("^5000:Invalid Token$");
	}

	public void validatePhoneAndPassword(long number, String password) throws Exception {
		this.phoneValidation(number);
		this.passwordValidation(password);		
	}

}
