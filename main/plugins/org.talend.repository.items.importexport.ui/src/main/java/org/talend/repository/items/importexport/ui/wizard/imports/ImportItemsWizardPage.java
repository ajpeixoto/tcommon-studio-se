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
package org.talend.repository.items.importexport.ui.wizard.imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.provider.EmfResourcesFactoryReader;
import org.talend.commons.runtime.model.emf.provider.ResourceOption;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IExchangeService;
import org.talend.core.service.IStudioLiteP2Service;
import org.talend.core.service.IStudioLiteP2Service.IInstallableUnitInfo;
import org.talend.core.ui.advanced.composite.FilteredCheckboxTree;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.migration.MigrationReportHelper;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.imports.IImportItemsHandler;
import org.talend.repository.items.importexport.handlers.imports.ImportBasicHandler;
import org.talend.repository.items.importexport.handlers.imports.ImportCacheHelper;
import org.talend.repository.items.importexport.handlers.imports.ImportDependencyRelationsHelper;
import org.talend.repository.items.importexport.handlers.model.EmptyFolderImportItem;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.dialog.ShowErrorsDuringImportItemsDialog;
import org.talend.repository.items.importexport.ui.i18n.Messages;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.items.importexport.ui.wizard.imports.providers.ImportItemsViewerContentProvider;
import org.talend.repository.items.importexport.ui.wizard.imports.providers.ImportItemsViewerFilter;
import org.talend.repository.items.importexport.ui.wizard.imports.providers.ImportItemsViewerLabelProvider;
import org.talend.repository.items.importexport.ui.wizard.imports.providers.ImportItemsViewerSorter;
import org.talend.repository.items.importexport.wizard.models.FolderImportNode;
import org.talend.repository.items.importexport.wizard.models.ImportNode;
import org.talend.repository.items.importexport.wizard.models.ImportNodesBuilder;
import org.talend.repository.items.importexport.wizard.models.ItemImportNode;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.dialog.AProgressMonitorDialogWithCancel;

/**
 *
 * DOC ggu class global comment. Detailled comment
 */
public class ImportItemsWizardPage extends WizardPage {

    private static final String TYPE_BEANS = "BEANS";
    private static final String TALEND_FILE_NAME = "talend.project";

    private Button itemFromDirectoryRadio, itemFromArchiveRadio;

    private Text directoryPathField, archivePathField;

    protected Button browseDirectoriesButton, browseArchivesButton;

    protected Button dependencyButton;
    
    protected Button requiredFeatureButton;

    protected FilteredCheckboxTree filteredCheckboxTree;

    private TableViewer errorsListViewer;

    private final List<String> errors = new ArrayList<String>();

    protected Button overwriteButton;
                                                                                                       
    /*
     *
     */
    private static final String[] ARCHIVE_FILE_MASK = { "*.jar;*.zip;*.tar;*.tar.gz;*.tgz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    protected String previouslyBrowsedDirectoryPath, previouslyBrowsedArchivePath, lastWorkedPath;

    protected List<ImportItem> selectedItemRecords = new ArrayList<ImportItem>();

    private final ImportNodesBuilder nodesBuilder = new ImportNodesBuilder();

    protected ResourcesManager resManager;

    private IStructuredSelection selection;

    private final ImportExportHandlersManager importManager = new ImportExportHandlersManager();

    private Button regenIdBtn;

    /**
     *
     * DOC ggu ImportItemsWizardPage constructor comment.
     *
     * @param pageName
     */
    public ImportItemsWizardPage(String pageName, IStructuredSelection s) {
        super(pageName);
        this.selection = s;
        setDescription(Messages.getString("ImportItemsWizardPage_importDescription")); //$NON-NLS-1$
        // setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_WIZBAN_IMPORT_WIZ")); //$NON-NLS-1$
    }

    public IStructuredSelection getSelection() {
        return this.selection;
    }

    @Override
    public void createControl(Composite parent) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
        setControl(scrolledComposite);
        scrolledComposite.setLayout(new GridLayout());
        scrolledComposite.setLayoutData( new GridData(GridData.FILL_BOTH));
        
        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        scrolledComposite.setContent(composite);
        
        createSelectionArea(composite);
        createImportDependenciesArea(composite);
        createItemListArea(composite);
        createErrorsListArea(composite);
        createAdditionArea(composite);
        
        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        Dialog.applyDialogFont(composite);
    }

    private void createSelectionArea(Composite parent) {
        Composite selectionArea = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        selectionArea.setLayout(layout);
        selectionArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // from directory
        createDirectorySelectionArea(selectionArea);

        // from archive
        createArchiveSelectionArea(selectionArea);

        // from directory by default.
        this.itemFromDirectoryRadio.setSelection(true);
        updateSelectionFields(this.itemFromDirectoryRadio.getSelection());
    }

