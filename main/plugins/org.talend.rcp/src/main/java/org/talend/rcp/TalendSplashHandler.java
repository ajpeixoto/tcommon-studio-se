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
package org.talend.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;
import org.talend.repository.ui.image.ImageUtils;

/**
 * this class is justmade so that we can get the handler instance (no other way to get the instance). this instance is
 * used by the RCP WorkbenchAdvisor.preStartup to execute so login task and report some progress on the splash screen.
 * */
public class TalendSplashHandler extends EclipseSplashHandler {

    public static TalendSplashHandler instance;

    /**
     * DOC sgandon TalendSplashHandler constructor comment.
     */
    public TalendSplashHandler() {
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    public IProgressMonitor getBundleProgressMonitor() {
        IProgressMonitor bundleProgressMonitor = super.getBundleProgressMonitor();
        if (!ImageUtils.isSonoma()) {
            return bundleProgressMonitor;
        }
        if (bundleProgressMonitor != null && bundleProgressMonitor instanceof ProgressMonitorPart) {
            ProgressMonitorPart pmp = (ProgressMonitorPart) bundleProgressMonitor;
            Shell splash = getSplash();
            if (splash != null) {
                Shell shell = splash.getShell();
                if (shell != null) {
                    Image backgroundImage = shell.getBackgroundImage();
                    if (backgroundImage != null) {
                        backgroundImage = ImageUtils.flipImage(shell.getDisplay(), backgroundImage);
                        pmp.setBackgroundImage(backgroundImage);
                        ImageUtils.addResourceDisposeListener(pmp, backgroundImage);
                        return pmp;
                    }
                }
            }
        }
        return bundleProgressMonitor;
    }
    
}
