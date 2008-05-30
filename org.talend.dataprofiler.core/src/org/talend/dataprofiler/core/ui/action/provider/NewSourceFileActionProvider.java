// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.action.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.sql.AddSqlFileAction;
import org.talend.dataprofiler.core.sql.DeleteSqlFileAction;
import org.talend.dataprofiler.core.sql.OpenSqlFileAction;
import org.talend.dataprofiler.core.sql.RenameSqlFileAction;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 * 
 */
public class NewSourceFileActionProvider extends CommonActionProvider {

    private String selectedFolderName;

    public NewSourceFileActionProvider() {
    }

    public void fillContextMenu(IMenuManager menu) {
        TreeSelection treeSelection = ((TreeSelection) this.getContext().getSelection());
        List<IFile> selectedFiles = new ArrayList<IFile>();
        if (treeSelection.size() == 1) {
            Object obj = treeSelection.getFirstElement();
            if (obj instanceof IFolder) {
                selectedFolderName = ((IFolder) obj).getName();
                if (selectedFolderName.equals(DQStructureManager.SOURCE_FILES)) {
                    menu.add(new AddSqlFileAction((IFolder) obj));
                }
            } else if (obj instanceof IFile) {
                IFile file = (IFile) obj;
                if (file.getFileExtension().equalsIgnoreCase("sql")) {
                    menu.add(new RenameSqlFileAction((IFile) obj));
                }
            }
        }
        boolean isSelectFile = computeSelectedFiles(treeSelection, selectedFiles);
        if (!isSelectFile && !selectedFiles.isEmpty()) {
            menu.add(new OpenSqlFileAction(selectedFiles));
            menu.add(new DeleteSqlFileAction(selectedFiles));
        }
    }

    /**
     * DOC qzhang Comment method "computeSelectedFiles".
     * 
     * @param treeSelection
     * @param selectedFiles
     * @return
     */
    public static boolean computeSelectedFiles(TreeSelection treeSelection, List<IFile> selectedFiles) {
        boolean isSelectFile = false;
        Iterator iterator = treeSelection.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof IFile) {
                IFile file = (IFile) obj;
                if (file.getFileExtension().equalsIgnoreCase("sql")) {
                    selectedFiles.add(file);
                }
            } else {
                isSelectFile = true;
                break;
            }
        }
        return isSelectFile;
    }
}
