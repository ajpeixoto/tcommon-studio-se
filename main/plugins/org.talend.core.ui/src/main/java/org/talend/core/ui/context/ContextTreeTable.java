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
package org.talend.core.ui.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.DetailGlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.SortableTreeComparator;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.ColorConstants;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IMetadataManagmentUiService;
import org.talend.core.ui.context.model.ContextTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableConstants;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.core.ui.context.nattableTree.ContextAutoResizeTextPainter;
import org.talend.core.ui.context.nattableTree.ContextColumnHeaderDecorator;
import org.talend.core.ui.context.nattableTree.ContextNatTableBackGroudPainter;
import org.talend.core.ui.context.nattableTree.ContextNatTableConfiguration;
import org.talend.core.ui.context.nattableTree.ContextNatTableStyleConfiguration;
import org.talend.core.ui.context.nattableTree.ContextNatTableUtils;
import org.talend.core.ui.context.nattableTree.ContextParaModeChangeMenuConfiguration;
import org.talend.core.ui.context.nattableTree.ContextRowDataListFixture;
import org.talend.core.ui.context.nattableTree.ContextValueLabelAccumulator;
import org.talend.core.ui.context.nattableTree.ExtendedContextColumnPropertyAccessor;
import org.talend.core.ui.i18n.Messages;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

/**
 * created by ldong on Jul 10, 2014 Detailled comment
 *
 */
public class ContextTreeTable {

    private NatTable natTable;

    // for bug TDI-32821锛� use LinkedList to keep the original order of context parameter list.
    private List<ContextTreeNode> treeNodes = new LinkedList<ContextTreeNode>();

    private static Map<String, Boolean> expandMap = new HashMap<>();

    private IStructuredSelection currentNatTabSel;

    private final static String TREE_CONTEXT_ROOT = "";

    private final static String TREE_DEFAULT_NODE = "node";

    // by default sort by the model id
    private final static String TREE_CONTEXT_ID = "orderId";

    private IContextModelManager manager;

    private final static int fixedCheckBoxWidth = 100;

    private final static int fixedTypeWidth = 100;

    public ContextTreeTable(IContextModelManager manager) {
        this.manager = manager;
    }

    public TControl createTable(Composite parentContainer) {
        TControl retObj = createTableControl(parentContainer);
        retObj.setControl(retObj.getControl());
        return retObj;
    }

    public IStructuredSelection getSelection() {
        return currentNatTabSel;
    }

    public void clearSelection() {
        currentNatTabSel = null;
    }

    public void refresh() {
        if (natTable == null) {
            return;
        }
        natTable.refresh();
    }

