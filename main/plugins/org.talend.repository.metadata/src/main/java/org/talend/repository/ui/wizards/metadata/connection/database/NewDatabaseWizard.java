// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.service.ITCKUIService;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.repository.metadata.i18n.Messages;
import org.talend.repository.model.RepositoryNode;

public class NewDatabaseWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

    private DatabaseSelectWizardPage databaseSelectWizardPage;

    private RepositoryNode node;

    private boolean isToolBar;

    public NewDatabaseWizard(IWorkbench workbench, boolean creation, boolean isToolBar, RepositoryNode node,
            String[] existingNames) {
        super(workbench, creation);
        this.existingNames = existingNames;
        setNeedsProgressMonitor(true);
        this.node = node;
        this.isToolBar = isToolBar;
    }

    @Override
    public void addPages() {
        setWindowTitle(Messages.getString("DatabaseWizard.windowTitle")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_CONNECTION_WIZ));
        databaseSelectWizardPage = new DatabaseSelectWizardPage();
        databaseSelectWizardPage.setPageComplete(false);
        addPage(databaseSelectWizardPage);
    }

    @Override
    public boolean performFinish() {
        if (!databaseSelectWizardPage.isPageComplete()) {
            return false;
        }
        String selectedDBType = databaseSelectWizardPage.getDBType();
        if (ITCKUIService.get() != null && "JDBCNew".equals(selectedDBType)) {
            int exit = ITCKUIService.get().openTCKWizard(selectedDBType, creation, node, existingNames);
            if (exit == Window.OK) {
                return true;
            }
            return false;
        }
        DatabaseWizard databaseWizard = new DatabaseWizard(getWorkbench(), creation, node, existingNames);
        databaseWizard.setToolBar(isToolBar);
        databaseWizard.setDbType(selectedDBType);
        WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), databaseWizard);
        wizardDialog.setPageSize(780, 540);
        wizardDialog.create();
        int exit = wizardDialog.open();
        if (exit == Window.OK) {
            return true;
        }
        return false;
    }

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        super.setWorkbench(workbench);
        this.selection = selection;
    }

}
