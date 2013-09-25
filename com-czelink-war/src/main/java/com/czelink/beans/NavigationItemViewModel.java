package com.czelink.beans;

import java.io.Serializable;

public class NavigationItemViewModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String hashLink;
	
	private String label;

	public String getHashLink() {
		return hashLink;
	}

	public void setHashLink(String hashLink) {
		this.hashLink = hashLink;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}