    /**
     * create the context NatTable
     *
     * @param parent
     * @return
     */
    private TControl createTableControl(Composite parent) {
        ConfigRegistry configRegistry = new ConfigRegistry();
        ColumnGroupModel columnGroupModel = new ColumnGroupModel();
        configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, DefaultComparator.getInstance());
        String[] propertyNames = ContextRowDataListFixture.getPropertyNameToLabels(manager);
        int comWidth = parent.getParent().getClientArea().width - 15;
        // the data source for the context
        if (propertyNames.length > 0) {
            treeNodes.clear();
            constructContextTreeNodes();
            EventList<ContextTreeNode> eventList = GlazedLists.eventList(treeNodes);
            SortedList<ContextTreeNode> sortedList = new SortedList<ContextTreeNode>(eventList, null);
            // init Column header layer
            IColumnPropertyAccessor<ContextTreeNode> columnPropertyAccessor = new ExtendedContextColumnPropertyAccessor<ContextTreeNode>(
                    propertyNames, columnGroupModel);

            IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames);
            DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);

            // init context tree model layer for the body layer
            ISortModel sortModel = new GlazedListsSortModel(sortedList, columnPropertyAccessor, configRegistry,
                    columnHeaderDataLayer);

            final TreeList<ContextTreeNode> treeList = new TreeList(sortedList, new ContextTreeFormat(sortModel),
                    new ContextExpansionModel());
            GlazedListTreeData<ContextTreeNode> treeData = new ContextTreeData(treeList);

            final GlazedListsDataProvider<ContextTreeNode> bodyDataProvider = new GlazedListsDataProvider(treeList,
                    columnPropertyAccessor);
            // the main dataLayer
            DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

            DetailGlazedListsEventLayer<ContextTreeNode> glazedListsEventLayer = new DetailGlazedListsEventLayer<ContextTreeNode>(
                    bodyDataLayer, treeList);

            // set up Body layer
            ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer);
            ColumnGroupReorderLayer columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
            ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
            // context columns hide or show for the column group
            ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                    columnHideShowLayer, columnGroupModel);

            RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(columnGroupExpandCollapseLayer);

            final TreeLayer treeLayer = new TreeLayer(rowHideShowLayer, new GlazedListTreeRowModel<ContextTreeNode>(treeData),
                    false);

            SelectionLayer selectionLayer = new SelectionLayer(treeLayer);
            addCustomSelectionBehaviour(selectionLayer);

            ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

            // set up Cloumn group layer
            ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);

            ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, selectionLayer,
                    columnGroupModel);

            // Register labels
            SortHeaderLayer<ContextTreeNode> sortHeaderLayer = new SortHeaderLayer<ContextTreeNode>(columnGroupHeaderLayer,
                    sortModel, false);

            // set up Row header layer
            DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
            DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
            RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

            // set up Corner layer
            DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider,
                    rowHeaderDataProvider);
            DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
            CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, sortHeaderLayer);

            // set up the final Grid layer
            final GridLayer gridLayer = new GridLayer(viewportLayer, sortHeaderLayer, rowHeaderLayer, cornerLayer);

            // config the column edit configuration
            ContextValueLabelAccumulator labelAccumulator = new ContextValueLabelAccumulator(bodyDataLayer, bodyDataProvider,
                    manager.getContextManager(), manager);
            bodyDataLayer.setConfigLabelAccumulator(labelAccumulator);
            registerColumnLabels(labelAccumulator, ContextRowDataListFixture.getContexts(manager.getContextManager()));

            ISelectionProvider selectionProvider = new RowSelectionProvider<ContextTreeNode>(selectionLayer, bodyDataProvider,
                    false);

            natTable = new NatTable(parent, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, gridLayer, false);
            natTable.setConfigRegistry(configRegistry);

            addCustomStylingBehaviour(parent.getFont(), bodyDataProvider, columnGroupModel, manager);

            addCustomContextMenuBehavior(manager, bodyDataProvider, selectionProvider);

            natTable.addConfiguration(new DefaultTreeLayerConfiguration(treeLayer));
            natTable.addConfiguration(new SingleClickSortConfiguration());

            addCustomColumnHeaderStyleBehaviour();

            List<Integer> checkColumnPos = getAllCheckPosBehaviour(manager, columnGroupModel);

            int dataColumnsWidth = bodyDataLayer.getWidth();

            int maxWidth = (comWidth > dataColumnsWidth) ? comWidth : dataColumnsWidth;

            // for caculate the suitable column size for when maxmum or minmum the context tab

            addCustomColumnsResizeBehaviour(bodyDataLayer, checkColumnPos, cornerLayer.getWidth(), maxWidth);

            NatGridLayerPainter layerPainter = new NatGridLayerPainter(natTable);
            natTable.setLayerPainter(layerPainter);

            attachCheckColumnTip(natTable);

            final Color backgroundColor = ColorConstants.getTableBackgroundColor();
            // global settings only effect on body and default region, so should set other regions' color separately.
            natTable.setBackground(backgroundColor);
            natTable.addConfiguration(new AbstractRegistryConfiguration() {

                @Override
                public void configureRegistry(IConfigRegistry configRegistry) {
                    Style cellStyle = new Style();
                    cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, backgroundColor);
                    configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL,
                            GridRegion.COLUMN_HEADER);
                    configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL,
                            GridRegion.CORNER);
                    configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL,
                            GridRegion.ROW_HEADER);
                }
            });

            natTable.configure();

            GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

            // add selection listener for the context NatTable
            addNatTableListener(bodyDataProvider, selectionProvider);

            GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

            TControl retObj = new TControl();
            retObj.setControl(natTable);
            return retObj;
        }
        return null;
    }

    private void attachCheckColumnTip(NatTable nt) {
        DefaultToolTip toolTip = new ContextNatTableToolTip(nt);
        toolTip.setPopupDelay(500);
        toolTip.activate();
        toolTip.setShift(new Point(10, 10));
    }

    private void constructContextTreeNodes() {
        List<IContext> contextList = ContextRowDataListFixture.getContexts(manager.getContextManager());
        List<IContextParameter> contextDatas = ContextTemplateComposite.computeContextTemplate(contextList);
        List<ContextTableTabParentModel> listofData = ContextNatTableUtils.constructContextDatas(contextDatas);
        contructContextTrees(listofData);
    }

    private void contructContextTrees(List<ContextTableTabParentModel> listOfData) {
        for (ContextTableTabParentModel contextModel : listOfData) {
            if (contextModel.hasChildren()) {
                ContextTreeNode parentTreeNode = createContextTreeNode(contextModel.getOrder(), manager, contextModel, null,
                        contextModel.getSourceName());
                List<ContextTabChildModel> childModels = contextModel.getChildren();
                for (ContextTabChildModel childModel : childModels) {
                    createContextTreeNode(contextModel.getOrder(), manager, childModel, parentTreeNode, childModel
                            .getContextParameter().getName());
                }
            } else {
                createContextTreeNode(contextModel.getOrder(), manager, contextModel, null, contextModel.getContextParameter()
                        .getName());
            }
        }
    }

    private ContextTreeNode createContextTreeNode(int orderId, IContextModelManager modelManager, Object data,
            ContextTreeNode parent, String currentNodeName) {
        ContextTreeNode datum = new ContextTreeNode(orderId, modelManager, data, parent, currentNodeName);
        treeNodes.add(datum);
        return datum;
    }

    private void addNatTableListener(final GlazedListsDataProvider<ContextTreeNode> bodyDataProvider,
            ISelectionProvider selectionProvider) {
        this.natTable.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                int rowPos = natTable.getRowPositionByY(e.y);
                if (rowPos == 0 || rowPos == -1) {
                    // in case click the column header or the empty space
                    return;
                }
                int rowIndex = natTable.getRowIndexByPosition(rowPos);
                ContextTreeNode treeNode = bodyDataProvider.getRowObject(rowIndex);
                if (treeNode != null && (treeNode.getChildren().size() != 0 || treeNode.getParent() != null)) {
                    String repositoryContextName = (treeNode.getChildren().size() != 0) ? treeNode.getName() : treeNode
                            .getParent().getName();
                    List<IRepositoryViewObject> contextObjs;
                    try {
                        contextObjs = ProxyRepositoryFactory.getInstance().getAll(
                                ProjectManager.getInstance().getCurrentProject(), ERepositoryObjectType.CONTEXT);
                        for (IRepositoryViewObject contextObj : contextObjs) {
                            if (contextObj.getProperty().getLabel().equals(repositoryContextName)) {
                                RepositoryNode relateNode = new RepositoryNode(contextObj, null, ENodeType.REPOSITORY_ELEMENT);
                                contextObj.setRepositoryNode(relateNode);
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentUiService.class)) {
                                    IMetadataManagmentUiService mmUIService = GlobalServiceRegister
                                            .getDefault().getService(IMetadataManagmentUiService.class);
                                    mmUIService.openRepositoryContextWizard(relateNode);
                                }
                            }
                        }
                    } catch (PersistenceException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {

            }

            @Override
            public void mouseUp(MouseEvent e) {

            }

        });

        selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                currentNatTabSel = (IStructuredSelection) event.getSelection();
            }
        });
    }

    private List<Integer> getAllCheckPosBehaviour(IContextModelManager manager, ColumnGroupModel contextGroupModel) {
        List<Integer> checkPos = new ArrayList<Integer>();
        if (manager.getContextManager() != null) {
            List<IContext> contexts = ContextRowDataListFixture.getContexts(manager.getContextManager());
            for (IContext envContext : contexts) {
                ColumnGroup group = contextGroupModel.getColumnGroupByName(envContext.getName());
                if (group != null) {
                    checkPos.add(3);
                }
            }
        }
        return checkPos;
    }

    private void addCustomColumnsResizeBehaviour(DataLayer dataLayer, List<Integer> checkColumnsPos, int cornerWidth,
            int maxWidth) {
        dataLayer.setColumnsResizableByDefault(true);
        int dataColumnsCount = dataLayer.getPreferredColumnCount();

        if (dataColumnsCount == 2) {
            int averageWidth = maxWidth / dataColumnsCount;
            for (int i = 0; i < dataColumnsCount; i++) {
                dataLayer.setColumnWidthByPosition(i, averageWidth);
            }
        } else {
            int typeColumnPos = dataLayer.getColumnPositionByIndex(1);

            int leftWidth = maxWidth - fixedTypeWidth - fixedCheckBoxWidth * checkColumnsPos.size() - cornerWidth * 2;

            int currentColumnsCount = dataColumnsCount - checkColumnsPos.size() - 1;
            int averageWidth = leftWidth / currentColumnsCount;
            for (int i = 0; i < dataLayer.getColumnCount(); i++) {
                boolean findHide = false;
                boolean findCheck = false;
                boolean findType = false;
                if (typeColumnPos == i) {
                    findType = true;
                    dataLayer.setColumnWidthByPosition(i, fixedTypeWidth);
                }
                for (int checkPos : checkColumnsPos) {
                    if (checkPos == i) {
                        findCheck = true;
                        dataLayer.setColumnWidthByPosition(i, fixedCheckBoxWidth);
                    }
                }
                if (!findHide && !findCheck && !findType) {
                    int colW = getColumWidth(dataLayer, i, averageWidth);
                    dataLayer.setColumnWidthByPosition(i, colW);
                }
            }
        }
    }

    private int getColumWidth(DataLayer dataLayer, int colPos, int avgWidth) {
        int colWidth = fixedTypeWidth;
        GC gc = new GCFactory(natTable).createGC();
        int max = 0;
        String text = "";
        for (int i = 0; i < dataLayer.getPreferredRowCount(); i++) {
            Object dataValueByPosition = dataLayer.getDataValueByPosition(colPos, i);
            if (dataValueByPosition == null) {
                continue;
            }
            text = dataValueByPosition.toString();
            Point size = gc.textExtent(text, SWT.DRAW_MNEMONIC);
            int temp = size.x;
            if (temp > max) {
                max = temp;
            }
        }
        gc.dispose();
        if (max > colWidth) {
            max = (int) (max - text.getBytes().length * 1.5);
        }
        return colWidth > max ? colWidth : max;
    }

    private void addCustomSelectionBehaviour(SelectionLayer layer) {
        // need control the selection style when select the rows.
        DefaultSelectionStyleConfiguration selectStyleConfig = new DefaultSelectionStyleConfiguration();
        selectStyleConfig.selectedHeaderBgColor = ColorConstants.getTableBackgroundColor();
        selectStyleConfig.selectedHeaderFgColor = ColorConstants.getTableForegroundColor();
        selectStyleConfig.selectedHeaderFont = GUIHelper.DEFAULT_FONT;
        layer.addConfiguration(selectStyleConfig);
    }

    private void addCustomColumnHeaderStyleBehaviour() {
        DefaultColumnHeaderStyleConfiguration columnStyle = new DefaultColumnHeaderStyleConfiguration();
        columnStyle.cellPainter = new ContextColumnHeaderDecorator(new TextPainter());
        natTable.addConfiguration(columnStyle);
    }

    private void addCustomStylingBehaviour(Font contextFont, final GlazedListsDataProvider<ContextTreeNode> bodyDataProvider,
            ColumnGroupModel groupModel, IContextModelManager modelManager) {
        ContextNatTableStyleConfiguration natTableConfiguration = new ContextNatTableStyleConfiguration(contextFont);
        natTableConfiguration.cellPainter = new ContextNatTableBackGroudPainter(new ContextAutoResizeTextPainter(false, false,
                true), bodyDataProvider);

        natTable.addConfiguration(natTableConfiguration);
        natTable.addConfiguration(new ContextNatTableConfiguration(bodyDataProvider, groupModel,
                modelManager.getContextManager(), modelManager));
    }

    private void addCustomContextMenuBehavior(final IContextModelManager modelManager,
            final GlazedListsDataProvider<ContextTreeNode> bodyDataProvider, final ISelectionProvider selection) {
        natTable.addConfiguration(new ContextParaModeChangeMenuConfiguration(natTable, bodyDataProvider, selection));
    }

    private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator, List<IContext> contexts) {
        columnLabelAccumulator.registerColumnOverrides(0, new String[] { ContextTableConstants.COLUMN_NAME_PROPERTY });
        columnLabelAccumulator.registerColumnOverrides(1, new String[] { ContextTableConstants.COLUMN_TYPE_PROPERTY });
        columnLabelAccumulator.registerColumnOverrides(2, new String[] { ContextTableConstants.COLUMN_COMMENT_PROPERTY });
        columnLabelAccumulator.registerColumnOverrides(3, new String[] { ContextTableConstants.COLUMN_CHECK_PROPERTY });
        int j = 4;
        for (IContext context : contexts) {
            columnLabelAccumulator.registerColumnOverrides(j++, new String[] { ContextTableConstants.COLUMN_CONTEXT_VALUE });
        }
    }

    private static class ContextTreeFormat implements TreeList.Format<ContextTreeNode> {

        private final ISortModel sortModel;

        public ContextTreeFormat(ISortModel sortModel) {
            this.sortModel = sortModel;
        }

        @Override
        public boolean allowsChildren(ContextTreeNode element) {
            return true;
        }

        @Override
        public Comparator<ContextTreeNode> getComparator(int depth) {
            return new SortableTreeComparator<ContextTreeNode>(GlazedLists.beanPropertyComparator(ContextTreeNode.class,
                    TREE_CONTEXT_ID), sortModel);
        }

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.TreeList.Format#getPath(java.util.List, java.lang.Object)
         */
        @Override
        public void getPath(List<ContextTreeNode> path, ContextTreeNode element) {
            path.add(element);
            ContextTreeTable.ContextTreeNode parent = element.getParent();
            while (parent != null) {
                path.add(parent);
                parent = parent.getParent();
            }
            Collections.reverse(path);
        }
    }

    private static class ContextTreeData extends GlazedListTreeData<ContextTreeNode> {

        public ContextTreeData(TreeList<ContextTreeNode> treeList) {
            super(treeList);
        }

    }

    private static class ContextExpansionModel implements TreeList.ExpansionModel<ContextTreeNode> {

        @Override
        public boolean isExpanded(ContextTreeNode element, List<ContextTreeNode> path) {
            if (element.getTreeData() instanceof ContextTableTabParentModel) {
                ContextTableTabParentModel obj = (ContextTableTabParentModel) element.getTreeData();
                return expandMap.getOrDefault(obj.getSourceId(), true);
            }
            return true;
        }

        @Override
        public void setExpanded(ContextTreeNode element, List<ContextTreeNode> path, boolean expanded) {
            ContextTableTabParentModel obj = (ContextTableTabParentModel) element.getTreeData();
            expandMap.put(obj.getSourceId(), expanded);
        }
    }

    /**
     * A control and it's width.
     */
    public class TControl {

        Control control;

        Integer width;

        /**
         * Getter for control.
         *
         * @return the control
         */
        public Control getControl() {
            return this.control;
        }

        /**
         * Sets the control.
         *
         * @param control the control to set
         */
        public void setControl(Control control) {
            this.control = control;
        }

        /**
         * Getter for width.
         *
         * @return the width
         */
        public Integer getWidth() {
            return this.width;
        }

        /**
         * Sets the width.
         *
         * @param width the width to set
         */
        public void setWidth(Integer width) {
            this.width = width;
        }
    }

    public class ContextTreeNode implements Comparable<ContextTreeNode> {

        private IContextModelManager modelManager;

        private Object treeData;

        private final ContextTreeNode parent;

        private final List<ContextTreeNode> children = new ArrayList<ContextTreeNode>();

        private final String name;

        private final int orderId;

        public ContextTreeNode(int orderId, IContextModelManager modelManager, Object data, ContextTreeNode parent, String name) {
            this.orderId = orderId;
            this.modelManager = modelManager;
            this.treeData = data;
            this.parent = parent;
            if (parent != null) {
                parent.addChild(this);
            }

            this.name = name;
        }

        public ContextTreeNode getParent() {
            return parent;
        }

        public IContextModelManager getManager() {
            return modelManager;
        }

        public Object getTreeData() {
            return treeData;
        }

        public void addChild(ContextTreeNode child) {
            children.add(child);
        }

        public List<ContextTreeNode> getChildren() {
            return children;
        }

        public ContextTreeNode getSelf() {
            return this;
        }

        public String getName() {
            return name;
        }

        public int getOrderId() {
            return this.orderId;
        }

        /**
         * Comparison is based on name only
         */
        @Override
        public int compareTo(ContextTreeNode o) {
            if (this.orderId > o.orderId) {
                return 1;
            } else if (this.orderId < o.orderId) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class ContextNatTableToolTip extends DefaultToolTip {

        private NatTable nt;

        public ContextNatTableToolTip(NatTable natTable) {
            super(natTable, 2, false);
            this.nt = natTable;
        }

        @Override
        protected Object getToolTipArea(Event event) {
            int col = this.nt.getColumnPositionByX(event.x);
            int row = this.nt.getRowPositionByY(event.y);

            Object cellValue = this.nt.getDataValueByPosition(col, row);

            if (cellValue instanceof Boolean) {
                return new Point(col, row);
            }
            ILayerCell cell = this.nt.getCellByPosition(col, row);
            if (cell != null && cell.getConfigLabels() != null
                    && cell.getConfigLabels().contains(ContextTableConstants.LABEL_VALUE_NOT_MATCH_TYPE)
                    && cell.getConfigLabels().contains(ContextTableConstants.COLUMN_CONTEXT_VALUE)) {
                return new Point(col, row);
            }
            return null;
        }

        @Override
        protected String getText(Event event) {
            int col = this.nt.getColumnPositionByX(event.x);
            int row = this.nt.getRowPositionByY(event.y);

            Object cellValue = this.nt.getDataValueByPosition(col, row);

            if (cellValue instanceof Boolean) {
                return Messages.getString("ContextTreeTable.PromptToolTips");
            }
            ILayerCell cell = this.nt.getCellByPosition(col, row);
            if (cell != null && cell.getConfigLabels() != null
                    && cell.getConfigLabels().contains(ContextTableConstants.LABEL_VALUE_NOT_MATCH_TYPE)
                    && cell.getConfigLabels().contains(ContextTableConstants.COLUMN_CONTEXT_VALUE)) {
                return Messages.getString("ContextValidator.ParameterValueNotMatch");
            }
            return null;
        }

        @Override
        protected Composite createToolTipContentArea(Event event, Composite parent) {
            return super.createToolTipContentArea(event, parent);
        }
    }
}
