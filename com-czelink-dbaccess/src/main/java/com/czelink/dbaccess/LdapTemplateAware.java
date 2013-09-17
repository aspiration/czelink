package com.czelink.dbaccess;

import org.springframework.ldap.core.LdapTemplate;

public interface LdapTemplateAware {

	public void setLdapTemplate(final LdapTemplate ldapTemplate);

}
