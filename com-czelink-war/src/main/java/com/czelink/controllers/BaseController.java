package com.czelink.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.uploadrepo.intg.UploadRepository;

@Controller
@SessionAttributes("conversations")
public class BaseController {

	@Resource(name = "uploadRepository")
	private UploadRepository uploadRepository;

	@ModelAttribute("conversations")
	public Map<String, Map<String, Object>> createConversationHolder() {
		return new HashMap<String, Map<String, Object>>();
	}

	@RequestMapping("/startConversation")
	public @ResponseBody
	String startConversation(
			@ModelAttribute("conversations") final Map<String, Map<String, Object>> conversations) {
		final String uid = UUID.randomUUID().toString();
		final JSONObject result = new JSONObject();
		result.put("uid", uid);
		conversations.put(uid, new HashMap());
		return result.toString();
	}

	@RequestMapping("/endConversation")
	public @ResponseBody
	String endConversation(
			@ModelAttribute("conversations") final Map<String, Map<String, Object>> conversations,
			@RequestParam final String conversationID) {
		conversations.remove(conversationID);
		final JSONObject result = new JSONObject();
		result.put("status", true);
		return result.toString();
	}

	@RequestMapping("/fileupload")
	public @ResponseBody
	String uploadFileToRepository(
			@RequestHeader("conversation-id") final String conversationID,
			@RequestParam final MultipartFile file,
			@ModelAttribute("conversations") final Map<String, Map<String, Object>> conversations) {
		final Map<String, Object> conversation = conversations
				.get(conversationID);
		// TODO : do stuffs here.
		return "Hello world!";
	}

	public UploadRepository getUploadRepository() {
		return uploadRepository;
	}

	public void setUploadRepository(UploadRepository uploadRepository) {
		this.uploadRepository = uploadRepository;
	}

}
