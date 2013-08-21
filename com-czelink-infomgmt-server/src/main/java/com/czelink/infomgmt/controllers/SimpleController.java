package com.czelink.infomgmt.controllers;

import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.infomgmt.intg.services.InformationManagementService;

@Controller
public class SimpleController {

	@Resource(name = "infomgmtService")
	private InformationManagementService informationManagementService;

	@RequestMapping("/simple")
	public @ResponseBody
	String simple() {
		final List<InfoArticle> infoArticle = this.informationManagementService
				.retrieveLatestInformationList(5);

		final JSONObject result = new JSONObject();

		final JSONArray array = new JSONArray();
		for (int i = 0; i < infoArticle.size(); i++) {
			final JSONObject infoContent = new JSONObject();
			infoContent.put("id", infoArticle.get(i).getId());
			infoContent.put("title", infoArticle.get(i).getTitle());
			array.add(infoContent);
		}

		result.put("contents", array);

		return result.toString();
	}

	public InformationManagementService getInformationManagementService() {
		return informationManagementService;
	}

	public void setInformationManagementService(
			InformationManagementService informationManagementService) {
		this.informationManagementService = informationManagementService;
	}

}
