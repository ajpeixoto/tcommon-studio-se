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
package org.talend.repository.items.importexport.handlers.imports;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemRelation;
import org.talend.core.model.properties.ItemRelations;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.repository.items.importexport.handlers.HandlerUtil;
import org.talend.repository.items.importexport.handlers.ImportHandlerHelper;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.wizard.models.ItemImportNode;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ImportDependencyRelationsHelper {

    private static final String SEPRATOR = ":";

    private static final ImportDependencyRelationsHelper instance = new ImportDependencyRelationsHelper();

    public static ImportDependencyRelationsHelper getInstance() {
        return instance;
    }

    private Map<IPath, Map<Relation, Set<Relation>>> projectRelationsCache = new HashMap<IPath, Map<Relation, Set<Relation>>>();

    public Map<Relation, Set<Relation>> getImportItemsRelations(IPath projectFilePath) {
        Map<Relation, Set<Relation>> relationsMap = new HashMap<Relation, Set<Relation>>();
        if (projectRelationsCache.get(projectFilePath) != null) {
            relationsMap = projectRelationsCache.get(projectFilePath);
        }
        return relationsMap;
    }

    public void loadRelations(IPath projectFilePath, List itemRelationsList) {
        if (projectRelationsCache.get(projectFilePath) != null) {
            return;
        }

        projectRelationsCache.put(projectFilePath, new HashMap<Relation, Set<Relation>>());
        Map<Relation, Set<Relation>> relationsMap = projectRelationsCache.get(projectFilePath);

        for (Object o : itemRelationsList) {
            ItemRelations relations = (ItemRelations) o;
            Relation baseItem = new Relation();

            baseItem.setId(relations.getBaseItem().getId());
            baseItem.setType(relations.getBaseItem().getType());
            baseItem.setVersion(relations.getBaseItem().getVersion());

            relationsMap.put(baseItem, new HashSet<Relation>());
            for (Object o2 : relations.getRelatedItems()) {
                ItemRelation emfRelatedItem = (ItemRelation) o2;

                Relation relatedItem = new Relation();
                relatedItem.setId(emfRelatedItem.getId());
                relatedItem.setType(emfRelatedItem.getType());
                relatedItem.setVersion(emfRelatedItem.getVersion());

                relationsMap.get(baseItem).add(relatedItem);
            }
        }
    }

    public void checkImportRelationDependency(List<ItemImportNode> checkedNodeList, Set<ItemImportNode> toSelectSet,
            List<ItemImportNode> allImportItemNodesList) {
        checkedNodeList.forEach(checkedNode -> {
            List<ItemImportNode> relatedImportNodes = findOutRelationsItemImportNodes(checkedNode, toSelectSet,
                    allImportItemNodesList);
            findOutConnectionContextRelationItemImportNodes(checkedNode, toSelectSet, relatedImportNodes, allImportItemNodesList);
            checkImportRelationDependency(relatedImportNodes, toSelectSet, allImportItemNodesList);
        });
    }

    private List<ItemImportNode> findOutRelationsItemImportNodes(ItemImportNode checkedNode, Set<ItemImportNode> toSelectSet,
            List<ItemImportNode> allImportItemNodesList) {
        List<ItemImportNode> relatedImportNodesList = new ArrayList<ItemImportNode>();
        Map<Relation, Set<Relation>> relationsMap = null;
        String nodeProjectTechName = checkedNode.getProjectNode().getProject().getTechnicalLabel();
        Map<IPath, Project> pathWithProjects = ImportCacheHelper.getInstance().getPathWithProjects();
        for (IPath path : pathWithProjects.keySet()) {
            Project project = pathWithProjects.get(path);
            if (project != null && project.getTechnicalLabel().equals(nodeProjectTechName)) {
                relationsMap = projectRelationsCache.get(path);
                break;
            }
        }
        if (relationsMap == null) {
            return relatedImportNodesList;
        }

        List<Relation> collect = relationsMap.keySet().stream().filter(baseRelation -> {
            Property property = checkedNode.getItemRecord().getProperty();
            return baseRelation.getId().equals(property.getId()) && baseRelation.getVersion().equals(property.getVersion());
        }).collect(Collectors.toList());
        if (collect == null || collect.isEmpty()) {
            return relatedImportNodesList;
        }

        Set<Relation> dependencyRelations = relationsMap.get(collect.get(0));
        dependencyRelations.stream().forEach(relation -> {
            ItemImportNode relatedNode = null;
            String projectLabel = null;
            String id = relation.getId();
            if (id.contains(SEPRATOR)) {
                String[] split = id.split(SEPRATOR);
                if (split.length > 1) {
                    projectLabel = split[0];
                    id = split[1];
                }
            }
            boolean isGlobalRoutine = RelationshipItemBuilder.ROUTINE_RELATION.equals(relation.getType());
            if (RelationshipItemBuilder.LATEST_VERSION.equals(relation.getVersion())) {
                relatedNode = getLatestVersionItemImportNode(id, projectLabel, allImportItemNodesList, isGlobalRoutine);
            } else {
                relatedNode = getItemImportNodeByIdVersion(id, relation.getVersion(), projectLabel,
                        allImportItemNodesList, isGlobalRoutine);
            }
            if (relatedNode != null && !toSelectSet.contains(relatedNode)) {
                // avoid loop
                toSelectSet.add(relatedNode);
                relatedImportNodesList.add(relatedNode);
            }
        });
        return relatedImportNodesList;
    }

    private void findOutConnectionContextRelationItemImportNodes(ItemImportNode checkedNode, Set<ItemImportNode> toSelectSet,
            List<ItemImportNode> relatedImportNodes, List<ItemImportNode> allImportItemNodesList) {
        ImportItem importItem = checkedNode.getItemRecord();
        if (!(importItem.getProperty().getItem() instanceof ConnectionItem)) {
            return;
        }
        BufferedInputStream stream = null;
        try {
            ResourceSet resourceSet = importItem.getResourceSet();
            IPath itemPath = HandlerUtil.getItemPath(importItem.getPath(), importItem.getItem());
            URI itemUri = HandlerUtil.getURI(itemPath);
            Resource itemResource = resourceSet.getResource(itemUri, false);
            if (itemResource == null) {
                itemResource = resourceSet.createResource(itemUri);
            }
            File itemFile = new File(itemPath.toPortableString());
            stream = new BufferedInputStream(new FileInputStream(itemFile));
            itemResource.load(stream, null);
            URI propertyUri = HandlerUtil.getURI(importItem.getPath());
            Resource resource = resourceSet.getResource(propertyUri, false);
            File propertyFile = new File(importItem.getPath().toPortableString());
            stream = new BufferedInputStream(new FileInputStream(propertyFile));
            resource.load(stream, null);
            Property property = new ImportHandlerHelper().generateProperty(resource);
            Item item = property.getItem();
            if (item instanceof ConnectionItem) {
                ConnectionItem connItem = (ConnectionItem) item;
                Connection connection = connItem.getConnection();
                if (connection.isContextMode() && StringUtils.isNotBlank(connection.getContextId())) {
                    String technicalLabel = checkedNode.getProjectNode().getProject().getTechnicalLabel();
                    ItemImportNode contextImportNode = getLatestVersionItemImportNode(connection.getContextId(), technicalLabel,
                            allImportItemNodesList, false);
                    if (contextImportNode != null && !toSelectSet.contains(contextImportNode)) {
                        toSelectSet.add(contextImportNode);
                        relatedImportNodes.add(contextImportNode);
                    }
                }
            }
        } catch (Exception e) {
            if (Platform.inDebugMode()) {
                ExceptionHandler.process(e);
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    if (Platform.inDebugMode()) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

    public ItemImportNode getLatestVersionItemImportNode(String id, String projectTecLabel,
            List<ItemImportNode> allImportItemNodesList, boolean isGlobalRoutine) {
        List<ItemImportNode> allItemImportNodesById = getItemImportNode(allImportItemNodesList, node -> {
            Property property = node.getItemRecord().getProperty();
            boolean projectFlag = true;
            if (StringUtils.isNotBlank(projectTecLabel)) {
                projectFlag = node.getProjectNode().getProject().getTechnicalLabel().equals(projectTecLabel);
            }
            return (isGlobalRoutine ? property.getLabel().equals(id) : property.getId().equals(id)) && projectFlag;
        });
        Optional<ItemImportNode> optional = allItemImportNodesById.stream().max((node1, node2) -> VersionUtils
                .compareTo(node1.getItemRecord().getProperty().getVersion(), node2.getItemRecord().getProperty().getVersion()));
        return optional.isPresent() ? optional.get() : null;
    }

    public ItemImportNode getItemImportNodeByIdVersion(String id, String version, String projectTecLabel,
            List<ItemImportNode> allImportItemNodesList, boolean isGlobalRoutine) {
        List<ItemImportNode> importNodeList = getItemImportNode(allImportItemNodesList, node -> {
            boolean projectFlag = true;
            if (StringUtils.isNotBlank(projectTecLabel)) {
                projectFlag = node.getProjectNode().getProject().getTechnicalLabel().equals(projectTecLabel);
            }
            Property property = node.getItemRecord().getProperty();
            return (isGlobalRoutine ? property.getLabel().equals(id) : property.getId().equals(id))
                    && property.getVersion().equals(version) && projectFlag;
        });
        return importNodeList == null || importNodeList.isEmpty() ? null : importNodeList.get(0);
    }

    private List<ItemImportNode> getItemImportNode(List<ItemImportNode> allImportItemNodesList,
            Predicate<ItemImportNode> predicate) {
        List<ItemImportNode> collect = allImportItemNodesList.stream().filter(predicate).collect(Collectors.toList());
        return collect;
    }

    public void clear() {
        projectRelationsCache.clear();
    }
}
