/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServOpPlatform implements IEntity {

	private Long id;
	private int version;
	private Long objServOpId;
	private Integer line;
	private Integer platformKey;
	private String platformValue;


	public T011ObjServOpPlatform() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Long getObjServOpId() {
		return objServOpId;
	}

	public void setObjServOpId(Long objServOpId) {
		this.objServOpId = objServOpId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getPlatformKey() {
		return platformKey;
	}

	public void setPlatformKey(Integer platformKey) {
		this.platformKey = platformKey;
	}

	public String getPlatformValue() {
		return platformValue;
	}

	public void setPlatformValue(String platformValue) {
		this.platformValue = platformValue;
	}


}