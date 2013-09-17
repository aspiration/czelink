package com.czelink.usermgmt.dbaccess;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

import com.czelink.common.intg.entities.User;
import com.czelink.dbaccess.LdapOperationsAware;
import com.czelink.dbaccess.MongoOperationsAware;
import com.czelink.usermgmt.intg.services.UserManagementService;

public class UserManagementServiceImpl implements UserManagementService,
		LdapOperationsAware, MongoOperationsAware {

	private LdapOperations ldapOperations;

	private MongoOperations mongoOperations;

	public boolean addNewUser(User user) {

		boolean result = false;

		try {
			final DistinguishedName distinguisedName = new DistinguishedName();
			distinguisedName.add("cn", user.getUsername());

			final Attributes userAttributes = new BasicAttributes();
			userAttributes.put("sn", user.getDisplayName());
			userAttributes.put("mail", user.getUsername());
			userAttributes.put("uid", user.getUsername());
			userAttributes.put("userPassword", user.getPassword());

			final BasicAttribute classAttribute = new BasicAttribute(
					"objectclass");
			classAttribute.add("top");
			classAttribute.add("person");
			classAttribute.add("inetOrgPerson");
			classAttribute.add("organizationalPerson");
			userAttributes.put(classAttribute);

			this.ldapOperations.bind(distinguisedName, null, userAttributes);

			// will not store user password in MongoDB.
			user.setPassword(StringUtils.EMPTY);
			this.mongoOperations.insert(user);

			result = true;
		} catch (final Throwable th) {
			// TODO: to add log here.
			th.printStackTrace();
			result = false;
		}

		return result;
	}

	public boolean ModifyUser(User user) {

		boolean result = false;

		try {
			final DistinguishedName distinguisedName = new DistinguishedName();
			distinguisedName.add("cn", user.getUsername());

			final Attribute displayNameAttribute = new BasicAttribute(
					"displayName", user.getDisplayName());
			final ModificationItem displayNameItem = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, displayNameAttribute);

			final Attribute organizationNameAttribute = new BasicAttribute(
					"organizationName", user.getCompany());
			final ModificationItem organizationNameItem = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, organizationNameAttribute);

			final Attribute titleAttribute = new BasicAttribute("title",
					user.getCompany());
			final ModificationItem titleItem = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, titleAttribute);

			final String eol = System.getProperty("line.separator");
			final String descrptionString = StringUtils.join(
					user.getExperiences(), eol);
			final Attribute descriptionAttribute = new BasicAttribute(
					"description", descrptionString);
			final ModificationItem descriptionItem = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, descriptionAttribute);

			if (StringUtils.isNotBlank(user.getPassword())) {
				final Attribute passwordAttribute = new BasicAttribute(
						"userPassword", user.getPassword());
				final ModificationItem passwordItem = new ModificationItem(
						DirContext.REPLACE_ATTRIBUTE, passwordAttribute);

				this.ldapOperations.modifyAttributes(distinguisedName,
						new ModificationItem[] { passwordItem, displayNameItem,
								organizationNameItem, titleItem,
								descriptionItem });
			} else {
				this.ldapOperations.modifyAttributes(distinguisedName,
						new ModificationItem[] { displayNameItem,
								organizationNameItem, titleItem,
								descriptionItem });
			}

			// will not store user password in MongoDB.
			user.setPassword(StringUtils.EMPTY);
			this.mongoOperations.save(user);

			result = true;
		} catch (final Throwable th) {
			// TODO: to add log
			th.printStackTrace();
			result = false;
		}

		return result;
	}

	public boolean removeUser(User user) {
		// TODO Not required at this moment.
		return false;
	}

	public User getUserDetail(User user) {

		final BasicQuery query = new BasicQuery("{ username : "
				+ user.getUsername() + " }");
		final User result = this.mongoOperations.findOne(query, User.class);

		return result;
	}

	public void setLdapOperations(final LdapOperations pLdapOperations) {
		this.ldapOperations = pLdapOperations;
	}

	public void setMongoOperations(final MongoOperations pMongoOperations) {
		this.mongoOperations = pMongoOperations;
	}
}
