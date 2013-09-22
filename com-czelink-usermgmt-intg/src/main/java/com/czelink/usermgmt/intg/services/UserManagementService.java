package com.czelink.usermgmt.intg.services;

import java.util.Map;

import com.czelink.common.intg.entities.User;

public interface UserManagementService {

	public boolean addNewUser(final User user, final Map context);

	public boolean activateNewUser(final String registerUid, final Map context);

	public boolean modifyUser(final User user, final Map context);

	public boolean removeUser(final User user, final Map context);

	public User getUserDetail(final User user, final Map context);
}
