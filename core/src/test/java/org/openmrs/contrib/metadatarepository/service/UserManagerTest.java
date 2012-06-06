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
 
package org.openmrs.contrib.metadatarepository.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.contrib.metadatarepository.Constants;
import org.openmrs.contrib.metadatarepository.model.User;
import org.openmrs.contrib.metadatarepository.service.RoleManager;
import org.openmrs.contrib.metadatarepository.service.UserManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class UserManagerTest extends BaseManagerTestCase {
    private Log log = LogFactory.getLog(UserManagerTest.class);
    @Autowired
    private UserManager mgr;
    @Autowired
    private RoleManager roleManager;
    private User user;

    @Test
    public void testGetUser() throws Exception {
        user = mgr.getUserByUsername("user");
        assertNotNull(user);
        
        log.debug(user);
        assertEquals(1, user.getRoles().size());
    }

    @Test
    public void testSaveUser() throws Exception {
        user = mgr.getUserByUsername("user");
        user.setPhoneNumber("303-555-1212");

        log.debug("saving user with updated phone number: " + user);

        user = mgr.saveUser(user);
        assertEquals("303-555-1212", user.getPhoneNumber());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    public void testAddAndRemoveUser() throws Exception {
        user = new User();

        // call populate method in super class to populate test data
        // from a properties file matching this class name
        user = (User) populate(user);

        user.addRole(roleManager.getRole(Constants.USER_ROLE));

        user = mgr.saveUser(user);
        assertEquals("john", user.getUsername());
        assertEquals(1, user.getRoles().size());

        log.debug("removing user...");

        mgr.removeUser(user.getId().toString());

        try {
            user = mgr.getUserByUsername("john");
            fail("Expected 'Exception' not thrown");
        } catch (Exception e) {
            log.debug(e);
            assertNotNull(e);
        }
    }
}
