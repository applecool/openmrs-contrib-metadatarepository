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

package org.openmrs.contrib.metadatarepository.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import org.openmrs.contrib.metadatarepository.dao.PackageDao;
import org.openmrs.contrib.metadatarepository.model.MetadataPackage;
import org.openmrs.contrib.metadatarepository.model.User;
import org.openmrs.contrib.metadatarepository.service.APIException;
import org.openmrs.contrib.metadatarepository.service.PackageManager;
import org.openmrs.contrib.metadatarepository.service.impl.GenericManagerImpl;
import org.openmrs.contrib.metadatarepository.util.xstream.DateTimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Implementation of PackageManager interface.
 */
@Service("packageManager")
public class PackageManagerImpl extends
		GenericManagerImpl<MetadataPackage, Long> implements PackageManager {

	PackageDao packageDao;
	public static final String ENCODING = "UTF-8";
	public static final String HEADER_FILE = "header.xml";

	@Autowired
	public void setPackageDao(PackageDao packageDao) {
		this.dao = packageDao;
		this.packageDao = packageDao;
	}

	@Value("${packages.storage.dir}")
	String packagesStorageDir;

	public MetadataPackage savePackage(MetadataPackage metadataPackage) {

		boolean saveFile = (metadataPackage.getId() == null);
		MetadataPackage metadatapackage = super.save(metadataPackage);
		String filename = metadatapackage.getId().toString() + ".zip";
		if (saveFile) {
			try {
				saveFile(filename, metadataPackage.getFile());
			} catch (IOException e) {
				remove(metadataPackage.getId());
				throw new APIException("Failed to save the package", e);
			}
		}
		return metadatapackage;
	}

	protected void saveFile(final String filename, byte[] file)
			throws IOException {

		// Create the directory if it doesn't exist
		File dirPath = new File(packagesStorageDir);

		if (!dirPath.exists()) {
			dirPath.mkdir();
		}

		if (log.isDebugEnabled())
			log.debug("Saving file to " + dirPath.toString());

		// write the file to the file specified
		File packagedata = new File(dirPath, filename);
		FileOutputStream bos = null;
		try {
			bos = new FileOutputStream(packagedata);
			bos.write(file);
			bos.close();
		} catch (IOException e) {
			throw new APIException("error writing a file", e);
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				// close quietly
				log.error(e);
			}
		}

	}

	public void deleteFile(String filename) {

		File f = new File(packagesStorageDir + "/" + filename + ".zip");
		f.delete();

	}

	/**
	 * @param id
	 *            The id of the package.
	 * @throws APIException
	 *             If any error occurs while downloading package or if package
	 *             doesn't exist
	 * @return returns MetadataPackage object of that particular id
	 */
	public MetadataPackage loadPackage(Long id) throws IOException {
		byte[] data = null;
		File f = new File(packagesStorageDir + "/" + id + ".zip");
		if (f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				data = new byte[fis.available()];
				fis.read(data);
				fis.close();
			} catch (FileNotFoundException e) {

				throw new APIException("Error downloading the package", e);
			}
		} else {
			Exception fe = null;
			throw new APIException("Package doesn't exist", fe);
		}

		MetadataPackage pkg = dao.get(id);
		pkg.setFile(data);
		return pkg;

	}

	/**
	 * {@inheritDoc}
	 */
	public List<MetadataPackage> search(String searchTerm) {
		return super.search(searchTerm, MetadataPackage.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MetadataPackage> searchByUser(String query, User user) {
		if (query == null) {
			query = "";
		}

		if (user != null) {
			query += " userId:" + user.getId();
		}

		List<MetadataPackage> packageList = search(query, MetadataPackage.class);
		return packageList;
	}

	public MetadataPackage deserializePackage(byte[] file) {
		Map<String, String> files = new LinkedHashMap<String, String>();

		InputStream input = new ByteArrayInputStream(file);

		ZipInputStream zip = new ZipInputStream(input);
		try {
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip
					.getNextEntry()) {
				String file1 = IOUtils.toString(zip, ENCODING);
				files.put(entry.getName(), file1);
			}
		} catch (IOException e) {
			throw new APIException("error", e);
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {

					log.error(e);
				}
			}
		}
		String header = files.get(HEADER_FILE);

		XStream xstream = new XStream(new DomDriver());

		MetadataPackage deserializedPackage = new MetadataPackage();
		xstream.registerConverter(new DateTimeConverter());
		xstream.alias("package", MetadataPackage.class);
		xstream.omitField(MetadataPackage.class, "file");
		xstream.omitField(MetadataPackage.class, "user");
		xstream.omitField(MetadataPackage.class, "downloadCount");
		xstream.omitField(MetadataPackage.class, "serializedPackage");
		xstream.omitField(MetadataPackage.class, "modules");
		xstream.omitField(MetadataPackage.class, "items");
		xstream.omitField(MetadataPackage.class, "relatedItems");
		deserializedPackage = (MetadataPackage) xstream.fromXML(header);

		return deserializedPackage;
	}

	public List<MetadataPackage> getAllPackageVersions(
			MetadataPackage metadataPackage) {

		List<MetadataPackage> sortedPackageList = null; //need to write functionality

		return sortedPackageList;
	}

}
