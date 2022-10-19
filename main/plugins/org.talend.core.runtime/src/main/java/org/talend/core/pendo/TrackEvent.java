// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.pendo;


/**
 * DOC jding  class global comment. Detailled comment
 */
public enum TrackEvent {

    PROJECT_LOGIN("Project Login"),
    IMPORT_API_DEF("Import API Definition"),
    UPDATE_API_DEF("Update API Definition"),
    USE_API_DEF("Use API Definition"),
    OPEN_IN_APIDesigner("Open in API Designer"),
    OPEN_IN_APITester("Open in API Tester"),
    OPEN_API_DOCUMENTATION("Open API Documentation"),
    AUTOMAP("tMap Automap"),
    TMAP("tMap"),
    ITEM_IMPORT("Import items"),
    ITEM_SIGNATURE("Item Signature");

    private String event;

    TrackEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

}
