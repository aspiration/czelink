package com.czelink.infomgmt.intg.entities;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.czelink.common.intg.entities.User;

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

	public String toString() {
		final ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("id", this.id);
		tsb.append("auther", this.auther);
		tsb.append("date", this.date);
		tsb.append("paragraphs", this.paragraphs);
		tsb.append("title", this.title);
		return tsb.toString();
	}

}
