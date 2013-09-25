package com.czelink.beans;

import java.util.List;

import com.czelink.server.base.beans.JsonBaseViewBean;

public class NavigationListViewBean extends JsonBaseViewBean {

	private static final long serialVersionUID = 1L;
	
	private List<NavigationItemViewModel> navigationList;
	
	private String role;
	
	private String verifyKey;

	public List<NavigationItemViewModel> getNavigationList() {
		return navigationList;
	}

	public void setNavigationList(List<NavigationItemViewModel> navigationList) {
		this.navigationList = navigationList;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getVerifyKey() {
		return verifyKey;
	}

	public void setVerifyKey(String verifyKey) {
		this.verifyKey = verifyKey;
	}
}
