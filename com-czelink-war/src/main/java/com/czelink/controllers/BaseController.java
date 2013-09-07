package com.czelink.controllers;

import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.server.base.support.ConversationManager;
import com.czelink.server.base.support.ConversationManager.ConversationTask;
import com.czelink.uploadrepo.intg.UploadRepository;

@Controller
public class BaseController {

	@Resource(name = "uploadConversationManager")
	private ConversationManager uploadConversationManager;

	@Resource(name = "uploadRepository")
	private UploadRepository uploadRepository;

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
