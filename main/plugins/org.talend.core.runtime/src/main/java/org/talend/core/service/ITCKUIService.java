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
package org.talend.core.service;

import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.swt.graphics.Image;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

public interface ITCKUIService extends IService {

    List<IRepositoryNode> mergeTCKDBRepositoryNode(Object[] base);

    Image getTCKImage(Object element, BiFunction<Image, IRepositoryViewObject, Image> decorator);

    boolean isTCKRepoistoryNode(RepositoryNode node);

    ERepositoryObjectType getTCKRepositoryType(String componentName);

    int openTCKWizard(String type, boolean creation, Object node, String[] existingNames);

    public static ITCKUIService get() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITCKUIService.class)) {
            return GlobalServiceRegister.getDefault().getService(ITCKUIService.class);
        }
        return null;
    }

}
