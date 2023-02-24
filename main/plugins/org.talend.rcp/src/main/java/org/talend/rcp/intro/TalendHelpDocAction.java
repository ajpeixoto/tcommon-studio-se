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
package org.talend.rcp.intro;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.talend.rcp.i18n.Messages;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class TalendHelpDocAction extends Action {

    private static final String TALEND_DOC_LINK = Messages.getString("TalendHelpDocAction.documentationLink"); //$NON-NLS-1$

    public TalendHelpDocAction() {
        setText(WorkbenchMessages.HelpContentsAction_text);
        setToolTipText(WorkbenchMessages.HelpContentsAction_toolTip);
        setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS));
    }

    @Override
    public void run() {
        Program.launch(TALEND_DOC_LINK);
    }

}
