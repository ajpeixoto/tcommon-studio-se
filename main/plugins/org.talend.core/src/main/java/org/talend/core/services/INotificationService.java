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
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;

/**
 * @author bhe created on Oct 27, 2022
 *
 */
public interface INotificationService extends IService {

    WindowManager getNotificationWindowManager();

    public static INotificationService get() {
        GlobalServiceRegister register = GlobalServiceRegister.getDefault();
        if (!register.isServiceRegistered(INotificationService.class)) {
            return null;
        }
        return register.getService(INotificationService.class);
    }
}
