package com.czelink.controllers;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.uploadrepo.intg.UploadRepository;

@Controller
public class BaseController {

	@Resource(name = "uploadRepository")
	private UploadRepository uploadRepository;

	@RequestMapping("/showUploadRepositoryPath")
	public @ResponseBody
	String showUploadRepositoryPath() {
		// TODO: dummy impl
		this.uploadRepository.printAbsolutePath();
		return "Hello world!";
	}

	public UploadRepository getUploadRepository() {
		return uploadRepository;
	}

	public void setUploadRepository(UploadRepository uploadRepository) {
		this.uploadRepository = uploadRepository;
	}

}
