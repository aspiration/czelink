package com.czelink.usermgmt.dbaccess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.internet.MimeMessage;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.czelink.common.intg.entities.User;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;
import com.czelink.usermgmt.intg.services.UserManagementService;

public class UserManagementServiceImpl implements UserManagementService,
		Serializable {

	private static final long serialVersionUID = 1L;

	private static transient final ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	static {
		UserManagementServiceImpl.passwordEncoder.setEncodeHashAsBase64(true);
	}

	private transient LdapOperations ldapOperations;

	private transient MongoOperations mongoOperations;

	private transient RedisOperations<Object, Object> redisOperations;

	private transient JavaMailSender mailSender;

	private transient VelocityEngine velocityEngine;

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
			// step1: remove potential legacy in redis.
			// remove potential legacy from redis.
			final String username = user.getUsername();
			final String verfiyId = (String) this.redisOperations.opsForValue()
					.get(username);
			if (StringUtils.isNotBlank(verfiyId)) {
				this.redisOperations.delete(verfiyId);
			}

			this.redisOperations.delete(username);
			this.redisOperations.delete(username + "_pass");
			this.redisOperations.delete(username + "_display");

			try {
				// step2: check if user exists
				if (null == this.getUserDetail(user, context)) {

					// step3: record in Redis and send mail.
					final String uid = UUID.randomUUID().toString();
					this.redisOperations.opsForValue().set(uid,
							user.getUsername());
					this.redisOperations.opsForValue().set(user.getUsername(),
							uid);
					this.redisOperations.opsForValue().set(
							user.getUsername() + "_display",
							user.getDisplayName());
					this.redisOperations.opsForValue().set(
							user.getUsername() + "_pass", user.getPassword());

					final MimeMessage message = this.mailSender
							.createMimeMessage();
					final MimeMessageHelper helper = new MimeMessageHelper(
							message, false, "UTF-8");
					helper.setTo(user.getUsername());
					helper.setFrom("czelink.com");

					final Map model = new HashMap();
					model.put("username", user.getDisplayName());
					final String activatelink = ((String) context
							.get(UsermgmtConstants.ACTIVATE_URL_KEY))
							.concat("?uid=" + uid);
					model.put("activatelink", activatelink);
					String text = VelocityEngineUtils.mergeTemplateIntoString(
							this.getVelocityEngine(),
							"external/template/mail.vm", "UTF-8", model);
					helper.setText(text, true);
					helper.setSubject("感谢注册财智网，点击链接激活账户");

					this.mailSender.send(message);

					result = true;
				} else {
					// user exists.
					result = false;

					// EORROR_MSG_CDE: 002, user registered.
					context.put(UsermgmtConstants.EORROR_MSG_CDE, "002");
				}

			} catch (final Throwable th) {
				result = false;
				// EORROR_MSG_CDE: 008, unknown issue, fatal error.
				context.put(UsermgmtConstants.EORROR_MSG_CDE, "008");

				// remove potential legacy in redis.
				final String uid = (String) this.redisOperations.opsForValue()
						.get(username);
				if (StringUtils.isNotBlank(uid)) {
					this.redisOperations.delete(uid);
				}
				this.redisOperations.delete(username);
				this.redisOperations.delete(username + "_pass");
				this.redisOperations.delete(username + "_display");

				throw new IllegalStateException("[usermgmt: " + th.getMessage()
						+ "]", th);
			}
		} catch (Throwable e) {
			result = false;
			// EORROR_MSG_CDE: 012, Memory database connection issue.
			context.put(UsermgmtConstants.EORROR_MSG_CDE, "012");

			throw new IllegalStateException("[usermgmt: " + e.getMessage()
					+ "]", e);
		}

		return result;
	}

	public boolean activateNewUser(final String registerUid, final Map context) {

		boolean result = false;

		try {
			final String username = (String) this.redisOperations.opsForValue()
					.get(registerUid);
			if (StringUtils.isNotBlank(username)) {
				final String verfiyId = (String) this.redisOperations
						.opsForValue().get(username);
				final String displayName = (String) this.redisOperations
						.opsForValue().get(username + "_display");
				final String password = (String) this.redisOperations
						.opsForValue().get(username + "_pass");
				// remove from data store.
				this.redisOperations.delete(registerUid);
				if (StringUtils.isNotBlank(username)) {
					this.redisOperations.delete(username);
					this.redisOperations.delete(username + "_pass");
					this.redisOperations.delete(username + "_display");
				}
				if (StringUtils.isNotBlank(verfiyId)) {
					this.redisOperations.delete(verfiyId);
				}

				if (StringUtils.isNotBlank(username)
						&& StringUtils.isNotBlank(verfiyId)
						&& verfiyId.equals(registerUid)
						&& StringUtils.isNotBlank(password)) {

					User user = new User();
					user.setUsername(username);
					user.setPassword(password);
					user.setDisplayName(displayName);
					user.setActivated(true);

					context.put(UsermgmtConstants.USER_NAME, user.getUsername());

					try {
						// step1: process on LDAP.
						final DistinguishedName distinguisedName = new DistinguishedName();
						distinguisedName.add("ou", "users");
						distinguisedName.add("uid", user.getUsername());

						final Attributes userAttributes = new BasicAttributes();
						userAttributes
								.put("displayName", user.getDisplayName());
						userAttributes.put("sn", user.getUsername());
						userAttributes.put("mail", user.getUsername());
						userAttributes.put("cn", user.getUsername());
						userAttributes.put("userPassword",
								encryptLdapPassword(user.getPassword()));

						final BasicAttribute classAttribute = new BasicAttribute(
								"objectclass");
						classAttribute.add("top");
						classAttribute.add("person");
						classAttribute.add("inetOrgPerson");
						classAttribute.add("organizationalPerson");
						userAttributes.put(classAttribute);

						this.ldapOperations.bind(distinguisedName, null,
								userAttributes);

						final DirContextAdapter dirContextAdapter = (DirContextAdapter) this.ldapOperations
								.lookup(distinguisedName);
						final String fullName = dirContextAdapter
								.getNameInNamespace();
						this.addToUserRole(fullName);

						// will not store user password in MongoDB.
						user.setPassword(StringUtils.EMPTY);

						// step2: process on MongoDB.
						this.mongoOperations.insert(user);

						result = true;
					} catch (final Throwable th) {
						result = false;

						if (th instanceof NameAlreadyBoundException) {
							// EORROR_MSG_CDE: 002, user registered.
							context.put(UsermgmtConstants.EORROR_MSG_CDE, "002");
						} else {
							// EORROR_MSG_CDE: 008, unknown issue, fatal error.
							context.put(UsermgmtConstants.EORROR_MSG_CDE, "008");
						}

						throw new IllegalStateException("[usermgmt: "
								+ th.getMessage() + "]", th);
					}
				}
			} else {
				result = false;
				// EORROR_MSG_CDE: out-dated user activate transaction id.
				context.put(UsermgmtConstants.EORROR_MSG_CDE, "012");
			}
		} catch (Throwable e) {
			result = false;
			// EORROR_MSG_CDE: 012, Memory database connection issue.
			context.put(UsermgmtConstants.EORROR_MSG_CDE, "012");

			throw new IllegalStateException("[usermgmt: " + e.getMessage()
					+ "]", e);
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
			// EORROR_MSG_CDE: 008, unknown issue, fatal error.
			context.put(UsermgmtConstants.EORROR_MSG_CDE, "008");
			result = false;

			throw new IllegalStateException("[usermgmt: " + th.getMessage()
					+ "]", th);
		}

		return result;
	}

	public boolean removeUser(final User user, final Map context) {
		boolean result = false;
		try {
			final User target = this.getUserDetail(user, context);
			target.setActivated(false);
			this.mongoOperations.save(target);
			result = true;
		} catch (Throwable th) {
			// EORROR_MSG_CDE: 010, application database connection issue.
			context.put(UsermgmtConstants.EORROR_MSG_CDE, "010");
			result = false;

			throw new IllegalStateException("[usermgmt: " + th.getMessage()
					+ "]", th);
		}
		return result;
	}

	public User getUserDetail(final User user, final Map context) {

		final BasicQuery query = new BasicQuery("{ username : '"
				+ user.getUsername() + "' }");
		final User result = this.mongoOperations.findOne(query, User.class);

		return result;
	}

	public void setLdapOperations(final LdapOperations pLdapOperations) {
		this.ldapOperations = pLdapOperations;
	}

	public void setMongoOperations(final MongoOperations pMongoOperations) {
		this.mongoOperations = pMongoOperations;
	}

	public void setMailSender(final JavaMailSender pMailSender) {
		this.mailSender = pMailSender;
	}

	public void setRedisOperations(
			RedisOperations<Object, Object> redisOperations) {
		this.redisOperations = redisOperations;
	}

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}
}
