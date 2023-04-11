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
package org.talend.commons.ui.runtime;

import java.util.HashMap;
import java.util.Map;

import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbstractCustomUI implements ICustomUI {

    private Map<String, IEventListener> eventMap = new HashMap<>();

    public AbstractCustomUI() {
        // nothing to do
    }

    @Override
    public void onEvent(IUIEvent event) {
        String eventKey = event.getEventKey();
        IEventListener eventListener = eventMap.get(eventKey);
        if (eventListener != null) {
            eventListener.onEvent(event);
        } else {
            ExceptionHandler.process(new Exception("Can't handle event: " + eventKey));
        }
    }

    @Override
    public void run() {
        doRun();
    }

    protected abstract void doRun();

    protected void registerEventListener(String key, IEventListener listener) {
        eventMap.put(key, listener);
    }

}
