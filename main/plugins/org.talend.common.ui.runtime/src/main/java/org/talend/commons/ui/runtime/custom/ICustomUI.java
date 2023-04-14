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

import java.util.Map;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface ICustomUI {

    String getId();

    void run();
    
    void handleUIEvent(IUIEvent event);

    void dispatchUIEvent(IUIEvent event);

    public interface IEventHandler {

        void handleEvent(IUIEvent event);

    }

    public interface IUIEvent {

        String getEventKey();

        Map<String, Object> getEventParams();

    }

    static enum BuiltinEvent {
        open,
        ok,
        apply,
        close,
        cancel;
    }

    static enum BuiltinParams {
        uiId,
        title,
        message;
    }

}
