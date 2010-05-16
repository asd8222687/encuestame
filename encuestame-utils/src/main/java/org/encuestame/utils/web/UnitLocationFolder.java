/*
 ************************************************************************************
 * Copyright (C) 2001-2010 encuestame: system online surveys Copyright (C) 2009
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.encuestame.utils.web;

import java.io.Serializable;

/**
 * Location Folder Bean.
 * @author Picado, Juan juanATencuestame.org
 * @since May 16, 2010 1:54:45 PM
 * @version Id:
 */
public class UnitLocationFolder implements Serializable {

    /**
     * Serial.
     */
    private static final long serialVersionUID = -8070322441826004390L;

    private Long locationFolderId;

    private String name;

    private UnitUserBean unitUserBean;

    private String type;

    /**
     * @return the locationFolderId
     */
    public Long getLocationFolderId() {
        return locationFolderId;
    }

    /**
     * @param locationFolderId the locationFolderId to set
     */
    public void setLocationFolderId(final Long locationFolderId) {
        this.locationFolderId = locationFolderId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the unitUserBean
     */
    public UnitUserBean getUnitUserBean() {
        return unitUserBean;
    }

    /**
     * @param unitUserBean the unitUserBean to set
     */
    public void setUnitUserBean(final UnitUserBean unitUserBean) {
        this.unitUserBean = unitUserBean;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }
}
