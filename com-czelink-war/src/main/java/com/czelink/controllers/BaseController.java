package com.czelink.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.beans.FileUploadViewBean;
import com.czelink.beans.NavigationItemViewModel;
import com.czelink.beans.NavigationListViewBean;
import com.czelink.common.intg.constants.CommonConstants;
import com.czelink.server.base.beans.JsonBaseViewBean;
import com.czelink.server.base.support.ConversationManager;
import com.czelink.server.base.support.ConversationTask;
import com.czelink.utils.ComponentAvailabilityHook;
import com.czelink.utils.ComponentAvailabilityHook.ComponentAvailabilityResult;
import com.czelink.uploadrepo.intg.UploadRepository;

@Controller
public class BaseController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String UPLOAD_CONVERSATION_GROUP = "upload_conversation_group";

	@Resource(name = "conversationManager")
	private transient ConversationManager uploadConversationManager;

	@Resource(name = "uploadRepository")
	private transient UploadRepository uploadRepository;

	@Resource(name = "navigationMessageSource")
	private transient MessageSource navigationMessageSource;

	@Resource(name = "navigationLabelMessageSource")
	private transient MessageSource navigationLabelMessageSource;

	@Resource(name = "componentAvailabilityHook")
	private transient ComponentAvailabilityHook componentAvailabilityHook;

	@Resource(name = "redisOperations")
	private transient RedisOperations<Object, Object> redisOperations;

	@RequestMapping(value = "/getCurrentRole", produces = "application/json")
	@ResponseBody
	public NavigationListViewBean getCurrentRole(final HttpSession session) {
		final NavigationListViewBean response = new NavigationListViewBean();

		final List<String> rolesList = (List<String>) session
				.getAttribute(CommonConstants.ROLE_LIST_IN_SESSION_KEY);

		String role = CommonConstants.ROLE_ANONYMOUS;

		if (null != rolesList) {
			if (rolesList.contains(CommonConstants.ROLE_ADMIN)) {
				role = CommonConstants.ROLE_ADMIN;
			} else if (rolesList.contains(CommonConstants.ROLE_USER)) {
				role = CommonConstants.ROLE_USER;
			} else {
				role = CommonConstants.ROLE_ANONYMOUS;
			}
		} else {
			if (componentAvailabilityHook.checkIfComponentAvailable("usermgmt")
					.equals(ComponentAvailabilityResult.AVAILABLE)) {
				role = CommonConstants.ROLE_ANONYMOUS;
			} else if (componentAvailabilityHook.checkIfComponentAvailable(
					"usermgmt").equals(ComponentAvailabilityResult.UNAVAILABLE)) {
				role = CommonConstants.ROLE_ADMIN;
			} else {
				role = CommonConstants.ROLE_ANONYMOUS;
			}
		}
		response.setRole(role);

		return response;
	}

	@RequestMapping(value = "/navigationList", produces = "application/json")
	public @ResponseBody
	NavigationListViewBean getNavigationList(
			final HttpSession session,
			@RequestParam(value = "activateInstance") final String activateInstance) {

		final NavigationListViewBean response = new NavigationListViewBean();

		final List<String> rolesList = (List<String>) session
				.getAttribute(CommonConstants.ROLE_LIST_IN_SESSION_KEY);

		String navStr = StringUtils.EMPTY;
		String role = CommonConstants.ROLE_ANONYMOUS;
		if (null != rolesList) {
			if (rolesList.contains(CommonConstants.ROLE_ADMIN)) {
				navStr = this.navigationMessageSource.getMessage(
						CommonConstants.ROLE_ADMIN, null, Locale.getDefault());
				role = CommonConstants.ROLE_ADMIN;
			} else if (rolesList.contains(CommonConstants.ROLE_USER)) {
				navStr = this.navigationMessageSource.getMessage(
						CommonConstants.ROLE_USER, null, Locale.getDefault());
				role = CommonConstants.ROLE_USER;
			} else {
				navStr = this.navigationMessageSource.getMessage(
						CommonConstants.ROLE_ANONYMOUS, null,
						Locale.getDefault());
				role = CommonConstants.ROLE_ANONYMOUS;
			}
		} else {
			if (componentAvailabilityHook.checkIfComponentAvailable("usermgmt")
					.equals(ComponentAvailabilityResult.AVAILABLE)) {
				navStr = this.navigationMessageSource.getMessage(
						CommonConstants.ROLE_ANONYMOUS, null,
						Locale.getDefault());
				role = CommonConstants.ROLE_ANONYMOUS;
			} else if (componentAvailabilityHook.checkIfComponentAvailable(
					"usermgmt").equals(ComponentAvailabilityResult.UNAVAILABLE)) {
				navStr = this.navigationMessageSource.getMessage(
						CommonConstants.ROLE_ADMIN, null, Locale.getDefault());
				role = CommonConstants.ROLE_ADMIN;
			} else {
				navStr = StringUtils.EMPTY;
				role = CommonConstants.ROLE_ANONYMOUS;
			}
		}
		if (StringUtils.isNotBlank(navStr)) {
			response.setStatus(true);
			final String[] navArr = navStr.split(",");
			final List<NavigationItemViewModel> navigationList = new ArrayList<NavigationItemViewModel>();
			for (int i = 0; i < navArr.length; i++) {
				final String labelCode = StringUtils.substring(navArr[i], 1);
				final String label = this.navigationLabelMessageSource
						.getMessage(labelCode, null, Locale.getDefault());

				final NavigationItemViewModel navItem = new NavigationItemViewModel();
				navItem.setHashLink(navArr[i]);
				navItem.setLabel(label);
				navigationList.add(navItem);
			}

			response.setNavigationList(navigationList);
			response.setRole(role);
		} else {
			throw new IllegalStateException(
					"invalid security or navigation information provided in navigation.properites or hooks.properites.");
		}

		if (StringUtils.isNotBlank(activateInstance)) {
			final String verifyKey = (String) this.redisOperations
					.opsForValue().get(activateInstance);
			if (StringUtils.isNotBlank(verifyKey)) {
				response.setVerifyKey(verifyKey);
			}
		}

		return response;
	}

	@RequestMapping(value = "/startUploadConversation", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean startUploadConversation() {
		final String uid = this.uploadConversationManager.startConversation(
				BaseController.UPLOAD_CONVERSATION_GROUP, false);
		this.uploadConversationManager.setConversatioinOnEndTask(
				BaseController.UPLOAD_CONVERSATION_GROUP, uid,
				new ConversationTask(BaseController.UPLOAD_CONVERSATION_GROUP,
						uid, this.uploadConversationManager) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onRun() {
						final String completePath = uploadRepository
								.getRepositoryAbsolutePath()
								+ "/imgs/"
								+ BaseController.UPLOAD_CONVERSATION_GROUP;
						final String fileName = uid;
						final boolean result = uploadRepository
								.deleteFile(
										fileName,
										"imgs/"
												+ BaseController.UPLOAD_CONVERSATION_GROUP);
						if (!result) {
							throw new IllegalStateException(
									"fail delete the conversation folder as: "
											+ completePath + "/" + uid);
						}
					}

				});
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setUid(uid);
		response.setStatus(true);
		return response;
	}

	@RequestMapping(value = "/endUploadConversation", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean endUploadConversation(
			@RequestParam("conversation-id") final String conversationID) {
		this.uploadConversationManager.endConversation(
				BaseController.UPLOAD_CONVERSATION_GROUP, conversationID);
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(true);
		return response;
	}

	@RequestMapping(value = "/completeUploadConversation", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean completeUploadConversation(
			@RequestParam("conversation-id") final String conversationID) {
		this.uploadConversationManager.completeConversation(
				BaseController.UPLOAD_CONVERSATION_GROUP, conversationID);
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(true);
		return response;
	}

	@RequestMapping(value = "/fileupload", produces = "application/json")
	public @ResponseBody
	FileUploadViewBean uploadFileToRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final MultipartFile file) {
		final FileUploadViewBean response = new FileUploadViewBean();

		final String filePath = this.uploadRepository
				.getRepositoryAbsolutePath()
				+ "/imgs/"
				+ BaseController.UPLOAD_CONVERSATION_GROUP
				+ "/"
				+ conversationID + "/" + file.getOriginalFilename();
		final String fileSrc = this.uploadRepository.getRepositoryContextPath()
				+ "/imgs/" + BaseController.UPLOAD_CONVERSATION_GROUP + "/"
				+ conversationID + "/";

		boolean result = false;

		try {
			result = this.uploadRepository.saveFile(file, "imgs/"
					+ BaseController.UPLOAD_CONVERSATION_GROUP + "/"
					+ conversationID);
			if (result) {
				this.uploadConversationManager.putIntoConversation(
						BaseController.UPLOAD_CONVERSATION_GROUP,
						conversationID, file.getOriginalFilename(), filePath);
				response.setSrc(fileSrc);
			}
		} catch (Exception e) {
			result = false;
			throw new IllegalStateException("File '"
					+ file.getOriginalFilename()
					+ "' upload fail with conversationID: " + conversationID);
		}
		response.setStatus(result);
		return response;
	}

	@RequestMapping(value = "/cancelupload", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean cancelUploadFromRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final String fileName) {
		final String targetSrc = (String) this.uploadConversationManager
				.getFromConversation(BaseController.UPLOAD_CONVERSATION_GROUP,
						conversationID, fileName);
		boolean result = false;
		if (null != targetSrc) {
			boolean deleteStatus = this.uploadRepository.deleteFile(fileName,
					"imgs/" + BaseController.UPLOAD_CONVERSATION_GROUP + "/"
							+ conversationID);
			if (deleteStatus) {
				result = this.uploadConversationManager.removeFromConversation(
						BaseController.UPLOAD_CONVERSATION_GROUP,
						conversationID, fileName);
			}
		}
		if (!result) {
			throw new IllegalStateException("fail cancelling upload for file '"
					+ fileName + "' with conversationID: " + conversationID);
		}
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(true);
		return response;
	}

	public ConversationManager getUploadConversationManager() {
		return uploadConversationManager;
	}

	public void setUploadConversationManager(
			ConversationManager uploadConversationManager) {
		this.uploadConversationManager = uploadConversationManager;
	}

	public UploadRepository getUploadRepository() {
		return uploadRepository;
	}

	public void setUploadRepository(UploadRepository uploadRepository) {
		this.uploadRepository = uploadRepository;
	}

	public MessageSource getNavigationMessageSource() {
		return navigationMessageSource;
	}

	public void setNavigationMessageSource(MessageSource navigationMessageSource) {
		this.navigationMessageSource = navigationMessageSource;
	}

	public ComponentAvailabilityHook getComponentAvailabilityHook() {
		return componentAvailabilityHook;
	}

	public void setComponentAvailabilityHook(
			ComponentAvailabilityHook componentAvailabilityHook) {
		this.componentAvailabilityHook = componentAvailabilityHook;
	}

	public MessageSource getNavigationLabelMessageSource() {
		return navigationLabelMessageSource;
	}

	public void setNavigationLabelMessageSource(
			MessageSource navigationLabelMessageSource) {
		this.navigationLabelMessageSource = navigationLabelMessageSource;
	}
}
