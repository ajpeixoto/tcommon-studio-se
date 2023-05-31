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
package org.talend.repository.ui.wizards.metadata.connection.database;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.ui.swt.listviewer.ControlListItem;

public class DatabaseInfoItem<T> extends ControlListItem<T> {

    private T element;

    private Button button;

    public DatabaseInfoItem(Composite parent, int style, T element) {
        super(parent, style, element);
        this.element = element;
        init(parent);
    }

    protected void init(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(this);
        Composite panel = new Composite(this, SWT.NONE);
        panel.setLayout(new GridLayout());
        panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button = new Button(panel, SWT.RADIO);
        button.setText((String) element);
        button.setImage(JFaceResources.getImageRegistry().get(DatabaseImageHelper.getDBImageName((String) element)));
        button.setLayoutData(new GridData(GridData.CENTER));
        Label horizonLine = new Label(panel, SWT.SEPARATOR | SWT.HORIZONTAL);
        horizonLine.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
    }

    @Override
    public void updateColors(int index) {
        //
    }

    @Override
    protected void refresh() {
        //
    }

    public Button getRadio() {
        return button;
    }

    public T getElement() {
        return element;
    }

}
