// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.pendo.mapper;

import org.apache.log4j.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.pendo.PendoTrackDataUtil.TrackEvent;
import org.talend.core.pendo.PendoTrackSender;
import org.talend.core.pendo.properties.PendoTMapProperties;

/**
 * DOC jding  class global comment. Detailled comment
 */
public abstract class AbstractPendoTMapManager {

    protected abstract PendoTMapProperties calculateProperties();

    public void sendTrackToPendo() {
        Job job = new Job("send pendo track") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (PendoTrackSender.getInstance().isTrackSendAvailable()) {
                        PendoTMapProperties properties = calculateProperties();
                        PendoTrackSender.getInstance().sendTrackData(TrackEvent.TMAP, properties);
                    }
                } catch (Exception e) {
                    // warning only
                    ExceptionHandler.process(e, Level.WARN);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

}
