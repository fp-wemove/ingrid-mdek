/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.mapper;


/**
 * Common stuff for all mappers.
 * 
 * @author Martin
 */
public interface IMapper {

	/** How much to map of bean/doc content */
	public enum MappingQuantity {
		/** IGE: initial data of entity when created */
		INITIAL_ENTITY,
		/** IGE: minimum data of entity needed */
		BASIC_ENTITY,
		/** IGE: entity displayed in tree */
		TREE_ENTITY,
		/** IGE: entity displayed in table */
		TABLE_ENTITY,
		/** IGE: entity edit/save */
		DETAIL_ENTITY,
		/** complete data EXCLUDING entity specific stuff (ORG_UUID) -> copy entity to new entity */
		COPY_DATA,
		/** ALL data INCLUDING entity specific stuff (ORG_UUID) -> copy published <-> working version */
		COPY_ENTITY,
	}
}
