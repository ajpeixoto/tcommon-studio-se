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
package org.talend.designer.maven.migration.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.ITestContainerCoreService;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ItemReportRecorder {

    protected Item item;

    protected String detailMessage;

    public String getItemType() {
        String type = "";
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        if (itemType != null) {
            if (ERepositoryObjectType.getAllTypesOfTestContainer().contains(itemType)) {
                Item parentJobItem = getTestCaseParentJobItem(item);
                if (parentJobItem != null) {
                    ERepositoryObjectType parentJobType = ERepositoryObjectType.getItemType(parentJobItem);
                    if (parentJobType != null) {
                        String parentTypePath = getCompleteObjectTypePath(parentJobType);
                        if (StringUtils.isNotBlank(parentTypePath)) {
                            type = parentTypePath + "/";
                        }
                    }
                }
                type += itemType;
            } else {
                type = getCompleteObjectTypePath(itemType);
            }
        }
        return type;
    }

    public String getItemPath() {
        String path = "";
        StringBuffer buffer = new StringBuffer();
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);

        if (ERepositoryObjectType.getAllTypesOfTestContainer().contains(itemType)) {
            StringBuffer testcaseBuffer = new StringBuffer();
            Item parentJobItem = getTestCaseParentJobItem(item);
            if (parentJobItem != null) {
                if (parentJobItem.getState() != null && StringUtils.isNotBlank(parentJobItem.getState().getPath())) {
                    testcaseBuffer.append(parentJobItem.getState().getPath()).append("/");
                }
                testcaseBuffer.append(parentJobItem.getProperty() != null ? parentJobItem.getProperty().getLabel() : "");
                if (StringUtils.isNotBlank(testcaseBuffer.toString())) {
                    buffer.append(testcaseBuffer.toString()).append("/");
                }
            }
        } else {
            if (item.getState() != null && StringUtils.isNotBlank(item.getState().getPath())) {
                buffer.append(item.getState().getPath()).append("/");
            }
        }

        Property property = item.getProperty();
        if (property != null) {
            buffer.append(property.getLabel() + "_" + property.getVersion());
        }
        path = buffer.toString();
        return path;
    }

    private Item getTestCaseParentJobItem(Item testcaseItem) {
        Item parentJobItem = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerCoreService.class)) {
            ITestContainerCoreService testcaseService = GlobalServiceRegister.getDefault()
                    .getService(ITestContainerCoreService.class);
            if (testcaseService != null) {
                try {
                    parentJobItem = testcaseService.getParentJobItem(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return parentJobItem;
    }

    private String getCompleteObjectTypePath(ERepositoryObjectType itemType) {
        ERepositoryObjectType rootItemType = itemType;
        if (ERepositoryObjectType.JDBC != null && ERepositoryObjectType.JDBC.equals(rootItemType)) {
            rootItemType = ERepositoryObjectType.METADATA_CONNECTIONS;
        }
        List<String> typeLabels = new ArrayList<String>();
        findOutCompleteTypePath(rootItemType, typeLabels);
        StringBuffer buffer = new StringBuffer();
        if (!typeLabels.isEmpty()) {
            for (int i = 0; i < typeLabels.size(); i++) {
                if (i != 0) {
                    buffer.append("/");
                }
                buffer.append(typeLabels.get(i));
            }
        }
        return buffer.toString();
    }

    private void findOutCompleteTypePath(ERepositoryObjectType type, List<String> typeLabels) {
        ERepositoryObjectType parentType = ERepositoryObjectType.findParentType(type);
        if (parentType != null) {
            findOutCompleteTypePath(parentType, typeLabels);
        }
        typeLabels.add(type.getLabel());
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }

}
