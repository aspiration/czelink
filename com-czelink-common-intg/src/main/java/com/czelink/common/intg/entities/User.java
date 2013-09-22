package com.czelink.common.intg.entities;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Users")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String username;

	private String password;

	private String displayName;

	private Boolean activated;

	/**
	 * true for man, false for woman.
	 */
	private String gender;

	private String company;

	private String jobTitle;

	private String[] experiences;

	private String[] interests;

	private String[] otherDescriptions;

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

	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean isActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
	}

	public String toString() {
		final ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("id", this.getId());
		tsb.append("username", this.username);
		tsb.append("password", this.password);
		tsb.append("displayName", this.displayName);
		tsb.append("gender", this.gender);
		tsb.append("company", this.company);
		tsb.append("jobTitle", this.jobTitle);
		tsb.append("experiences", this.experiences);
		tsb.append("interests", this.interests);
		tsb.append("otherDescriptions", this.otherDescriptions);

		return tsb.toString();
	}

}
