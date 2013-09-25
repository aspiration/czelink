package com.czelink.infomgmt.beans;

import java.util.List;

import com.czelink.server.base.beans.JsonBaseViewBean;

public class InfoArticleResponse extends JsonBaseViewBean {

	private static final long serialVersionUID = 1L;
	
	private List<InfoContentResponse> contents;

	public List<InfoContentResponse> getContents() {
		return contents;
	}

	public void setContents(List<InfoContentResponse> contents) {
		this.contents = contents;
	}
}
