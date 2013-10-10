package com.czelink.infomgmt.beans;

import java.io.Serializable;

public class NewInfoArticleRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private TitleMetaData title;

	private String[] paragraphs;

	private String[] picUrls;

	private String userId;

	public TitleMetaData getTitle() {
		return title;
	}

	public void setTitle(TitleMetaData title) {
		this.title = title;
	}

	public String[] getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(String[] paragraphs) {
		this.paragraphs = paragraphs;
	}

	public String[] getPicUrls() {
		return picUrls;
	}

	public void setPicUrls(String[] picUrls) {
		this.picUrls = picUrls;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
