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
 
package org.openmrs.contrib.metadatarepository.util;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * This class is converts a Double to a double-digit String
 * (and vise-versa) by BeanUtils when copying properties.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class CurrencyConverter implements Converter {
    private final Log log = LogFactory.getLog(CurrencyConverter.class);
    private DecimalFormat formatter = new DecimalFormat("###,###.00");

    public void setDecimalFormatter(DecimalFormat df) {
        this.formatter = df;
    }

    /**
     * Convert a String to a Double and a Double to a String
     *
     * @param type the class type to output
     * @param value the object to convert
     * @return object the converted object (Double or String)
     */
    public final Object convert(final Class type, final Object value) {
        // for a null value, return null
        if (value == null) {
            return null;
        } else {
            if (value instanceof String) {
                if (log.isDebugEnabled()) {
                    log.debug("value (" + value + ") instance of String");
                }

                try {
                    if (StringUtils.isBlank(String.valueOf(value))) {
                        return null;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("converting '" + value + "' to a decimal");
                    }

                    //formatter.setDecimalSeparatorAlwaysShown(true);
                    Number num = formatter.parse(String.valueOf(value));

                    return num.doubleValue();
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }
            } else if (value instanceof Double) {
                if (log.isDebugEnabled()) {
                    log.debug("value (" + value + ") instance of Double");
                    log.debug("returning double: " + formatter.format(value));
                }

                return formatter.format(value);
            }
        }

        throw new ConversionException("Could not convert " + value + " to " + type.getName() + "!");
    }
}
