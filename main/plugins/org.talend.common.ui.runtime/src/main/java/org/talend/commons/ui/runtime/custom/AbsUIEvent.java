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
package org.talend.commons.ui.runtime.custom;

import java.util.HashMap;
import java.util.Map;

import org.talend.commons.ui.runtime.custom.ICustomUI.IUIEvent;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbsUIEvent implements IUIEvent {

    private String key;

    private Map<String, Object> params = new HashMap<>();

    public AbsUIEvent(String key) {
        this.key = key;
    }

    @Override
    public String getEventKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Map<String, Object> getEventParams() {
        return params;
    }

}
