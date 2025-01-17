/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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
package de.ingrid.mdek.job.protocol;

import java.util.List;
import java.util.Map;

public interface ProtocolHandler {
    
    public enum Type { INFO, DEBUG, WARN, ERROR };
	
	public void startProtocol();
	public void stopProtocol();
	public void clearProtocol();
	public List<String> getProtocol(Type type);
	public Map<Type, List<String>> getProtocol();
	public void addMessage(Type type, String protocolMessage);
	public boolean isDebugEnabled();
	public boolean isErrorEnabled();
	public boolean isInfoEnabled();
	public boolean isWarningEnabled();
	public String getCurrentFilename();
	public void setCurrentFilename(String currentFilename);
}
