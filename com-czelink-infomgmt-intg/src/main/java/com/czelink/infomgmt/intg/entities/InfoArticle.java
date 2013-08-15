package com.czelink.infomgmt.intg.entities;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.czelink.intg.entities.User;

@Document(collection = "InfoArticles")
public class InfoArticle implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String title;

	private String[] paragraphs;

	private Date date;

	@DBRef
	private User auther;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String[] getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(String[] paragraphs) {
		this.paragraphs = paragraphs;
	}

	public User getAuther() {
		return auther;
	}

	public void setAuther(User auther) {
		this.auther = auther;
	}
}
