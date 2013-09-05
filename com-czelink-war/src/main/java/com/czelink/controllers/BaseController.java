package com.czelink.controllers;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.uploadrepo.intg.UploadRepository;

@Controller
public class BaseController {

	@Resource(name = "uploadRepository")
	private UploadRepository uploadRepository;

	@RequestMapping("/fileupload")
	public @ResponseBody
	String uploadFileToRepository(final HttpServletRequest request) {
		try {
			// TODO: dummy impl
			System.out.println("fileHash: "
					+ request.getHeader("file-hashcode"));
			this.uploadRepository.saveFile(request.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Hello world!";
	}

	public UploadRepository getUploadRepository() {
		return uploadRepository;
	}

	public void setUploadRepository(UploadRepository uploadRepository) {
		this.uploadRepository = uploadRepository;
	}

}
