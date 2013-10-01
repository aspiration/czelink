package com.czelink.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.czelink.server.base.support.ConversationManager.ConversationTask;
import com.czelink.utils.ComponentAvailabilityHook;
import com.czelink.utils.ComponentAvailabilityHook.ComponentAvailabilityResult;
import com.czelink.uploadrepo.intg.UploadRepository;

@Controller
public class BaseController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(name = "uploadConversationManager")
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

	@RequestMapping(value = "/getCurrentRole", produces = "application/json")
	@ResponseBody
	NavigationListViewBean getCurrentRole(final HttpSession session) {
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

	@RequestMapping(value = "/startUploadConversation", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean startUploadConversation() {
		final String uid = this.uploadConversationManager.startConversation();
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setUid(uid);
		response.setStatus(true);
		this.uploadConversationManager.setConversationOnCompleteTask(uid,
				new UploadConverstionTask(uid, this.uploadConversationManager));
		return response;
	}

	@RequestMapping(value = "/endUploadConversation", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean endUploadConversation(
			@RequestParam("conversation-id") final String conversationID) {
		this.uploadConversationManager.endConversation(conversationID);
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(true);
		return response;
	}

	@RequestMapping(value = "/completeUploadConversation", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean completeUploadConversation(
			@RequestParam("conversation-id") final String conversationID) {
		this.uploadConversationManager.completeConversation(conversationID);
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(true);
		return response;
	}

	@RequestMapping(value = "/fileupload", produces = "application/json")
	public @ResponseBody
	FileUploadViewBean uploadFileToRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final MultipartFile file) {
		final boolean result = this.uploadConversationManager
				.putIntoConversation(conversationID,
						file.getOriginalFilename(), file);
		final FileUploadViewBean response = new FileUploadViewBean();
		if (result) {
			response.setSrc(this.uploadRepository.getRepositoryContextPath()
					+ "/imgs/" + conversationID + "/"
					+ file.getOriginalFilename());
		} else {
			throw new IllegalStateException("invalid conversationID: "
					+ conversationID);
		}
		response.setStatus(result);
		return response;
	}

	@RequestMapping(value = "/cancelupload", produces = "application/json")
	public @ResponseBody
	JsonBaseViewBean uploadFileToRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final String fileName) {
		final boolean result = this.uploadConversationManager
				.removeFromConversation(conversationID, fileName);
		if (!result) {
			throw new IllegalStateException("invalid conversationID: "
					+ conversationID);
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

	private class UploadConverstionTask extends ConversationTask {

		private static final long serialVersionUID = 1L;

		public UploadConverstionTask(String pConversationId,
				ConversationManager pConversationManager) {
			super(pConversationId, pConversationManager);
		}

		@Override
		public void run() {
			// persist the uploaded file to disk.
			final Map<String, MultipartFile> conversation = this
					.getConversation(MultipartFile.class);
			try {
				getUploadRepository().saveFile(conversation,
						"imgs/" + this.conversationId);
			} catch (Exception e) {
				throw new IllegalStateException("[uploadFile: "
						+ e.getMessage() + "]", e);
			}
		}
	}
}
