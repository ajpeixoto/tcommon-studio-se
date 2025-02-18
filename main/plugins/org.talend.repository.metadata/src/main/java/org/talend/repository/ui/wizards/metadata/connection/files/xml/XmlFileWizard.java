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
package org.talend.repository.ui.wizards.metadata.connection.files.xml;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xsd.XSDSchema;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.XmlXPathLoopDescriptor;
import org.talend.core.model.metadata.types.JavaDataTypeHelper;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlDataTypeHelper;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.preference.metadata.MetadataTypeLengthConstants;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.core.utils.CsvArray;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.OtherConnectionContextUtils;
import org.talend.metadata.managment.ui.utils.ShadowProcessHelper;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.metadata.managment.ui.wizard.PropertiesWizardPage;
import org.talend.metadata.managment.ui.wizard.metadata.connection.Step0WizardPage;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.CopyDeleteFileUtilForWizard;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.metadata.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

import orgomg.cwm.resource.record.RecordFactory;
import orgomg.cwm.resource.record.RecordFile;

/**
 * FileWizard present the FileForm. Use to create a new connection to a DB.
 */

public class XmlFileWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

    private static Logger log = Logger.getLogger(XmlFileWizard.class);

    private PropertiesWizardPage propertiesWizardPage;

    private XmlFileSelectWizardPage xmlFileSelectPage;

    private IWizardPage currentPage;

    private XmlFileConnection connection;

    private Property connectionProperty;

    private List<IWizardPage> dynamicWizardPages;

    private ATreeNode treeRootNode;

    private List<ATreeNode> rootNodes;

    private XSDSchema xsdSchema;

    protected boolean xsdRootChange = false;

    private final Map<String, List<FOXTreeNode>> foxNodesMap = new HashMap<String, List<FOXTreeNode>>();

    private boolean isToolbar;

    private String originaleObjectLabel;

    private String originalVersion;

    private String originalPurpose;

    private String originalDescription;

    private String originalStatus;

    private String oldAbstractQueryPath = "";

    private List<IMetadataTable> oldMetadataTable = new ArrayList<IMetadataTable>();

    protected static final String DEFAULT_LABEL = "Column";

    /**
     * Sets the isToolbar.
     *
     * @param isToolbar the isToolbar to set
     */
    public void setToolbar(boolean isToolbar) {
        this.isToolbar = isToolbar;
    }

    /**
     * Constructor for FileWizard.
     *
     * @param workbench
     * @param selection
     * @param strings
     */
    @SuppressWarnings("unchecked")
    public XmlFileWizard(IWorkbench workbench, boolean creation, ISelection selection, String[] existingNames) {
        super(workbench, creation);
        this.selection = selection;
        this.existingNames = existingNames;
        setNeedsProgressMonitor(true);

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        RepositoryNode node = (RepositoryNode) obj;
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }

        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            connection = ConnectionFactory.eINSTANCE.createXmlFileConnection();
            connection.setName(ERepositoryObjectType.METADATA_FILE_XML.getKey());
            // MetadataSchema metadataSchema = ConnectionFactory.eINSTANCE.createMetadataSchema();
            MetadataTable metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            metadataTable.setId(factory.getNextId());
            // connection.getTables().add(metadataSchema);
            RecordFile record = (RecordFile) ConnectionHelper.getPackage(connection.getName(), connection, RecordFile.class);
            if (record != null) { // hywang
                PackageHelper.addMetadataTable(metadataTable, record);
            } else {
                RecordFile newrecord = RecordFactory.eINSTANCE.createRecordFile();
                newrecord.setName(connection.getName());
                ConnectionHelper.addPackage(newrecord, connection);
                PackageHelper.addMetadataTable(metadataTable, newrecord);
            }
            connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$

            connectionItem = PropertiesFactory.eINSTANCE.createXmlFileConnectionItem();
            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            break;

        case REPOSITORY_ELEMENT:
            connection = (XmlFileConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
            connectionProperty = node.getObject().getProperty();
            connectionItem = (XmlFileConnectionItem) node.getObject().getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            setRepositoryObject(node.getObject());
            isRepositoryObjectEditable();
            initLockStrategy();
            break;
        }
        if (!creation) {
            this.originaleObjectLabel = this.connectionItem.getProperty().getLabel();
            this.originalVersion = this.connectionItem.getProperty().getVersion();
            this.originalDescription = this.connectionItem.getProperty().getDescription();
            this.originalPurpose = this.connectionItem.getProperty().getPurpose();
            this.originalStatus = this.connectionItem.getProperty().getStatusCode();
        }
    }

    public XmlFileWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        super(workbench, creation);
        this.selection = selection;
        this.existingNames = existingNames;
        setNeedsProgressMonitor(true);
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }

        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            connection = ConnectionFactory.eINSTANCE.createXmlFileConnection();
            connection.setName(ERepositoryObjectType.METADATA_FILE_XML.getKey());
            // MetadataSchema metadataSchema = ConnectionFactory.eINSTANCE.createMetadataSchema();
            MetadataTable metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            metadataTable.setId(factory.getNextId());
            // connection.getTables().add(metadataSchema);
            RecordFile record = (RecordFile) ConnectionHelper.getPackage(connection.getName(), connection, RecordFile.class);
            if (record != null) { // hywang
                PackageHelper.addMetadataTable(metadataTable, record);
            } else {
                RecordFile newrecord = RecordFactory.eINSTANCE.createRecordFile();
                newrecord.setName(connection.getName());
                ConnectionHelper.addPackage(newrecord, connection);
                PackageHelper.addMetadataTable(metadataTable, newrecord);
            }
            connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$

            connectionItem = PropertiesFactory.eINSTANCE.createXmlFileConnectionItem();
            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            break;

        case REPOSITORY_ELEMENT:
            connection = (XmlFileConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
            connectionProperty = node.getObject().getProperty();
            connectionItem = (XmlFileConnectionItem) node.getObject().getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            setRepositoryObject(node.getObject());
            isRepositoryObjectEditable();
            initLockStrategy();
            break;
        }
        if (!creation) {
            this.originaleObjectLabel = this.connectionItem.getProperty().getLabel();
            this.originalVersion = this.connectionItem.getProperty().getVersion();
            this.originalDescription = this.connectionItem.getProperty().getDescription();
            this.originalPurpose = this.connectionItem.getProperty().getPurpose();
            this.originalStatus = this.connectionItem.getProperty().getStatusCode();
            MetadataTable[] tables = ConnectionHelper.getTables(connectionItem.getConnection()).toArray(new MetadataTable[0]);
            for (MetadataTable table : tables) {
                this.oldMetadataTable.add(ConvertionHelper.convert(table));
            }
        }
    }

    /**
     * Adding the page to the wizard.
     */
    @Override
    public void addPages() {
        if (isToolbar) {
            pathToSave = null;
        }
        propertiesWizardPage = new Step0WizardPage(connectionProperty, pathToSave, ERepositoryObjectType.METADATA_FILE_XML,
                !isRepositoryObjectEditable(), creation);

        xmlFileSelectPage = new XmlFileSelectWizardPage(creation, connectionItem, isRepositoryObjectEditable(), existingNames);

        if (connection != null) {
            List schemas = connection.getSchema();
            if (!schemas.isEmpty()) {
                XmlXPathLoopDescriptor currentSchema = (XmlXPathLoopDescriptor) schemas.get(0);
                oldAbstractQueryPath = currentSchema.getAbsoluteXPathQuery();
            }
        }
        if (creation) {
            setWindowTitle(Messages.getString("XmlFileWizard.windowTitleCreate")); //$NON-NLS-1$

            propertiesWizardPage
                    .setTitle(Messages.getString("FileWizardPage.titleCreate") + " 1 " + Messages.getString("FileWizardPage.of") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            + " 5"); //$NON-NLS-1$
            propertiesWizardPage.setDescription(Messages.getString("FileWizardPage.descriptionCreateStep0")); //$NON-NLS-1$
            addPage(propertiesWizardPage);
            propertiesWizardPage.setPageComplete(false);

            xmlFileSelectPage
                    .setTitle(Messages.getString("FileWizardPage.titleCreate") + " 2 " + Messages.getString("FileWizardPage.of") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            + " 5");
            xmlFileSelectPage.setDescription("Select input or output model to create XML metadata connection");
            addPage(xmlFileSelectPage);
            xmlFileSelectPage.setPageComplete(true);

        } else {
            setWindowTitle(Messages.getString("XmlFileWizard.windowTitleUpdate")); //$NON-NLS-1$

            propertiesWizardPage
                    .setTitle(Messages.getString("FileWizardPage.titleUpdate") + " 1 " + Messages.getString("FileWizardPage.of") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            + " 4"); //$NON-NLS-1$
            propertiesWizardPage.setDescription(Messages.getString("FileWizardPage.descriptionUpdateStep0")); //$NON-NLS-1$
            addPage(propertiesWizardPage);

            xmlFileSelectPage
                    .setTitle(Messages.getString("FileWizardPage.titleUpdate") + " 2 " + Messages.getString("FileWizardPage.of") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            + " 4");
            xmlFileSelectPage.setDescription("Select input or output model to create XML metadata connection");
            addPage(xmlFileSelectPage);
            xmlFileSelectPage.setPageComplete(true);
            if (!isRepositoryObjectEditable()) {
                creation = true;
            }
        }
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        IWizardPage nextPage = null;
        if (page instanceof XmlFileWizardPage) {
            if (page instanceof XmlFileSelectWizardPage) {
                nextPage = dynamicWizardPages.get(0);
            } else if (page instanceof XmlFileOutputWizardPage) {
                int curStep = ((XmlFileOutputWizardPage) page).step;
                for (IWizardPage curPage : dynamicWizardPages) {
                    if (curPage instanceof XmlFileOutputWizardPage) {
                        if (((XmlFileOutputWizardPage) curPage).step == curStep + 1) {
                            nextPage = curPage;
                        }
                    }
                }
            } else {
                int curStep = ((XmlFileWizardPage) page).step;
                for (IWizardPage curPage : dynamicWizardPages) {
                    if (curPage instanceof XmlFileWizardPage && !(curPage instanceof XmlFileSelectWizardPage)
                            && !(curPage instanceof XmlFileOutputWizardPage)) {
                        if (((XmlFileWizardPage) curPage).step == curStep + 1) {
                            nextPage = curPage;
                        }
                    }
                }
            }
        }

        if (nextPage != null) {
            return nextPage;
        }

        return super.getNextPage(page);
    }

    /**
     * This method determine if the 'Finish' button is enable This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        boolean formIsPerformed = false;
        IWizardPage finalPage = getCurrentPage();
        if (finalPage == null) {
            finalPage = propertiesWizardPage;
        }
        deleteTemFile();
        if (connection.isInputModel()) {

            if (finalPage instanceof XmlFileWizardPage) {
                int step = ((XmlFileWizardPage) finalPage).step;
                if (step == 2) {
                    formIsPerformed = finalPage.isPageComplete();
                    if (formIsPerformed) {
                        List schemas = connection.getSchema();
                        Set tables = ConnectionHelper.getTables(connection);
                        if (!schemas.isEmpty() && !tables.isEmpty()) {
                            XmlXPathLoopDescriptor currentSchema = (XmlXPathLoopDescriptor) schemas.get(0);
                            MetadataTable currentTable = (MetadataTable) tables.toArray(new MetadataTable[0])[0];
                            if (!currentSchema.getAbsoluteXPathQuery().equals(oldAbstractQueryPath)) {
                                resetMetadata(currentSchema.getSchemaTargets(), true);
                            } else {
                                resetMetadata(currentSchema.getSchemaTargets(), false);
                            }
                        }
                    }
                } else {
                    formIsPerformed = finalPage.isPageComplete();
                }
            } else {
                formIsPerformed = finalPage.isPageComplete();
            }

        } else {
            formIsPerformed = finalPage.isPageComplete();
        }

        if (formIsPerformed) {
            final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (creation) {
                                String nextId = factory.getNextId();
                                connectionProperty.setId(nextId);
                                final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {

                                    @Override
                                    protected void run() throws LoginException, PersistenceException {
                                        factory.create(connectionItem, propertiesWizardPage.getDestinationPath());
                                    }

                                };
                                workUnit.setAvoidUnloadResources(true);
                                factory.executeRepositoryWorkUnit(workUnit);
                            } else {
                                // update schemas
                                Map<String, SchemaTarget> schemaTargetMap = new HashMap<String, SchemaTarget>();
                                EList<XmlXPathLoopDescriptor> schema = connection.getSchema();
                                if (schema != null && schema.size() > 0) {
                                    XmlXPathLoopDescriptor xmlXPathLoopDescriptor = schema.get(0);
                                    if (xmlXPathLoopDescriptor != null) {
                                        EList<SchemaTarget> schemaTargets = xmlXPathLoopDescriptor.getSchemaTargets();
                                        if (schemaTargets != null && schemaTargets.size() > 0) {
                                            for (SchemaTarget schemaTarget : schemaTargets) {
                                                schemaTargetMap.put(schemaTarget.getTagName(), schemaTarget);
                                            }
                                        }
                                    }
                                }
                                Map<String, MetadataColumn> columnsMap = new HashMap<String, MetadataColumn>();
                                MetadataTable[] tables = ConnectionHelper.getTables(connectionItem.getConnection()).toArray(
                                        new MetadataTable[0]);
                                for (MetadataTable table : tables) {
                                    EList<MetadataColumn> columns = table.getColumns();
                                    Iterator<MetadataColumn> columnsIter = columns.iterator();
                                    while (columnsIter.hasNext()) {
                                        MetadataColumn column = columnsIter.next();
                                        if (connection.isInputModel()) {
                                            if (schemaTargetMap.get(column.getLabel()) == null) {
                                                columnsIter.remove();
                                            } else {
                                                columnsMap.put(column.getLabel(), column);
                                            }
                                        } else {
                                            columnsMap.put(column.getLabel(), column);
                                        }
                                    }
                                }
                                boolean hasAddedColumns = false;
                                Iterator<Entry<String, SchemaTarget>> schemaTargetIter = schemaTargetMap.entrySet().iterator();
                                while (schemaTargetIter.hasNext()) {
                                    Map.Entry<String, SchemaTarget> entry = schemaTargetIter.next();
                                    String key = entry.getKey();
                                    if (columnsMap.get(key) == null) {
                                        hasAddedColumns = true;
                                        break;
                                    }
                                }
                                if (hasAddedColumns) {
                                    MessageDialog.openInformation(getShell(),
                                            Messages.getString("XmlFileWizard.newColumnsDectect.title"), //$NON-NLS-1$
                                            Messages.getString("XmlFileWizard.newColumnsDectect.desc")); //$NON-NLS-1$
                                }
                                // update
                                RepositoryUpdateManager.updateFileConnection(connectionItem, oldMetadataTable);
                                refreshInFinish(propertiesWizardPage.isNameModifiedByUser());
                                final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {

                                    @Override
                                    protected void run() throws LoginException, PersistenceException {
                                        factory.save(connectionItem);
                                    }

                                };
                                workUnit.setAvoidUnloadResources(true);
                                factory.executeRepositoryWorkUnit(workUnit);
                                closeLockStrategy();
                            }
                            final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {

                                @Override
                                protected void run() throws LoginException, PersistenceException {
                                    ProxyRepositoryFactory.getInstance().saveProject(
                                            ProjectManager.getInstance().getCurrentProject());
                                }

                            };
                            workUnit.setAvoidUnloadResources(true);
                            factory.executeRepositoryWorkUnit(workUnit);
                        }
                    });
                }
            };
            IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        ISchedulingRule schedulingRule = workspace.getRoot();
                        // the update the project files need to be done in the workspace runnable to avoid all
                        // notification
                        // of changes before the end of the modifications.
                        workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }

                }
            };

            try {
                new ProgressMonitorDialog(null).run(true, true, iRunnableWithProgress);
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                //
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOC gldu Comment method "copyMetadata".
     *
     * @param schemaTargets
     */
    private void copyMetadata(EList<SchemaTarget> schemaTargets) {
        // TODO Auto-generated method stub
        System.out.println(schemaTargets.size());
        for (int i = 0; i < schemaTargets.size(); i++) {

        }
    }

    @Override
    public boolean performCancel() {
        if (!creation) {
            connectionItem.getProperty().setVersion(this.originalVersion);
            connectionItem.getProperty().setLabel(this.originaleObjectLabel);
            connectionItem.getProperty().setDisplayName(this.originaleObjectLabel);
            connectionItem.getProperty().setDescription(this.originalDescription);
            connectionItem.getProperty().setPurpose(this.originalPurpose);
            connectionItem.getProperty().setStatusCode(this.originalStatus);
        }
        deleteTemFile();
        return super.performCancel();
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     *
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection2) {
        this.selection = selection2;
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

    private void resetMetadata(List<SchemaTarget> schemaTarget, boolean flag) {
        XmlFileConnection connection2 = OtherConnectionContextUtils.getOriginalValueConnection(connection, this.connectionItem,
                connection.isContextMode(), true);
        ProcessDescription processDescription = ShadowProcessHelper.getProcessDescription(connection2);
        CsvArray csvArray = null;
        try {
            csvArray = ShadowProcessHelper.getCsvArray(processDescription, "FILE_XML");//$NON-NLS-1$
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }

        List<MetadataColumn> newColumns = new ArrayList<MetadataColumn>();

        String file = ((XmlFileConnection) this.connectionItem.getConnection()).getXmlFilePath();
        if (connection.isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(), true);
            file = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, file));
        }

        if (file != null && XmlUtil.isXSDFile(file)) {
            // prepareColumnsFromXSD(file, newColumns, schemaTarget);
            return;
        }

        if (csvArray == null || csvArray.getRows().isEmpty()) {
            return;
        } else {

            List<String[]> csvRows = csvArray.getRows();
            String[] fields = csvRows.get(0);
            int numberOfCol = fields.length;

            // define the label to the metadata width the content of the first row
            int firstRowToExtractMetadata = 0;

            // the first rows is used to define the label of any metadata
            String[] label = new String[numberOfCol];
            for (int i = 0; i < numberOfCol; i++) {
                label[i] = DEFAULT_LABEL + i;

                if (firstRowToExtractMetadata == 0) {
                    if (schemaTarget.get(i).getTagName() != null && !schemaTarget.get(i).getTagName().equals("")) { //$NON-NLS-1$
                        label[i] = "" + schemaTarget.get(i).getTagName().trim(); //$NON-NLS-1$
                        label[i] = MetadataToolHelper.validateColumnName(label[i], i);
                    }
                }
            }

            for (int i = 0; i < numberOfCol; i++) {
                // define the first currentType and assimile it to globalType
                String globalType = null;
                int lengthValue = 0;
                int precisionValue = 0;

                int current = firstRowToExtractMetadata;
                while (globalType == null) {
                    if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                        if (i >= csvRows.get(current).length) {
                            globalType = "id_String"; //$NON-NLS-1$
                        } else {
                            globalType = JavaDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                            // if (current == csvRows.size()) {
                            // globalType = "id_String"; //$NON-NLS-1$
                            // }
                        }
                    } else {
                        if (i >= csvRows.get(current).length) {
                            globalType = "String"; //$NON-NLS-1$
                        } else {
                            globalType = PerlDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                            // if (current == csvRows.size()) {
                            // globalType = "String"; //$NON-NLS-1$
                            // }
                        }
                    }
                }
                // for another lines
                for (int f = firstRowToExtractMetadata; f < csvRows.size(); f++) {
                    fields = csvRows.get(f);
                    if (fields.length > i) {
                        String value = fields[i];
                        if (!value.equals("")) { //$NON-NLS-1$

                            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                                if (!JavaDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = JavaDataTypeHelper.getCommonType(globalType,
                                            JavaDataTypeHelper.getTalendTypeOfValue(value));
                                }
                            } else {
                                if (!PerlDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = PerlDataTypeHelper.getCommonType(globalType,
                                            PerlDataTypeHelper.getTalendTypeOfValue(value));
                                }
                            }
                            if (lengthValue < value.length()) {
                                lengthValue = value.length();
                            }
                            int positionDecimal = 0;
                            if (value.indexOf(',') > -1) {
                                positionDecimal = value.lastIndexOf(',');
                                precisionValue = lengthValue - positionDecimal;
                            } else if (value.indexOf('.') > -1) {
                                positionDecimal = value.lastIndexOf('.');
                                precisionValue = lengthValue - positionDecimal;
                            }
                        } else {
                            IPreferenceStore corePreferenceStore = null;
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreUIService.class)) {
                                IDesignerCoreUIService designerCoreUiService = (IDesignerCoreUIService) GlobalServiceRegister
                                        .getDefault().getService(IDesignerCoreUIService.class);
                                corePreferenceStore = designerCoreUiService.getPreferenceStore();
                            }
                            if (corePreferenceStore != null
                                    && corePreferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE) != null
                                    && !corePreferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE).equals("")) { //$NON-NLS-1$
                                globalType = corePreferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE);
                                if (corePreferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH) != null
                                        && !corePreferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH)
                                                .equals("")) { //$NON-NLS-1$
                                    lengthValue = Integer.parseInt(corePreferenceStore
                                            .getString(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH));
                                }
                            }

                        }
                    }
                }

                // define the metadataColumn to field i
                MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                // hshen bug7249
                metadataColumn.setPattern("\"dd-MM-yyyy\""); //$NON-NLS-1$
                // Convert javaType to TalendType
                String talendType = null;
                talendType = globalType;
                if (globalType.equals(JavaTypesManager.FLOAT.getId()) || globalType.equals(JavaTypesManager.DOUBLE.getId())) {
                    metadataColumn.setPrecision(precisionValue);
                } else {
                    metadataColumn.setPrecision(0);
                }
                metadataColumn.setTalendType(talendType);
                metadataColumn.setLength(lengthValue);
                metadataColumn.setLabel(label[i]);

                newColumns.add(i, metadataColumn);
            }
        }
        EList columns = ConnectionHelper.getTables(connection).toArray(new MetadataTable[0])[0].getColumns();
        if (!flag) {
            for (int i = 0; i < newColumns.size(); i++) {
                for (int j = 0; j < columns.size(); j++) {
                    if (newColumns.get(i).getLabel().equals(((MetadataColumn) columns.get(j)).getLabel())) {
                        newColumns.remove(i);
                        newColumns.add(i, (MetadataColumn) columns.get(j));
                    }
                }
            }
        }
        columns.clear();
        columns.addAll(newColumns);
    }

    @Override
    public boolean canFinish() {
        if (!creation) {
            return true;
        } else {
            if (currentPage == null) {
                return false;
            } else if (currentPage instanceof XmlFileOutputWizardPage) {
                int step = ((XmlFileOutputWizardPage) currentPage).step;
                if (step == 3) {
                    return true;
                }
            } else if (currentPage instanceof XmlFileWizardPage && !(currentPage instanceof XmlFileSelectWizardPage)
                    && !(currentPage instanceof XmlFileOutputWizardPage)) {
                int step = ((XmlFileWizardPage) currentPage).step;
                if (step == 3) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<IWizardPage> getDynamicWizardPages() {
        return this.dynamicWizardPages;
    }

    public void setDynamicWizardPages(List<IWizardPage> dynamicWizardPages) {
        this.dynamicWizardPages = dynamicWizardPages;
    }

    public Map<String, List<FOXTreeNode>> getFoxNodesMap() {
        return this.foxNodesMap;
    }

    public XSDSchema getXSDSchema() {
        return this.xsdSchema;
    }

    public void setXsModel(XSDSchema xsdSchema) {
        this.xsdSchema = xsdSchema;
    }

    public ATreeNode getTreeRootNode() {
        return this.treeRootNode;
    }

    public void setTreeRootNode(ATreeNode treeRootNode) {
        this.treeRootNode = treeRootNode;
    }

    public IWizardPage getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(IWizardPage currentPage) {
        this.currentPage = currentPage;
    }

    private void deleteTemFile() {
        CopyDeleteFileUtilForWizard.deleteWizardTempFiles();
    }

    public List<ATreeNode> getRootNodes() {
        return this.rootNodes;
    }

    public void setRootNodes(List<ATreeNode> rootNodes) {
        this.rootNodes = rootNodes;
    }

    public boolean isXsdRootChange() {
        return this.xsdRootChange;
    }

    public void setXsdRootChange(boolean xsdRootChange) {
        this.xsdRootChange = xsdRootChange;
    }
}
