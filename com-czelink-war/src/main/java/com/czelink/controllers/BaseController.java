package com.czelink.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.common.intg.constants.CommonConstants;
import com.czelink.server.base.support.ConversationManager;
import com.czelink.server.base.support.ConversationManager.ConversationTask;
import com.czelink.uploadrepo.intg.UploadRepository;
import com.czelink.utils.ComponentAvailabilityHook;
import com.czelink.utils.ComponentAvailabilityHook.ComponentAvailabilityResult;

@Controller
public class BaseController {

	@Resource(name = "uploadConversationManager")
	private ConversationManager uploadConversationManager;

	@Resource(name = "uploadRepository")
	private UploadRepository uploadRepository;

	@Resource(name = "navigationMessageSource")
	private MessageSource navigationMessageSource;

	@Resource(name = "navigationLabelMessageSource")
	private MessageSource navigationLabelMessageSource;

	@Resource(name = "componentAvailabilityHook")
	private ComponentAvailabilityHook componentAvailabilityHook;

	@RequestMapping("/navigationList")
	public @ResponseBody
	String getNavigationList(final HttpSession session) {
		final JSONObject result = new JSONObject();

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
			result.put("status", true);
			final String[] navArr = navStr.split(",");
			final JSONArray navigationList = new JSONArray();
			for (int i = 0; i < navArr.length; i++) {
				final String labelCode = StringUtils.substring(navArr[i], 1);
				final String label = this.navigationLabelMessageSource
						.getMessage(labelCode, null, Locale.getDefault());

				final JSONObject navItem = new JSONObject();
				navItem.put("hashLink", navArr[i]);
				navItem.put("label", label);
				navigationList.add(navItem);
			}
			result.put("navigationList", navigationList);
			result.put("role", role);
		} else {
			throw new IllegalStateException(
					"invalid security or navigation information provided in navigation.properites or hooks.properites.");
		}

		return result.toString();
	}

	@RequestMapping("/startUploadConversation")
	public @ResponseBody
	String startUploadConversation() {
		final String uid = this.uploadConversationManager.startConversation();
		final JSONObject result = new JSONObject();
		result.put("uid", uid);
		this.uploadConversationManager.setConversationOnCompleteTask(uid,
				new UploadConverstionTask(uid, this.uploadConversationManager));
		return result.toString();
	}

	@RequestMapping("/endUploadConversation")
	public @ResponseBody
	String endUploadConversation(
			@RequestParam("conversation-id") final String conversationID) {
		this.uploadConversationManager.endConversation(conversationID);
		final JSONObject result = new JSONObject();
		result.put("status", true);
		return result.toString();
	}

	@RequestMapping("/completeUploadConversation")
	public @ResponseBody
	String completeUploadConversation(
			@RequestParam("conversation-id") final String conversationID) {
		this.uploadConversationManager.completeConversation(conversationID);
		final JSONObject result = new JSONObject();
		result.put("status", true);
		return result.toString();
	}

	@RequestMapping("/fileupload")
	public @ResponseBody
	String uploadFileToRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final MultipartFile file) {
		final boolean result = this.uploadConversationManager
				.putIntoConversation(conversationID,
						file.getOriginalFilename(), file);
		final JSONObject json = new JSONObject();
		if (result) {
			json.put("src",
					this.uploadRepository.getRepositoryContextPath() + "/imgs/"
							+ conversationID + "/" + file.getOriginalFilename());
		} else {
			throw new IllegalStateException("invalid conversationID: "
					+ conversationID);
		}
		json.put("status", result);
		return json.toString();
	}

	@RequestMapping("/cancelupload")
	public @ResponseBody
	String uploadFileToRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final String fileName) {
		final boolean result = this.uploadConversationManager
				.removeFromConversation(conversationID, fileName);
		if (!result) {
			throw new IllegalStateException("invalid conversationID: "
					+ conversationID);
		}
		final JSONObject json = new JSONObject();
		json.put("status", result);
		return json.toString();
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
				e.printStackTrace();
			}
		}
	}
}