    /**
     * DOC ggu Comment method "createDirectorySelectionArea".
     *
     * @param selectionArea
     */
    private void createDirectorySelectionArea(Composite selectionArea) {
        this.itemFromDirectoryRadio = new Button(selectionArea, SWT.RADIO);
        this.itemFromDirectoryRadio.setText(Messages.getString("ImportItemsWizardPage_selectDirectoryText")); //$NON-NLS-1$
        setButtonLayoutData(this.itemFromDirectoryRadio);

        this.itemFromDirectoryRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDirectoryRadioSelected();
            }
        });

        this.directoryPathField = new Text(selectionArea, SWT.BORDER);
        this.directoryPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        this.directoryPathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    updateItemsList(directoryPathField.getText().trim(), true, false);
                }
            }

        });
        this.directoryPathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                updateItemsList(directoryPathField.getText().trim(), true, false);
            }

        });

        this.browseDirectoriesButton = new Button(selectionArea, SWT.PUSH);
        this.browseDirectoriesButton.setText(Messages.getString("ImportItemsWizardPage_browseText")); //$NON-NLS-1$
        setButtonLayoutData(this.browseDirectoriesButton);
        this.browseDirectoriesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleBrowseDirectoryButtonPressed();
            }
        });

        // just fill the empty
        new Label(selectionArea, SWT.NONE);
    }

    /**
     * DOC ggu Comment method "createArchiveSelectionArea".
     *
     * @param selectionArea
     */
    protected void createArchiveSelectionArea(Composite selectionArea) {
        this.itemFromArchiveRadio = new Button(selectionArea, SWT.RADIO);
        this.itemFromArchiveRadio.setText(Messages.getString("ImportItemsWizardPage_selectArchiveText")); //$NON-NLS-1$
        setButtonLayoutData(this.itemFromArchiveRadio);

        this.itemFromArchiveRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleArchiveRadioSelected();
            }
        });

        this.archivePathField = new Text(selectionArea, SWT.BORDER);
        this.archivePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        this.archivePathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    updateItemsList(archivePathField.getText().trim(), false, false);
                }
            }
        });

        this.archivePathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                updateItemsList(archivePathField.getText().trim(), false, false);
            }
        });

        this.browseArchivesButton = new Button(selectionArea, SWT.PUSH);
        this.browseArchivesButton.setText(Messages.getString("ImportItemsWizardPage_browseText")); //$NON-NLS-1$
        setButtonLayoutData(this.browseArchivesButton);

        this.browseArchivesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleArchiveButtonPressed();
            }
        });

    }

    private void updateSelectionFields(boolean fromDir) {
        this.directoryPathField.setEnabled(fromDir);
        this.browseDirectoriesButton.setEnabled(fromDir);

        this.archivePathField.setEnabled(!fromDir);
        this.browseArchivesButton.setEnabled(!fromDir);
    }

    protected void createImportDependenciesArea(Composite parent) {
        Composite dependencyArea = new Composite(parent, SWT.None);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.makeColumnsEqualWidth = false;
        dependencyArea.setLayout(layout);
        dependencyArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        dependencyButton = new Button(dependencyArea, SWT.CHECK);
        dependencyButton.setText(Messages.getString("ImportItemsWizardPage_importDependenciesText"));
        setButtonLayoutData(dependencyButton);
        dependencyButton.setSelection(getImportDependenciesPref());
        dependencyButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dependencyButton.getSelection()) {
                    handleImportDependencies();
                }
                filteredCheckboxTree.calculateCheckedLeafNodes();
                checkSelectedItemErrors();
            }
        });
           
        if (IBrandingService.get().isPoweredbyTalend()) {
            Composite requiredCom = new Composite(dependencyArea, SWT.None);
            GridLayout requiredComLayout = new GridLayout();
            requiredComLayout.numColumns = 2;
            requiredComLayout.marginWidth = 0;
            requiredComLayout.marginHeight = 0;
            requiredComLayout.horizontalSpacing = 0;
            requiredComLayout.makeColumnsEqualWidth = false;
            requiredCom.setLayout(requiredComLayout);
            requiredCom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            requiredFeatureButton = new Button(requiredCom, SWT.CHECK);
            requiredFeatureButton.setText(Messages.getString("ImportItemsWizardPage_AnalyseRequiredFeatureButton"));
            requiredFeatureButton.setToolTipText(Messages.getString("ImportItemsWizardPage_AnalyseRequiredFeatureButtonTooltip"));
            requiredFeatureButton.setSelection(true);
            setButtonLayoutData(requiredFeatureButton);
            requiredFeatureButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (requiredFeatureButton.getSelection()) {
                        calRequiredFeatures();
                    }
                }
            });
            Label imageLabel = new Label(requiredCom, SWT.NONE);
            imageLabel.setImage(ImageProvider.getImage(EImage.WARNING_ICON));
            imageLabel.setLayoutData(new GridData());
            imageLabel.setToolTipText(Messages.getString("ImportItemsWizardPage_AnalyseRequiredFeatureButtonTooltip"));
        }  
    }
    
    private boolean calRequiredFeatures() {
        IStudioLiteP2Service p2Service = IStudioLiteP2Service.get();
        if (this.resManager != null && p2Service != null) {
            Map<String, Set<IInstallableUnitInfo>> projectToFeatureMap = new HashMap<String, Set<IInstallableUnitInfo>>();
            for (IPath path : resManager.getPaths()) {
                if (path.lastSegment().equals(TALEND_FILE_NAME)) {
                    projectToFeatureMap.put(path.removeLastSegments(1).toPortableString(), null);
                }
            }

            AProgressMonitorDialogWithCancel<Boolean> dialogWithCancel = new AProgressMonitorDialogWithCancel<Boolean>(
                    getShell()) {

                @Override
                protected Boolean runWithCancel(IProgressMonitor monitor) throws Throwable {
                    for (String projectPath : projectToFeatureMap.keySet()) {
                        projectToFeatureMap.put(projectPath, p2Service.calAllRequiredFeature(monitor, projectPath, true));
                    }
                    Set<IInstallableUnitInfo> allRequiredFeatures = new HashSet<IInstallableUnitInfo>();
                    for (String projectPath : projectToFeatureMap.keySet()) {
                        allRequiredFeatures.addAll(projectToFeatureMap.get(projectPath));
                    }
                    if (allRequiredFeatures.size() > 0) {
                        return p2Service.showMissingFeatureWizard(monitor, allRequiredFeatures);
                    } else {
                        return true;
                    }
                }
            };

            String executingMessage = Messages.getString("ImportItemsWizardPage_ProgressDialog_ExecutingMessage"); //$NON-NLS-1$
            String waitingFinishMessage = Messages.getString("ImportItemsWizardPage_ProgressDialog_WaitingCheckRequiredFeature"); //$NON-NLS-1$
            try {
                dialogWithCancel.run(executingMessage, waitingFinishMessage, true, AProgressMonitorDialogWithCancel.ENDLESS_WAIT_TIME);
                Throwable executeException = dialogWithCancel.getExecuteException();
                if (executeException != null) {
                    throw new Exception(executeException);
                }
            } catch (Exception ex) {
                ExceptionHandler.process(ex);
            }
        }
        return false;
    }

    protected boolean getImportDependenciesPref() {
        IPreferenceStore repositoryPreferenceStore = RepositoryManager.getRepositoryPreferenceStore();
        if (repositoryPreferenceStore != null) {
            String option = repositoryPreferenceStore.getString(IRepositoryPrefConstants.ITEM_IMPORT_DEPENDENCIES);
            return StringUtils.isBlank(option) ? true : Boolean.valueOf(option);
        }
        return false;
    }

    protected void saveImportDependenciesPref() {
        IPreferenceStore repositoryPreferenceStore = RepositoryManager.getRepositoryPreferenceStore();
        if (repositoryPreferenceStore != null) {
            repositoryPreferenceStore.setValue(IRepositoryPrefConstants.ITEM_IMPORT_DEPENDENCIES,
                    dependencyButton.getSelection() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        }
    }

    protected void createItemListArea(Composite parent) {
        Composite itemsArea = new Composite(parent, SWT.NONE);
        GridLayout layout2 = new GridLayout();
        layout2.numColumns = 2;
        layout2.marginWidth = 0;
        layout2.makeColumnsEqualWidth = false;
        itemsArea.setLayout(layout2);

        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        gridData.heightHint = 250;
        gridData.widthHint = 600;
        itemsArea.setLayoutData(gridData);

        createItemsTreeViewer(itemsArea);

        createItemsListButtonsArea(itemsArea);

    }

    protected TreeViewer createItemsTreeViewer(Composite parent) {
        filteredCheckboxTree = new FilteredCheckboxTree(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        CheckboxTreeViewer viewer = filteredCheckboxTree.getViewer();

        viewer.setContentProvider(new ImportItemsViewerContentProvider());
        viewer.setLabelProvider(new ImportItemsViewerLabelProvider());
        viewer.setSorter(new ImportItemsViewerSorter());
        viewer.addFilter(new ImportItemsViewerFilter());
        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (dependencyButton.getSelection()) {
                    handleImportDependencies();
                }
                filteredCheckboxTree.calculateCheckedLeafNodes();
                checkSelectedItemErrors();
            }
        });
        viewer.setInput(nodesBuilder.getProjectNodes());
        return viewer;
    }

    private void createItemsListButtonsArea(Composite listComposite) {
        Composite buttonsComposite = new Composite(listComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 26;
        buttonsComposite.setLayout(layout);

        buttonsComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

//        Button refresh = new Button(buttonsComposite, SWT.PUSH);
//        refresh.setText(Messages.getString("ImportItemsWizardPage_refreshButtonText")); //$NON-NLS-1$
//        refresh.addSelectionListener(new SelectionAdapter() {
//
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                if (itemFromDirectoryRadio.getSelection()) {
//                    updateItemsList(directoryPathField.getText().trim(), true, true);
//                } else {
//                    updateItemsList(archivePathField.getText().trim(), false, true);
//                }
//            }
//        });
//        setButtonLayoutData(refresh);
//        // hide for current version ,enable it later if needed.
//        refresh.setVisible(false);

        Button selectAll = new Button(buttonsComposite, SWT.PUSH);
        selectAll.setText(Messages.getString("ImportItemsWizardPage_selectButtonText")); //$NON-NLS-1$
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                final CheckboxTreeViewer viewer = filteredCheckboxTree.getViewer();
                if (viewer.getTree().getItemCount() > 0) {
                    for (int i = 0; i < viewer.getTree().getItemCount(); i++) {
                        TreeItem topItem = viewer.getTree().getItem(i)/* .getTopItem() */;
                        if (topItem != null) {
                            viewer.setSubtreeChecked(topItem.getData(), true);
                        }
                    }
                    filteredCheckboxTree.calculateCheckedLeafNodes();
                    checkSelectedItemErrors();
                }
            }
        });
        setButtonLayoutData(selectAll);

        Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
        deselectAll.setText(Messages.getString("ImportItemsWizardPage_deselectAllButtonText")); //$NON-NLS-1$
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                filteredCheckboxTree.getViewer().setCheckedElements(new Object[0]);
                filteredCheckboxTree.calculateCheckedLeafNodes();
                checkSelectedItemErrors();
            }
        });
        setButtonLayoutData(deselectAll);

        Button expandAll = new Button(buttonsComposite, SWT.PUSH);
        expandAll.setText(Messages.getString("ImportItemsWizardPage_expandAllButtonText")); //$NON-NLS-1$
        expandAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                filteredCheckboxTree.getViewer().expandAll();
            }
        });
        setButtonLayoutData(expandAll);

        Button collapseAll = new Button(buttonsComposite, SWT.PUSH);
        collapseAll.setText(Messages.getString("ImportItemsWizardPage_collapseAllButtonText")); //$NON-NLS-1$
        collapseAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                filteredCheckboxTree.getViewer().collapseAll();
            }
        });
        setButtonLayoutData(collapseAll);
    }

    protected void createErrorsListArea(Composite workArea) {
        Composite composite = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        gridData.heightHint = 100;
        composite.setLayoutData(gridData);

        Label title = new Label(composite, SWT.NONE);
        title.setText(Messages.getString("ImportItemsWizardPage_messagesText")); //$NON-NLS-1$
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        errorsListViewer = new TableViewer(composite, SWT.BORDER);
        errorsListViewer.getControl().setLayoutData(gridData);

        errorsListViewer.setContentProvider(new ArrayContentProvider());
        errorsListViewer.setLabelProvider(new LabelProvider());
        errorsListViewer.setSorter(new ViewerSorter());

    }

    /**
     * DOC ggu Comment method "createAdditionArea".
     *
     * @param workArea
     */
    protected void createAdditionArea(Composite workArea) {
        Composite optionsArea = new Composite(workArea, SWT.NONE);
        FormLayout optAreaLayout = new FormLayout();
        optionsArea.setLayout(optAreaLayout);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        optionsArea.setLayoutData(gridData);

//        Group internalIdGroup = new Group(optionsArea, SWT.NONE);
//        internalIdGroup.setText(Messages.getString("ImportItemsWizardPage_internalIdGroup"));
//        internalIdGroup.setLayout(new GridLayout(1, true));
//        FormData internalIdGroupLayoutData = new FormData();
//        internalIdGroupLayoutData.top = new FormAttachment(0);
//        internalIdGroupLayoutData.left = new FormAttachment(0);
//        internalIdGroup.setLayoutData(internalIdGroupLayoutData);
//
//        regenIdBtn = new Button(internalIdGroup, SWT.RADIO);
//        regenIdBtn.setText(Messages.getString("ImportItemsWizardPage_internalIdGroup_alwaysRegenId"));
//
//        Button keepOrigIdBtn = new Button(internalIdGroup, SWT.RADIO);
//        keepOrigIdBtn.setText(Messages.getString("ImportItemsWizardPage_internalIdGroup_keepOrigId"));
//        keepOrigIdBtn.setSelection(true);
//
//        // see feature 3949
//        this.overwriteButton = new Button(optionsArea, SWT.CHECK);
//        this.overwriteButton.setText(Messages.getString("ImportItemsWizardPage_overwriteItemsTxt")); //$NON-NLS-1$
//        this.overwriteButton.addSelectionListener(new SelectionAdapter() {
//
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                if (StringUtils.isNotEmpty(directoryPathField.getText()) || StringUtils.isNotEmpty(archivePathField.getText())) {
//                    populateItems(overwriteButton.getSelection(), true);
//                }
//            }
//
//        });
//        FormData overwriteLayoutData = new FormData();
//        overwriteLayoutData.top = new FormAttachment(internalIdGroup, 5, SWT.BOTTOM);
//        overwriteLayoutData.left = new FormAttachment(internalIdGroup, 0, SWT.LEFT);
//        this.overwriteButton.setLayoutData(overwriteLayoutData);
//
//        internalIdGroup.setVisible(false);
        
        // see feature 3949
        this.overwriteButton = new Button(optionsArea, SWT.CHECK);
        this.overwriteButton.setText(Messages.getString("ImportItemsWizardPage_overwriteItemsTxt")); //$NON-NLS-1$
        this.overwriteButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (StringUtils.isNotEmpty(directoryPathField.getText()) || StringUtils.isNotEmpty(archivePathField.getText())) {
                    populateItems(overwriteButton.getSelection(), true);
                }
            }

        });
        FormData overwriteLayoutData = new FormData();
        overwriteLayoutData.top = new FormAttachment(optionsArea, 5, SWT.BOTTOM);
        overwriteLayoutData.left = new FormAttachment(optionsArea, 0, SWT.LEFT);
        this.overwriteButton.setLayoutData(overwriteLayoutData);
    }

    /**
     * From directory
     *
     */
    private void handleDirectoryRadioSelected() {
        boolean selection = this.itemFromDirectoryRadio.getSelection();
        updateSelectionFields(selection);
        if (selection) {
            this.directoryPathField.setFocus();
            updateItemsList(this.directoryPathField.getText().trim(), true, false);
        }
    }

    /**
     * From directory
     *
     */
    private void handleBrowseDirectoryButtonPressed() {
        DirectoryDialog dialog = new DirectoryDialog(this.getShell());
        dialog.setText(Messages.getString("ImportItemsWizardPage_selectDirectoryDialogTitle")); //$NON-NLS-1$
        dialog.setMessage(dialog.getText()); // FIXME

        String dirPath = this.directoryPathField.getText().trim();
        if (dirPath.length() == 0 && previouslyBrowsedDirectoryPath != null) {
            dirPath = previouslyBrowsedDirectoryPath;
        }

        if (dirPath.length() == 0) {
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(dirPath);
            if (path.exists()) {
                dialog.setFilterPath(new Path(dirPath).toOSString());
            }
        }

        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            this.directoryPathField.setText(selectedDirectory);
            previouslyBrowsedDirectoryPath = selectedDirectory;
            updateItemsList(selectedDirectory, true, false);
        }

    }

    /**
     * From archive
     *
     */
    private void handleArchiveRadioSelected() {
        boolean selection = this.itemFromArchiveRadio.getSelection();
        updateSelectionFields(!selection);
        if (selection) {
            this.archivePathField.setFocus();
            updateItemsList(this.archivePathField.getText().trim(), false, false);
        }
    }

    /**
     * From archive
     *
     */
    private void handleArchiveButtonPressed() {
        FileDialog dialog = new FileDialog(archivePathField.getShell());
        dialog.setText(Messages.getString("ImportItemsWizardPage_selectArchiveDialogTitle")); //$NON-NLS-1$
        dialog.setFilterExtensions(ARCHIVE_FILE_MASK);

        String filePath = this.archivePathField.getText().trim();
        if (filePath.length() == 0 && previouslyBrowsedArchivePath != null) {
            filePath = previouslyBrowsedArchivePath;
        }

        if (filePath.length() == 0) {
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File file = new File(filePath);
            if (file.exists()) {
                dialog.setFilterPath(new Path(filePath).toOSString());
            }
        }

        String selectedArchive = dialog.open();
        if (selectedArchive != null) {
            this.archivePathField.setText(selectedArchive);
            previouslyBrowsedArchivePath = selectedArchive;
            updateItemsList(selectedArchive, false, false);
        }

    }

    protected void handleImportDependencies() {
        CheckboxTreeViewer viewer = filteredCheckboxTree.getViewer();
        if (viewer.getTree().getItemCount() == 0) {
            return;
        }
        Object[] checkedElements = viewer.getCheckedElements();
        if (checkedElements.length == 0) {
            return;
        }

        List<ItemImportNode> checkedNodeList = new ArrayList<ItemImportNode>();
        Set<ItemImportNode> toSelectSet = new HashSet<ItemImportNode>();
        for (Object object : checkedElements) {
            if (object instanceof ItemImportNode) {
                checkedNodeList.add((ItemImportNode)object);
                toSelectSet.add((ItemImportNode) object);
            }
        }
        if (nodesBuilder.getAllImportItemNode().size() == checkedNodeList.size()) {
            // already checked all
            return;
        }
        List<ItemImportNode> allImportItemNode = nodesBuilder.getAllImportItemNode();
        ImportDependencyRelationsHelper.getInstance().checkImportRelationDependency(checkedNodeList, toSelectSet,
                allImportItemNode);

        ITaCoKitService coKitService = ITaCoKitService.getInstance();

        Set<ItemImportNode> parentNodetoSelectSet = new HashSet<ItemImportNode>();

        if (coKitService != null) {
            for (ItemImportNode itemImportNode : toSelectSet) {

                ImportItem itemRecord = itemImportNode.getItemRecord();

                if (itemRecord != null) {
                    ERepositoryObjectType repositoryType = itemRecord.getRepositoryType();

                    boolean isTaCoKitType = coKitService.isTaCoKitType(repositoryType);

                    if (isTaCoKitType) {
                        IImportItemsHandler importHandler = itemRecord.getImportHandler();
                        if (importHandler instanceof ImportBasicHandler) {
                            ((ImportBasicHandler) importHandler).resolveItem(resManager, itemRecord);
                            Item item = itemRecord.getProperty().getItem();
                            String parentItemId = coKitService.getParentItemIdFromItem(item);
                            if (StringUtils.isNotBlank(parentItemId)) {
                                for (ItemImportNode importNode : allImportItemNode) {
                                    ImportItem importItem = importNode.getItemRecord();
                                    if (importItem != null && importItem.getProperty() != null
                                            && StringUtils.equals(parentItemId, importItem.getProperty().getId())) {
                                        parentNodetoSelectSet.add(importNode);
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        if (parentNodetoSelectSet.size() > 0) {

            toSelectSet.addAll(parentNodetoSelectSet);
        }
        // to make doCheckStateChanged execute from ContainerCheckedTreeViewer.setCheckedElements(Object[])
        filteredCheckboxTree.getViewer().setCheckedElements(new Object[0]);
        filteredCheckboxTree.getViewer().setCheckedElements(toSelectSet.toArray());
    }

    public void updateItemsList(final String path, final boolean fromDir/* Unuseful */, boolean isneedUpdate) {
        // if not force to update, and same as before path, nothing to do.
        if (!isneedUpdate && path.equals(lastWorkedPath)) {
            return;
        }
        lastWorkedPath = path;

        CheckboxTreeViewer viewer = filteredCheckboxTree.getViewer();

        if (StringUtils.isEmpty(path)) {
            selectedItemRecords.clear();
            viewer.refresh(true);
            // get the top item to check if tree is empty, if not then uncheck everything
            TreeItem topItem = viewer.getTree().getTopItem();
            if (topItem != null) {
                viewer.setSubtreeChecked(topItem.getData(), false);
            } // else not root element, tree is already empty
        } else {

            File srcFile = new File(path);
            try {
                final FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance().createFileUnityManager(
                        srcFile);
                AProgressMonitorDialogWithCancel<ResourcesManager> dialogWithCancel = new AProgressMonitorDialogWithCancel<ResourcesManager>(
                        getShell()) {

                    @Override
                    protected ResourcesManager runWithCancel(IProgressMonitor monitor) throws Throwable {
                        return fileUnityManager.doUnify(true);
                    }
                };
                String executingMessage = Messages.getString("ImportItemsWizardPage_ProgressDialog_ExecutingMessage"); //$NON-NLS-1$
                String waitingFinishMessage = Messages.getString("ImportItemsWizardPage_ProgressDialog_WaitingFinishMessage"); //$NON-NLS-1$
                dialogWithCancel.run(executingMessage, waitingFinishMessage, true,
                        AProgressMonitorDialogWithCancel.ENDLESS_WAIT_TIME);
                Throwable executeException = dialogWithCancel.getExecuteException();
                if (executeException != null) {
                    throw executeException;
                }
                resManager = dialogWithCancel.getExecuteResult();
            } catch (FileNotFoundException e) {
                return; // file is not existed
            } catch (ZipException e) {
                displayErrorDialog(Messages.getString("ImportItemsWizardPage_ZipImport_badFormat")); //$NON-NLS-1$
                // if folder, won't have errors.
                archivePathField.setFocus();
            } catch (TarException e) {
                displayErrorDialog(Messages.getString("ImportItemsWizardPage_TarImport_badFormat")); //$NON-NLS-1$
                // if folder, won't have errors.
                archivePathField.setFocus();
            } catch (IOException e) {
                displayErrorDialog(Messages.getString("ImportItemsWizardPage_couldNotRead")); //$NON-NLS-1$
                // if folder, won't have errors.
                archivePathField.setFocus();
            } catch (Throwable e) {
                displayErrorDialog(e.getMessage());
                archivePathField.setFocus();
            }

            if (resManager == null) {
                setErrorMessage(Messages.getString("ImportItemsWizardPage_noValidItemsInPathMessage")); //$NON-NLS-1$
                setPageComplete(false);
            } else {
                populateItems(this.overwriteButton == null ? false : this.overwriteButton.getSelection());
            }
            if (requiredFeatureButton != null && requiredFeatureButton.getSelection()) {
                calRequiredFeatures();
            }
        }

    }

    private void checkValidItemRecords() {
        ImportItem[] validItems = getValidItemRecords();
        boolean hasValidItems = validItems.length > 0;

        if (hasValidItems) {
            this.setErrorMessage(null);
        } else {
            this.setErrorMessage(Messages.getString("ImportItemsWizardPage_noValidItemsInPathMessage")); //$NON-NLS-1$
        }
        setPageComplete(hasValidItems);
    }

    public ImportItem[] getValidItemRecords() {

        List<ImportItem> validItems = new ArrayList<ImportItem>();
        for (ImportItem item : this.selectedItemRecords) {
            if (item.isValid()) {
                validItems.add(item);

            }
        }
        return validItems.toArray(new ImportItem[0]);
    }

    protected void displayErrorDialog(String message) {
        MessageDialog.openError(getContainer().getShell(), Messages.getString("ImportItemsWizardPage_errorTitle"), message); //$NON-NLS-1$
    }

    protected void populateItems(final boolean overwrite, boolean keepSelection) {

        setPageComplete(true);
        this.selectedItemRecords.clear();
        // importItemUtil.clearAllData();
        nodesBuilder.clear();
        errors.clear();
        updateErrorListViewer();

        if (resManager != null) { // if resource is not init successfully.
            IRunnableWithProgress op = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    final ResourceOption importOption = ResourceOption.ITEM_IMPORTATION;
                    try {
                        EmfResourcesFactoryReader.INSTANCE.addOption(importOption, true);
                        List<ImportItem> items = importManager.populateImportingItems(resManager, overwrite, monitor, true);
                        nodesBuilder.addItems(items);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    } finally {
                        EmfResourcesFactoryReader.INSTANCE.removOption(importOption, true);
                    }
                }

            };
            try {
                new ProgressMonitorDialog(getShell()).run(true, true, op);
            } catch (Exception e) {
                displayErrorDialog(e.getMessage());
            }
        }

        ImportItem[] allImportItemRecords = nodesBuilder.getAllImportItemRecords();
        for (ImportItem itemRecord : allImportItemRecords) {
            // bug 21738
            if (itemRecord.getExistingItemWithSameId() != null
                    && itemRecord.getExistingItemWithSameId() instanceof RepositoryViewObject) {
                RepositoryViewObject reObject = (RepositoryViewObject) itemRecord.getExistingItemWithSameId();
                if (itemRecord.getProperty() != null && reObject != null) {
                    if (itemRecord.getProperty().getId().equals(reObject.getId())
                            && itemRecord.getProperty().getLabel().equals(reObject.getLabel())) {
                        if (itemRecord.getProperty().getVersion().equals(reObject.getVersion())) {
                            for (String error : itemRecord.getErrors()) {
                                errors.add("'" + itemRecord.getItemName() + "' " + error); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            errors.add(Messages.getString(
                                    "ImportItemsWizardPage_ErrorsMessage", itemRecord.getItemName(), reObject.getVersion())); //$NON-NLS-1$
                        }
                    } else {
                        // TDI-21399,TDI-21401
                        // if item is locked, cannot overwrite
                        ERepositoryStatus status = reObject.getRepositoryStatus();
                        if (status == ERepositoryStatus.LOCK_BY_OTHER || status == ERepositoryStatus.LOCK_BY_USER) {
                            for (String error : itemRecord.getErrors()) {
                                errors.add("'" + itemRecord.getItemName() + "' " + error); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
            } else {
                if (itemRecord.getProperty() != null) {
                    for (String error : itemRecord.getErrors()) {
                        errors.add("'" + itemRecord.getItemName() + "' " + error); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }

        }

        updateErrorListViewer();

        selectedItemRecords.addAll(Arrays.asList(allImportItemRecords));

        final CheckboxTreeViewer viewer = this.filteredCheckboxTree.getViewer();
        viewer.setInput(nodesBuilder.getProjectNodes());
        viewer.refresh(true);
        viewer.expandAll();
        if (keepSelection) {
            Object[] checkedLeafNodes = filteredCheckboxTree.getCheckedLeafNodes();
            Set<ItemImportNode> newCheckedElement = new HashSet<ItemImportNode>();
            for (Object obj : checkedLeafNodes) {
                if (obj instanceof ItemImportNode) {
                    ItemImportNode importItem = (ItemImportNode) obj;
                    ImportItem record = importItem.getItemRecord();
                    for (ItemImportNode node : nodesBuilder.getAllImportItemNode()) {
                        ImportItem itemRecord = node.getItemRecord();
                        if (record.getPath() != null && record.getPath().equals(itemRecord.getPath())) {
                            newCheckedElement.add(node);
                            break;
                        }
                    }
                }
            }

            viewer.setCheckedElements(newCheckedElement.toArray());
            filteredCheckboxTree.resetCheckedElements();
            filteredCheckboxTree.calculateCheckedLeafNodes();
        } else {
            filteredCheckboxTree.resetCheckedElements();
        }

        checkValidItemRecords();
        if (this.isPageComplete()) {// if not valid already. no need check.
            checkSelectedItemErrors();
        }

    }

    protected void populateItems(final boolean overwrite) {
        populateItems(overwrite, false);
    }

    private void checkSelectedItemErrors() {
        List<ImportItem> checkedElements = getCheckedElements();
        if (checkedElements.isEmpty()) {
            setErrorMessage(Messages.getString("ImportItemsWizardPage_noSelectedItemsMessages")); //$NON-NLS-1$
            setPageComplete(false);
        } else {
            updateErrorMessage(checkedElements);
            if (getErrorMessage() != null) {
                setPageComplete(false);
            } else {
                setPageComplete(true);
            }
        }
    }

    private void updateErrorListViewer() {
        errorsListViewer.setInput(errors);
        errorsListViewer.refresh();
    }

    private List<ImportItem> getCheckedElements() {
        // add this if user use filter
        Set<ImportNode> checkedElements = new HashSet<ImportNode>();
        for (Object obj : filteredCheckboxTree.getCheckedLeafNodes()) {
            if (obj instanceof ImportNode) {
                checkedElements.add((ImportNode) obj);
            }
        }
        // add this if user does not use filter
        for (Object obj : filteredCheckboxTree.getViewer().getCheckedElements()) {
            if (obj instanceof ImportNode) {
                checkedElements.add((ImportNode) obj);
            }
        }
        // sort the item
        List<ImportNode> list = new ArrayList<ImportNode>(checkedElements);
        Collections.sort(list);

        List<ImportItem> items = new ArrayList<ImportItem>(list.size());
        for (ImportNode node : list) {
            if (node.getItemRecord() != null) {
                items.add(node.getItemRecord());
            }
        }
        return items;
    }

    private List<EmptyFolderImportItem> getCheckedFolders() {
        List<EmptyFolderImportItem> checkedEmptyFolder = new ArrayList<EmptyFolderImportItem>();

        // add this if user does not use filter
        for (Object obj : filteredCheckboxTree.getViewer().getCheckedElements()) {
            if (obj instanceof FolderImportNode) {
                ImportItem itemRecord = ((FolderImportNode) obj).getItemRecord();
                if (itemRecord instanceof EmptyFolderImportItem) {
                    checkedEmptyFolder.add((EmptyFolderImportItem) itemRecord);
                }
            }
        }

        return checkedEmptyFolder;
    }

    /**
     * Checks for consistency in selected elements and report an error message. in case of error or null the message
     * error.
     *
     * @param checkedElements element to be checked
     */
    private void updateErrorMessage(List<ImportItem> checkedElements) {
        String errorMessage = checkErrorFor2ItemsWithSameIdAndVersion(checkedElements);
        setErrorMessage(errorMessage);
    }

    /**
     * This check that 2 items in the list do not have the same Id and the same version. if that is so the return an
     * error message else return null.
     *
     * @param checkedElementsn the element to be checked
     * @return an error message or null if no error.
     */
    private String checkErrorFor2ItemsWithSameIdAndVersion(List<ImportItem> checkedElements) {
        String errorMessage = null;
        HashMap<String, ImportItem> duplicateCheckMap = new HashMap<String, ImportItem>();
        for (ImportItem itRecord : checkedElements) {
            if (itRecord instanceof EmptyFolderImportItem) {
                continue;
            }
            ImportItem otherRecord = duplicateCheckMap.put(itRecord.getProperty().getId() + itRecord.getProperty().getVersion(),
                    itRecord);
            if (otherRecord != null) {
                errorMessage = Messages.getString(
                        "ImportItemsWizardPage_sameIdProblemMessage", itRecord.getPath(), otherRecord.getPath()); //$NON-NLS-1$
            }// else keep going
        }
        return errorMessage;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        if (selectedItemRecords.isEmpty() || getErrorMessage() != null) {
            return false;
        }
        return super.isPageComplete();
    }

    public boolean performCancel() {
        // Check Error Items
        final List<String> errors = new ArrayList<String>();
        errors.addAll(ImportCacheHelper.getInstance().getImportErrors());
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!errors.isEmpty()) {
                    ShowErrorsDuringImportItemsDialog dialog = new ShowErrorsDuringImportItemsDialog(Display.getCurrent()
                            .getActiveShell(), errors);
                    dialog.open();
                    ImportCacheHelper.getInstance().getImportErrors().clear();
                }
            }
        });
        selectedItemRecords.clear();
        nodesBuilder.clear();
        return true;
    }

    public boolean performFinish() {
        final List<ImportItem> checkedItemRecords = getCheckedElements();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        importManager.getPendoImportManager().cacheItemProperty(checkedItemRecords);
        
        /*
         * ?? prepare to do import, unlock the existed one, and make sure the overwrite to work well.
         */
        for (ImportItem itemRecord : checkedItemRecords) {
            Item item = itemRecord.getProperty().getItem();
            if (item.getState().isLocked()) {
                try {
                    factory.unlock(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                } catch (LoginException e) {
                    ExceptionHandler.process(e);
                }
            }
            ERepositoryStatus status = factory.getStatus(item);
            if (status != null && status == ERepositoryStatus.LOCK_BY_USER) {
                try {
                    factory.unlock(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                } catch (LoginException e) {
                    ExceptionHandler.process(e);
                }
            }
        }

        final boolean overwrite = overwriteButton == null ? false : overwriteButton.getSelection();
        final boolean alwaysRegenId = regenIdBtn == null ? false : regenIdBtn.getSelection();
        try {
            IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IPath destinationPath = null;
                    Object firstElement = getSelection().getFirstElement();
                    if (firstElement != null && firstElement instanceof RepositoryNode) {
                        final RepositoryNode rNode = (RepositoryNode) firstElement;
                        if (rNode.getType() == IRepositoryNode.ENodeType.SIMPLE_FOLDER) {
                            destinationPath = RepositoryNodeUtilities.getPath(rNode);
                            // add the type of path
                            ERepositoryObjectType contentType = rNode.getContentType();
                            if (contentType.isResouce()) {
                                IPath typePath = new Path(contentType.getFolder());
                                if (!typePath.isPrefixOf(destinationPath)) {
                                    destinationPath = typePath.append(destinationPath);
                                }
                            }
                        }
                    }
                    final ResourceOption importOption = ResourceOption.ITEM_IMPORTATION;
                    try {
                        EmfResourcesFactoryReader.INSTANCE.addOption(importOption, false);
                        importManager.getPendoImportManager().setStudioImport(true);
                        importManager.importItemRecords(monitor, resManager, checkedItemRecords, overwrite,
                                nodesBuilder.getAllImportItemRecords(), destinationPath, alwaysRegenId);
                    } finally {
                        EmfResourcesFactoryReader.INSTANCE.removOption(importOption, false);
                        importManager.getPendoImportManager().sendTrackToPendo();
                    }
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
                                IEditorPart activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
                                if (activeEditor instanceof IMultiPageTalendEditor) {
                                    IMultiPageTalendEditor multiPageTEditor = (IMultiPageTalendEditor) activeEditor;
                                    multiPageTEditor.changePaletteComponentHandler();
                                    ComponentPaletteUtilities.updateFromRepositoryType(ERepositoryObjectType
                                            .getItemType(((IMultiPageTalendEditor) activeEditor).getProcess().getProperty()
                                                    .getItem()));
                                }
                            }
                        }
                    });
                    // FIXME add back since it could avoid deadlock "luckily"
                    // remove this part later since no use to update here
                    // regression check must be done for TUP-25372 & TESB-27401
                    MavenPomSynchronizer.addChangeLibrariesListener();
                    new AggregatorPomsHelper().updateCodeProjects(new NullProgressMonitor(), false);
                }
            };

            new ProgressMonitorDialog(getShell()).run(true, true, iRunnableWithProgress);

        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            // clean
            if (resManager != null) {
                resManager.closeResource();
            }
            // Check Error Items
            final List<String> errors = new ArrayList<String>();
            for (ImportItem itemRecord : checkedItemRecords) {
                errors.addAll(itemRecord.getErrors());
            }
            errors.addAll(ImportCacheHelper.getInstance().getImportErrors());
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!errors.isEmpty()) {
                        ShowErrorsDuringImportItemsDialog dialog = new ShowErrorsDuringImportItemsDialog(Display.getCurrent()
                                .getActiveShell(), errors);
                        dialog.open();
                        ImportCacheHelper.getInstance().getImportErrors().clear();
                    }
                }
            });
            checkedItemRecords.clear();
            nodesBuilder.clear();
        }
        saveImportDependenciesPref();
        MigrationReportHelper.getInstance().checkMigrationReport(false);

        return true;
    }
}
