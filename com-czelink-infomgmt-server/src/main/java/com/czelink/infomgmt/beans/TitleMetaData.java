package com.czelink.infomgmt.beans;

import java.io.Serializable;

public class TitleMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String text;

	private String picUrl;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

}
