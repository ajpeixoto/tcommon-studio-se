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
import java.util.concurrent.Semaphore;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.TalendUI;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbstractCustomUI implements ICustomUI {

    private Semaphore modalLock = new Semaphore(1);

    private boolean isModalDialog = true;

    private String id;

    private ICustomUIEngine uiEngine;

    private Map<String, IEventHandler> eventMap = new HashMap<>();

    public AbstractCustomUI(String id, boolean isModalDialog) {
        this.id = id;
        this.isModalDialog = isModalDialog;
        this.uiEngine = TalendUI.get().getStigmaUIEngine();
        registerEventHandlers();
    }

    protected IUIEvent createOpenEvent() {
        DefaultUIEvent openEvent = new DefaultUIEvent(BuiltinEvent.open.name());
        openEvent.getEventParams().put(BuiltinParams.uiId.name(), getId());
        return openEvent;
    }

    @Override
    public void handleUIEvent(IUIEvent event) {
        String eventKey = event.getEventKey();
        boolean closeDialog = false;
        if (BuiltinEvent.ok.name().equals(eventKey)) {
            closeDialog = onOk(event);
        } else if (BuiltinEvent.apply.name().equals(eventKey)) {
            closeDialog = onApply(event);
        } else if (BuiltinEvent.close.name().equals(eventKey)) {
            closeDialog = onClose(event);
        } else if (BuiltinEvent.cancel.name().equals(eventKey)) {
            closeDialog = onCancel(event);
        } else {
            IEventHandler eventListener = eventMap.get(eventKey);
            if (eventListener != null) {
                eventListener.handleEvent(event);
            } else {
                ExceptionHandler.process(new Exception("Can't handle event: " + eventKey));
            }
        }
        if (closeDialog) {
            closeDialog();
        }
    }

    protected void closeDialog() {
        modalLock.release();
    }

    @Override
    public void run() {
        try {
            modalLock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Can't open dialog", e);
        }
        doRun();
        if (isModalDialog()) {
            try {
                modalLock.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException("Dialog is closed unexpected", e);
            }
        }
        modalLock.release();
    }

    protected void doRun() {
        IUIEvent openEvent = createOpenEvent();
        dispatchUIEvent(openEvent);
    }

    @Override
    public void dispatchUIEvent(IUIEvent event) {
        this.uiEngine.dispatchUIEvent(this, event);
    }

    protected void registerEventListener(String key, IEventHandler listener) {
        eventMap.put(key, listener);
    }

    public boolean isModalDialog() {
        // currently don't support to change modal, if do it, need to update logic of run
        return isModalDialog;
    }

    @Override
    public String getId() {
        return this.id;
    }

    protected void registerEventHandlers() {
    }

    protected boolean onOk(IUIEvent event) {
        return true;
    }

    protected boolean onApply(IUIEvent event) {
        return true;
    }

    protected boolean onClose(IUIEvent event) {
        return true;
    }

    protected boolean onCancel(IUIEvent event) {
        return true;
    }

}
