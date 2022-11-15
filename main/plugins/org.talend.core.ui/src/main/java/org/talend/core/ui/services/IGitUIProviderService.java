package org.talend.core.ui.services;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.properties.tab.IDynamicProperty;

public interface IGitUIProviderService extends IService {

    boolean isGitHistoryComposite(IDynamicProperty dp);

    ISelection getGitHistorySelection(IDynamicProperty dp);

    IDynamicProperty createProcessGitHistoryComposite(Composite parent, Object view, TabbedPropertySheetWidgetFactory factory,
            IRepositoryViewObject obj);

    public String[] changeCredentials(Shell parent, Serializable uriIsh, String initUser, boolean canStoreCredentials);

    boolean checkPendingChanges();

    public void openPushFailedDialog(Object pushResult);

    boolean migrateOption(IProgressMonitor monitor, String newVersion, boolean hasUpdate) throws Exception;
    
    boolean openSwitchGitModeDialog();
    
    boolean canSwitchGitMode();
    
    boolean canShowSwitchGitModePopup();
    
    void showSwithGitModePopup();
    
    Map<Project, List<Project>> getInvalidReferencedProjects();

    public static IGitUIProviderService get() {
        GlobalServiceRegister register = GlobalServiceRegister.getDefault();
        if (!register.isServiceRegistered(IGitUIProviderService.class)) {
            return null;
        }
        return register.getService(IGitUIProviderService.class);
    }

}
