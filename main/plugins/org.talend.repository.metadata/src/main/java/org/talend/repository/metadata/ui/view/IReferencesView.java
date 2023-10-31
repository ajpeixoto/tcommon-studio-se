// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.metadata.ui.view;

import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.IRepositoryViewObject;

public interface IReferencesView {

    static final String VIEW_ID = "org.talend.repository.metadata.ui.view.ReferencesView";

    void setInput(IRepositoryViewObject repoObj, List<IReference> inputs);

    void addInput(List<IReference> inputs);

    static IReferencesView findReferencesView(boolean open) {
        try {
            IWorkbench workbench = PlatformUI.getWorkbench();
            if (workbench != null) {
                IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                if (activeWorkbenchWindow != null) {
                    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                    if (activePage != null) {
                        IViewPart view = activePage.findView(VIEW_ID);
                        if (view == null) {
                            view = activePage.showView(VIEW_ID);
                        }
                        return (IReferencesView) view;
                    }
                }
            }
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
        return null;
    }
}
