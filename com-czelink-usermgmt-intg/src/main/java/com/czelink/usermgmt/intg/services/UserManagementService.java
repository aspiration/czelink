package com.czelink.usermgmt.intg.services;

import com.czelink.common.intg.entities.User;

public interface UserManagementService {

	public boolean addNewUser(final User user);

	public boolean modifyUser(final User user);

	public boolean removeUser(final User user);

	public User getUserDetail(final User user);
}
