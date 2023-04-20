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

import java.util.concurrent.CompletableFuture;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface ICustomUIEngine {

    void run(ICustomUI ui);

    /**
     * Handle event from stigma
     */
    void handleUIEvent(IUIEvent event);

    /**
     * Provide UI data to stigma
     */
    Object getUIData(IUIData uiData);

    /**
     * Send event to stigma
     */
    void dispatchUIEvent(ICustomUI ui, IUIEvent event);

    /**
     * Request data from stigma
     */
    CompletableFuture<Object> requestUIData(ICustomUI ui, IUIData uiData);

    void registerThreadLocalContext(String key, Object value);

    void registerUIEventHandler(String uiId, IUIEventHandler handler);

    void unregisterUIEventHandler(String uiId);

    void registerGlobalUIEventHandler(String eventId, IUIEventHandler handler);

    void unregisterGlobalUIEventHandler(String eventId, IUIEventHandler handler);

    boolean isClientAlive();

}
