package com.czelink.dbaccess;

import org.springframework.ldap.core.LdapOperations;

public interface LdapOperationsAware {

	public void setLdapOperations(final LdapOperations ldapOperations);

}
