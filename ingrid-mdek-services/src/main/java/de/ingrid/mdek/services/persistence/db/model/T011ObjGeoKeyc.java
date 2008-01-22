package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeoKeyc implements IEntity {

	private Long id;
	private int version;
	private Long objGeoId;
	private Integer line;
	private String subjectCat;
	private String keyDate;
	private String edition;


	public T011ObjGeoKeyc() {}

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

	public Long getObjGeoId() {
		return objGeoId;
	}

	public void setObjGeoId(Long objGeoId) {
		this.objGeoId = objGeoId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getSubjectCat() {
		return subjectCat;
	}

	public void setSubjectCat(String subjectCat) {
		this.subjectCat = subjectCat;
	}

	public String getKeyDate() {
		return keyDate;
	}

	public void setKeyDate(String keyDate) {
		this.keyDate = keyDate;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}


}