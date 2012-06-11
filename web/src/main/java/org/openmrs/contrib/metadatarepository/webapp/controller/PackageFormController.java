/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.contrib.metadatarepository.webapp.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.contrib.metadatarepository.model.MetadataPackage;
import org.openmrs.contrib.metadatarepository.service.PackageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/packageform*")
public class PackageFormController extends BaseFormController {
	
	private PackageManager packageManager = null;
	//PackageDao packageDao;
	
	@Autowired
	public void setPackageManager(PackageManager packageManager) {
		this.packageManager = packageManager;
	}
	
	/*@Autowired
	public PackageDao getPackageDao() {
		return packageDao;
	}

	@Autowired
	public void setPackageDao(PackageDao packageDao) {
		this.packageDao = packageDao;
	}*/

	public PackageFormController() {
		setCancelView("redirect:mainMenu");
		setSuccessView("uploadDisplay");
	}
	
	@ModelAttribute
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	protected MetadataPackage showForm(@RequestParam(required = false) final Long id) throws Exception {
		
		if (id != null) {
			return packageManager.get(id);
		}
		
		return new MetadataPackage();
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(MetadataPackage pkg, BindingResult errors, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		if (request.getParameter("cancel") != null) {
			return getCancelView();
		}
		
		log.debug("entering 'onSubmit' method...");
		
		boolean isNew = (pkg.getId() == null);
		String success = getSuccessView();
		Locale locale = request.getLocale();
		
		if (request.getParameter("delete") != null) {
			packageManager.remove(pkg.getId());
			saveMessage(request, getText("package.deleted", locale));
		} else {
			packageManager.save(pkg);
			String key = (isNew) ? "package.added" : "package.updated";
			saveMessage(request, getText(key, locale));
			
			if (!isNew) {
				success = "redirect:packageform?id=" + pkg.getId();
			}
		}
		 /*log.debug(request.getSession().getAttribute("filename"));
		MetadataPackage metadataPackage = packageDao.get(request.getSession().getAttribute("filename"));
		metadataPackage.setDescription(request.getParameter("pkgDescription"));
		metadataPackage.setName(request.getParameter("pkgName"));
		metadataPackage.setVersion(request.getParameter("pkgVersion"));
		packageDao.save(metadataPackage);*/
         request.setAttribute("pkgName", pkg.getName());
		 request.setAttribute("pkgDescription", pkg.getDescription());
		 request.setAttribute("pkgVersion",pkg.getVersion());
	      
		return success;
	}
	
}