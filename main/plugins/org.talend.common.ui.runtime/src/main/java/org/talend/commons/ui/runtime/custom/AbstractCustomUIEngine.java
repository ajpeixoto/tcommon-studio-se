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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbstractCustomUIEngine implements ICustomUIEngine {

    private static final Logger log = Logger.getLogger(AbstractCustomUIEngine.class);

    private Map<String, IUIEventHandler> uiEventHandlers = Collections.synchronizedMap(new HashMap<>());

    private Map<String, Set<IUIEventHandler>> globalUIEventHandlers = Collections.synchronizedMap(new HashMap<>());

    public AbstractCustomUIEngine() {
        // nothing to do
    }

    @Override
    public void run(ICustomUI ui) {
        doRun(ui);
    }

    protected void doRun(ICustomUI ui) {
        ui.run();
    }

    @Override
    public void handleUIEvent(IUIEvent event) {
        String uiId = event.getUIId();
        if (StringUtils.isBlank(uiId)) {
            Set<IUIEventHandler> handlers = globalUIEventHandlers.get(event.getEventKey());
            if (handlers != null) {
                new Thread(() -> {
                    for (IUIEventHandler handler : handlers) {
                        if (handler.canHandle(event)) {
                            handler.handleUIEvent(event);
                        }
                    }
                }).start();
            }
        } else {
            IUIEventHandler handler = uiEventHandlers.get(uiId);
            if (handler != null && handler.canHandle(event)) {
                new Thread(() -> {
                    handler.handleUIEvent(event);
                }).start();
            }
        }
    }

    @Override
    public void registerUIEventHandler(String uiId, IUIEventHandler handler) {
        IUIEventHandler existing = uiEventHandlers.put(uiId, handler);
        if (existing != null) {
            log.warn("duplicated register for UI id: " + uiId);
        }
    }

    @Override
    public void unregisterUIEventHandler(String uiId) {
        uiEventHandlers.remove(uiId);
    }

    @Override
    public void registerGlobalUIEventHandler(String eventId, IUIEventHandler handler) {
        Set<IUIEventHandler> handlers = globalUIEventHandlers.getOrDefault(eventId,
                Collections.synchronizedSet(new LinkedHashSet<>()));
        handlers.add(handler);
    }

    @Override
    public void unregisterGlobalUIEventHandler(String eventId, IUIEventHandler handler) {
        Set<IUIEventHandler> handlers = globalUIEventHandlers.getOrDefault(eventId,
                Collections.synchronizedSet(new LinkedHashSet<>()));
        handlers.remove(handler);
    }

}
