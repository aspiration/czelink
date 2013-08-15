package com.czelink.infomgmt.controllers;

import java.util.List;

import javax.annotation.Resource;

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

		for (int i = 0; i < infoArticle.size(); i++) {
			System.out.println(infoArticle.get(i).getTitle());
		}

		return "From infomgmt!";
	}

	public InformationManagementService getInformationManagementService() {
		return informationManagementService;
	}

	public void setInformationManagementService(
			InformationManagementService informationManagementService) {
		this.informationManagementService = informationManagementService;
	}

}
