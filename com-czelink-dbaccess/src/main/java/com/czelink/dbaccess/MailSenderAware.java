package com.czelink.dbaccess;

import org.springframework.mail.javamail.JavaMailSender;

public interface MailSenderAware {

	public void setMailSender(final JavaMailSender pMailSender);
}
