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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.RepositoryNode;

/**
 * created by ycbai on 2015年9月10日 Detailled comment
 *
 */
public interface IGenericWizardService extends IService {

    /**
     *
     * Estimate whether <code>repObjType</code> is a generic type or not.
     *
     * @param repObjType
     * @return
     */
    public boolean isGenericType(ERepositoryObjectType repObjType);

    public boolean isGenericItem(Item item);

    public boolean isGenericConnection(Connection connection);

    /**
     * Get node image by node type name.
     *
     * @param typeName
     * @return
     */
    public Image getNodeImage(String typeName);

    /**
     * Get wizard image by node type name.
     *
     * @param typeName
     * @return
     */
    public Image getWiardImage(String typeName);

    /**
     *
     * Get metadata tables from connection
     *
     * @param connection
     * @return
     */
    public List<MetadataTable> getMetadataTables(Connection connection);

    /**
     * Get the dynamic composite
     *
     * @param composite
     * @param sectionCategory
     * @param isCompactView
     * @return
     */
    public Composite creatDynamicComposite(Composite composite, Element element, EComponentCategory sectionCategory,
            boolean isCompactView);

    /**
     * Refresh the <code>composite</code> if it is a instance of
     * <code>org.talend.repository.generic.ui.DynamicComposite</code>
     *
     * @param composite
     */
    public void refreshDynamicComposite(Composite composite);

    /**
     * Update component schema for node metadata table of node.
     *
     * @param componentProperties
     * @param metadataTable
     */
    public void updateComponentSchema(INode node, IMetadataTable metadataTable);

    /**
     * Get all component properties which are related to the <code>connection</code>.
     *
     * @param connection the connection.
     * @param tableLabel the table which need to consider the component properties along.
     * @return
     */
    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel);

    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel, boolean withEvaluator);

    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel, boolean withEvaluator,
            boolean forComponentValue, Map<Object, Object> contextMap);

    /**
     * Get the new repository type (the type from component framework) by the old repository type name.
     *
     * @param oldRepTypeName
     * @return
     */
    public ERepositoryObjectType getNewRepType(String oldRepTypeName);

    public String getConnectionProperties(Connection connection);

    /**
     * FIXME: will modify it according the rules of component definition when they finish.
     *
     * @param node
     * @return the default action which will be invoked when double click the node.
     */
    public ITreeContextualAction getDefaultAction(RepositoryNode node);

    public ITreeContextualAction getGenericAction(String typeName, String location);

    public void loadAdditionalJDBC();

    public List<String> getAllAdditionalJDBCTypes();

    public boolean getIfAdditionalJDBCDBType(String dbType);

    public void initAdditonalJDBCConnectionValue(DatabaseConnection connection, Composite dynamicForm, String dbType,
            String propertyId);

    public String getDefinitionName4AdditionalJDBC(IElement element);

    public String getDatabseNameByNode(IElement node);

    public Dbms getDbms4AdditionalJDBC(String typeName);
    
    public IWizard newSchemaWizard(IWorkbench workbench, boolean creation, IRepositoryViewObject object,
            MetadataTable metadataTable, String[] existingNames, boolean forceReadOnly);

    void openGenericWizard(String type, boolean creation, IPath path, String[] existingNames);

    public static IGenericWizardService get() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            return GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        return null;
    }
    
}
