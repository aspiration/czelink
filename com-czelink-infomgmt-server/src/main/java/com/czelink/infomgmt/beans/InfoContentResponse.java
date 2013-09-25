package com.czelink.infomgmt.beans;

import java.io.Serializable;
import java.util.List;

import com.czelink.server.base.beans.JsonBaseViewBean;

public class InfoContentResponse extends JsonBaseViewBean {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String title;
	
	private List<String> paragraphs;
	
	private String author;
	
	private String date;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<String> paragraphs) {
		this.paragraphs = paragraphs;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
