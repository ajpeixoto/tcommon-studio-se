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
package org.talend.repository.metadata.ui.actions.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.repository.metadata.ui.view.IReference;
import org.talend.repository.metadata.ui.view.IReferencesView;
import org.talend.repository.metadata.ui.view.ProcessReference;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;


public class ShowReferencesAction extends AContextualAction {

    public ShowReferencesAction() {
        super();
        this.setText("Show references");
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        setEnabled(false);
        Object o = selection.getFirstElement();
        if (selection.isEmpty() || selection.size() != 1 || !(o instanceof RepositoryNode)) {
            return;
        }
        RepositoryNode repNode = (RepositoryNode) o;
        repositoryNode = repNode;
        setEnabled(true);
    }

    @Override
    protected void doRun() {
        if (this.repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }
        RelationshipItemBuilder relationships = RelationshipItemBuilder.getInstance();
        Property property = repositoryNode.getObject().getProperty();
        List<Relation> relations = relationships.getItemsRelatedTo(property.getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.PROPERTY_RELATION);
        if (relations != null) {
            IReferencesView referencesView = IReferencesView.findReferencesView(true);
            List<IReference> refs = new ArrayList<>();
            referencesView.setInput(repositoryNode.getObject(), refs);

            ProxyRepositoryFactory repoFactory = ProxyRepositoryFactory.getInstance();
            for (Relation relation : relations) {
                IProcess process = null;
                try {
                    IRepositoryViewObject repoObj = repoFactory.getSpecificVersion(relation.getId(), relation.getVersion(), true);
                    Property prop = repoObj.getProperty();
                    ProcessReference processReference = new ProcessReference(prop);
                    List<String> uses = new ArrayList<>();
                    processReference.setUses(uses);
                    process = IDesignerCoreService.get().getProcessFromItem(prop.getItem());
                    List<? extends INode> graphicalNodes = process.getGraphicalNodes();
                    for (INode node : graphicalNodes) {
                        IElementParameter propertyType = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                        if (propertyType != null) {
                            if (propertyType.getChildParameters().get("PROPERTY_TYPE") != null && !EmfComponent.BUILTIN
                                    .equals(propertyType.getChildParameters().get("PROPERTY_TYPE").getValue())) {
                                String repositoryValue = (String) propertyType.getChildParameters()
                                        .get("REPOSITORY_PROPERTY_TYPE").getValue();
                                if (StringUtils.isNotBlank(repositoryValue)) {
                                    uses.add(node.getLabel() + " (" + node.getComponent().getName() + ")");
                                }
                            }
                        }
                    }
                    referencesView.addInput(Arrays.asList(processReference));
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                    if (process instanceof IProcess2) {
                        ((IProcess2) process).dispose();
                    }
                }
            }
        }
    }

}
