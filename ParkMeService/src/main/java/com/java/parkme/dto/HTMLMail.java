package com.java.parkme.dto;

public class HTMLMail {

    private final String to;

    public HTMLMail(String to) {
        this.to = to;
    }

    public String getTo() {
        return this.to;
    }

    public String getSubject() {
        return "Forgot Password Request";
    }

    public String getContent() {
        return "<html>" +
                    "<body>" +
                        "<p>Hello $$NAME$$,</p>" +
                        "<p>This an auto generated message. Your new password is '<strong>$$PASSWORD$$</strong>'. Do not share this password with anyone.</p>" +
                    "</body>" +
                "</html>";
    }
}