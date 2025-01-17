<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2023 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
    <title>InGrid iPlug Administration</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="description" content="" />
    <meta name="keywords" content="" />
    <meta name="author" content="wemove digital solutions" />
    <meta name="copyright" content="wemove digital solutions GmbH" />
    <link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
    <body>
        <div id="header">
            <img src="../images/base/logo.gif" alt="InGrid" />
            <h1>Konfiguration</h1>
            <security:authorize access="isAuthenticated()">
                <div id="language">
                    <a href="../base/auth/logout.html">Logout</a>
                </div>
            </security:authorize>
        </div>
        
        <div id="help"><a href="#">[?]</a></div>
        
        <c:set var="active" value="editor" scope="request"/>
        <c:import url="../base/subNavi.jsp"></c:import>
        
        <div id="contentBox" class="contentMiddle">
            <h1 id="head">Einstellungen für die Erfassungskomponente (IGE)</h1>
            <div class="controls">
                <a href="#" onclick="document.location='../base/dbParams.html';">Zurück</a>
                <a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
                <a href="#" onclick="document.getElementById('editorConfig').submit();">Weiter</a>
            </div>
            <div class="controls cBottom">
                <a href="#" onclick="document.location='../base/dbParams.html';">Zurück</a>
                <a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
                <a href="#" onclick="document.getElementById('editorConfig').submit();">Weiter</a>
            </div>
            <div id="content">
                <form:form method="post" action="../iplug-pages/editor.html" modelAttribute="editorConfig">
                    <table id="konfigForm">
                            <tr>
                                <td class="leftCol">Kommunikation zum IGE-iBus:</td>
                                <td><form:checkbox path="igcEnableIBusCommunication" /><form:errors path="igcEnableIBusCommunication" cssClass="error" element="div" />
                                     <br />Falls die Verbindung zum iBus der Erfassungskomponente deaktiviert werden soll und das iPlug nur zum Indexieren eines Katalogs dient,
                                     so deaktivieren Sie diese Option.
                                </td>
                            </tr>
                    </table>
                </form:form>
            </div>
        </div>
        <div id="footer" style="height:100px; width:90%"></div>
    </body>
</html>
