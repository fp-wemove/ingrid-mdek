package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServ implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer typeKey;
	private String typeValue;
	private String history;
	private String environment;
	private String base;
	private String description;

	private Set t011ObjServOperations = new HashSet();
	private Set t011ObjServTypes = new HashSet();
	private Set t011ObjServVersions = new HashSet();

	public T011ObjServ() {}

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

	public Long getObjId() {
		return objId;
	}

	public void setObjId(Long objId) {
		this.objId = objId;
	}

	public Integer getTypeKey() {
		return typeKey;
	}

	public void setTypeKey(Integer typeKey) {
		this.typeKey = typeKey;
	}

	public String getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public Set getT011ObjServOperations() {
		return t011ObjServOperations;
	}

	public void setT011ObjServOperations(Set t011ObjServOperations) {
		this.t011ObjServOperations = t011ObjServOperations;
	}

	public Set getT011ObjServTypes() {
		return t011ObjServTypes;
	}

	public void setT011ObjServTypes(Set t011ObjServTypes) {
		this.t011ObjServTypes = t011ObjServTypes;
	}

	public Set getT011ObjServVersions() {
		return t011ObjServVersions;
	}

	public void setT011ObjServVersions(Set t011ObjServVersions) {
		this.t011ObjServVersions = t011ObjServVersions;
	}

}