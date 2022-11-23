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
package org.talend.core.runtime.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.osgi.framework.Bundle;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.ProjectManager;

public class ModuleAccessHelper {

    private static final String FORMAT_ADD_OPPENS = "--add-opens=@=ALL-UNNAMED"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(ModuleAccessHelper.class);

    private static final Properties PROPS = new Properties();

    static {
        Bundle bundle = Platform.getBundle(CoreRuntimePlugin.PLUGIN_ID);
        URL templateUrl = bundle.getEntry("resources/module_access.properties"); //$NON-NLS-1$
        try (InputStream input = templateUrl.openStream()) {
            PROPS.load(input);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    private static boolean containsKey(String key) {
        return PROPS.containsKey(key);
    }

    private static Set<String> getModules(String key) {
        String modules = PROPS.getProperty(key);
        if (modules != null) {
            return Stream.of(modules.split(",")).map(module -> FORMAT_ADD_OPPENS.replace("@", module)) //$NON-NLS-1$ //$NON-NLS-2$
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private static boolean allowJavaInternalAcess(Property property) {
        if (CommonsPlugin.isTUJTest()) {
            return true;
        }
        String allow = System.getProperty(JavaUtils.ALLOW_JAVA_INTERNAL_ACCESS);
        if (allow != null) {
            return Boolean.valueOf(allow);
        }
        Project project;
        if (property != null) {
            project = ProjectManager.getInstance()
                    .getProjectFromProjectTechLabel(ProjectManager.getInstance().getProject(property).getTechnicalLabel());
        } else {
            project = ProjectManager.getInstance().getCurrentProject();
        }
        ProjectPreferenceManager preferenceManager = new ProjectPreferenceManager(project, CoreRuntimePlugin.PLUGIN_ID, false);
        return preferenceManager.getBoolean(JavaUtils.ALLOW_JAVA_INTERNAL_ACCESS);
    }

    public static Set<String> getModuleAccessVMArgsForProcessor(IProcessor processor) {
        Property property = processor.getProperty();
        if (property == null) {
            return Collections.emptySet();
        }
        if ("Mock_job_for_Guess_schema".equals(property.getLabel())) {
            // add all for preview process
            return PROPS.entrySet().stream().filter(en -> StringUtils.isNotBlank((String) en.getValue()))
                    .flatMap(en -> getModules((String) en.getKey()).stream()).collect(Collectors.toSet());

        }
        if (!allowJavaInternalAcess(property)) {
            return Collections.emptySet();
        }
        ProcessItem mainJobItem = (ProcessItem) property.getItem();
        Set<JobInfo> allJobInfos = new HashSet<>();
        allJobInfos.add(new JobInfo(mainJobItem, mainJobItem.getProcess().getDefaultContext()));
        allJobInfos.addAll(processor.getBuildChildrenJobsAndJoblets());
        return ModuleAccessHelper.getModuleAccessVMArgs(property, allJobInfos);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getModuleAccessVMArgs(Property property, Set<JobInfo> allJobInfos) {
        if (property == null || property.getItem() == null || !allowJavaInternalAcess(property)) {
            return Collections.emptySet();
        }
        Set<String> vmArgs = new HashSet<>();
        boolean hasTck = false;
        for (JobInfo info : allJobInfos) {
            EList<NodeType> nodes = null;
            EList<ElementParameterType> parameters = null;
            if (info.getJobletProperty() != null) {
                JobletProcessItem item = (JobletProcessItem) info.getJobletProperty().getItem();
                if (item.getJobletProcess() != null) {
                    nodes = item.getJobletProcess().getNode();
                    if (item.getJobletProcess().getParameters() != null) {
                        parameters = item.getJobletProcess().getParameters().getElementParameter();
                    }
                }
            } else if (info.getProcessItem() != null && info.getProcessItem().getProcess() != null) {
                nodes = info.getProcessItem().getProcess().getNode();
                if (info.getProcessItem().getProcess().getParameters() != null) {
                    parameters = info.getProcessItem().getProcess().getParameters().getElementParameter();
                }
            }
            if (nodes != null) {
                nodes.stream().filter(node -> containsKey(node.getComponentName()))
                        .forEach(node -> vmArgs.addAll(getModules(node.getComponentName())));
            }
            if (parameters != null) {
                // FIXME currently it depends on spark version, refine the condition if needed
                Optional<ElementParameterType> optional = parameters.stream()
                        .filter(p -> "SUPPORTED_SPARK_VERSION".equals(p.getName()) && containsKey(p.getValue())).findFirst();
                if (optional.isPresent()) {
                    vmArgs.addAll(getModules(optional.get().getValue()));
                }
            }
            if (!hasTck) {
                hasTck = nodes.stream()
                        .anyMatch(node -> node.getElementParameter().stream()
                                .anyMatch(p -> ((ElementParameterType) p).getField() != null
                                        && EParameterFieldType.TECHNICAL.getName().equals(((ElementParameterType) p).getField())
                                        && ((ElementParameterType) p).getName().equals("TACOKIT_COMPONENT_ID")));
            }
        }
        if (hasTck) {
            vmArgs.addAll(getModules("TCK_COMMON_ARGS"));
        }
        return vmArgs;
    }

}
