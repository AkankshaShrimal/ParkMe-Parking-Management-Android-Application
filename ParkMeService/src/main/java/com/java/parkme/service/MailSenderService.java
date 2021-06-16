package com.java.parkme.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.java.parkme.dto.HTMLMail;

@Service
public class MailSenderService {

	@Autowired
	private JavaMailSender mailSender;

	// Use it to send HTML emails
	@Async("asyncExecutor")
	public String sendHTMLMail(HTMLMail mail, String name, String password) throws MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		String messageText = mail.getContent().replace("$$NAME$$", name).replace("$$PASSWORD$$", password);
		MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

		helper.setTo(mail.getTo());
		helper.setSubject(mail.getSubject());
		message.setContent(messageText, "text/html");
		mailSender.send(message);
		return messageText;
	}
}