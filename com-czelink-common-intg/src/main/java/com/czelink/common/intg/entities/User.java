package com.czelink.common.intg.entities;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Users")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String username;

	private String password;

	/**
	 * true for man, false for woman.
	 */
	private boolean gender;

	/**
	 * unique for sign-in.
	 */
	private String mailAddress;

	private String company;

	private String jobTitle;

	private String[] experiences;

	private String[] interests;

	private String[] otherDescriptions;

	private Date registerDate;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isGender() {
		return gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}

	public String getMailAddress() {
		return mailAddress;
	}

	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String[] getExperiences() {
		return experiences;
	}

	public void setExperiences(String[] experiences) {
		this.experiences = experiences;
	}

	public String[] getInterests() {
		return interests;
	}

	public void setInterests(String[] interests) {
		this.interests = interests;
	}

	public String[] getOtherDescriptions() {
		return otherDescriptions;
	}

	public void setOtherDescriptions(String[] otherDescriptions) {
		this.otherDescriptions = otherDescriptions;
	}

	public Date getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

}
