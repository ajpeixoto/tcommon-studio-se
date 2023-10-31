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

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.talend.core.model.repository.IRepositoryViewObject;


public class ReferencesView extends ViewPart implements IReferencesView {

    private ReferencesPanel panel;

    public ReferencesView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        panel = new ReferencesPanel(parent, SWT.NONE);
    }

    @Override
    public void addInput(List<IReference> inputs) {
        panel.getReferences().addAll(inputs);
    }

    @Override
    public void setInput(IRepositoryViewObject repoObj, List<IReference> inputs) {
        panel.resetReferences();
        IObservableList<IReference> references = panel.getReferences();
        panel.setRepositoryViewObject(repoObj);
        references.addAll(inputs);
    }

    @Override
    public void setFocus() {

    }

}
