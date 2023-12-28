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
package org.talend.core.runtime.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.repository.model.RepositoryNode;

/**
 * created by ycbai on 2016年3月24日 Detailled comment
 *
 */
public interface IGenericService extends IService {

    /**
     * Get all the names of generic type.
     *
     * @return
     */
    public List<String> getGenericTypeNames();

    /**
     * Create repository nodes by component service.
     *
     * @param curParentNode
     */
    public List<RepositoryNode> createNodesFromComponentService(RepositoryNode curParentNode);

    /**
     * Call method <code>callBeforeActivate()</code> of <code>parameter</code>.
     *
     *
     * @param parameter
     * @return
     */
    public boolean callBeforeActivate(IElementParameter parameter);

    /**
     * Get all installed generic components information.
     *
     */
    public List<Map<String, String>> getAllGenericComponentsInfo();

    public void resetReferenceValue(INode curNode, String oldConnectionName, String newConnectionName);

    public boolean isTcompv0(IComponent component);

    public void validateGenericConnection(Connection conn) throws Exception;

    void mergeGenericNodes(RepositoryNode parent, String type);

    Set<ComponentDefinition> getJDBCComponentDefinitions();

    public static IGenericService getService() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericService.class)) {
            return GlobalServiceRegister.getDefault().getService(IGenericService.class);
        }
        return null;
    }

}
