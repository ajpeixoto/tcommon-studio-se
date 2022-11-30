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
package org.talend.core.services;

import org.eclipse.jface.window.WindowManager;

/**
 * @author bhe created on Oct 28, 2022
 *
 */
public class NotificationService implements INotificationService {

    private WindowManager manager = new WindowManager();

    @Override
    public WindowManager getNotificationWindowManager() {
        return manager;
    }

}
