package com.czelink.usermgmt.dbaccess;

import java.util.Map;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.czelink.common.intg.entities.User;
import com.czelink.dbaccess.LdapOperationsAware;
import com.czelink.dbaccess.MongoOperationsAware;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;
import com.czelink.usermgmt.intg.services.UserManagementService;

public class UserManagementServiceImpl implements UserManagementService,
		LdapOperationsAware, MongoOperationsAware {

	private static final ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	static {
		UserManagementServiceImpl.passwordEncoder.setEncodeHashAsBase64(true);
	}

	private LdapOperations ldapOperations;

	private MongoOperations mongoOperations;

	private String encryptLdapPassword(final String password) {

		final StringBuilder builder = new StringBuilder();
		builder.append("{sha}");
		builder.append(UserManagementServiceImpl.passwordEncoder
				.encodePassword(password, null));

		return builder.toString();
	}

	private void addToUserRole(final String fullName) {
		final DistinguishedName distinguisedName = new DistinguishedName();
		distinguisedName.add("ou", "roles");
		distinguisedName.add("cn", "user");

		final Attribute uniqueMemberAttribue = new BasicAttribute(
				"uniqueMember", fullName);
		final ModificationItem uniqueMemberItem = new ModificationItem(
				DirContext.ADD_ATTRIBUTE, uniqueMemberAttribue);

		this.ldapOperations.modifyAttributes(distinguisedName,
				new ModificationItem[] { uniqueMemberItem });
	}

	public boolean addNewUser(final User user, final Map context) {

		boolean result = false;

		try {
			final DistinguishedName distinguisedName = new DistinguishedName();
			distinguisedName.add("ou", "users");
			distinguisedName.add("uid", user.getUsername());

			final Attributes userAttributes = new BasicAttributes();
			userAttributes.put("sn", user.getUsername());
			userAttributes.put("mail", user.getUsername());
			userAttributes.put("cn", user.getUsername());
			userAttributes.put("userPassword",
					encryptLdapPassword(user.getPassword()));
			userAttributes.put("destinationIndicator", "false");

			final BasicAttribute classAttribute = new BasicAttribute(
					"objectclass");
			classAttribute.add("top");
			classAttribute.add("person");
			classAttribute.add("inetOrgPerson");
			classAttribute.add("organizationalPerson");
			userAttributes.put(classAttribute);

			this.ldapOperations.bind(distinguisedName, null, userAttributes);

			final DirContextAdapter dirContextAdapter = (DirContextAdapter) this.ldapOperations
					.lookup(distinguisedName);
			final String fullName = dirContextAdapter.getNameInNamespace();
			this.addToUserRole(fullName);

			// will not store user password in MongoDB.
			user.setPassword(StringUtils.EMPTY);
			this.mongoOperations.insert(user);

			result = true;
		} catch (final Throwable th) {
			// TODO: to add log here.
			th.printStackTrace();

			result = false;
			if (th instanceof NameAlreadyBoundException) {
				// EORROR_MSG_CDE: 002, user registered.
				context.put(UsermgmtConstants.EORROR_MSG_CDE, "002");
			} else {
				// EORROR_MSG_CDE: 008, unknown issue, fatal error.
				context.put(UsermgmtConstants.EORROR_MSG_CDE, "008");
			}
		}

		return result;
	}

	public boolean modifyUser(final User user, final Map context) {

		boolean result = false;

		try {
			final DistinguishedName distinguisedName = new DistinguishedName();
			distinguisedName.add("ou", "users");
			distinguisedName.add("uid", user.getUsername());

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
						"userPassword", encryptLdapPassword(user.getPassword()));
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

	public boolean removeUser(final User user, final Map context) {
		// TODO Not required at this moment.
		return false;
	}

	public User getUserDetail(final User user, final Map context) {

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
