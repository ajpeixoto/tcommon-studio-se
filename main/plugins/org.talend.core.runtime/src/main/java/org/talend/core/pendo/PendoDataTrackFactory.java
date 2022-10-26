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
package org.talend.core.pendo;

import org.talend.core.pendo.properties.IPendoDataProperties;
import org.talend.core.service.IRemoteService;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoDataTrackFactory {

    private static PendoDataTrackFactory instance;

    private static IRemoteService remoteService;

    static {
        instance = new PendoDataTrackFactory();
        remoteService = IRemoteService.get();
    }

    private PendoDataTrackFactory() {
    }

    public static PendoDataTrackFactory getInstance() {
        return instance;
    }

    public boolean isTrackSendAvailable() throws Exception {
        if (remoteService != null) {
            return remoteService.isPendoTrackAvailable();
        }
        return false;
    }

    public void sendTrackData(TrackEvent event, IPendoDataProperties properties) throws Exception {
        if (remoteService != null) {
            remoteService.sendPendoTrackData(event, properties);
        }
    }

    public void sendProjectLoginTrack() {
        if (remoteService != null) {
            AbstractPendoTrackManager pendoProjectLoginManager = remoteService.getPendoProjectLoginManager();
            pendoProjectLoginManager.sendTrackToPendo();
        }
    }

    public void sendGenericTrack(TrackEvent event, IPendoDataProperties properties) {
        if (remoteService != null) {
            AbstractPendoTrackManager genericManager = remoteService.getPendoGenericManager(event, properties);
            genericManager.sendTrackToPendo();
        }
    }

    public String getTmcUser(String url, String token) {
        if (remoteService != null) {
            return remoteService.getTmcUser(url, token);
        }
        return "";
    }

}
