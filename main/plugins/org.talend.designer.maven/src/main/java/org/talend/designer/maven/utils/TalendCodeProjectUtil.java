// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.maven.utils;

import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.talend.designer.maven.model.TalendJavaProjectConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.creator.CreateMavenCodeProject;
import org.talend.repository.ProjectManager;

/**
 * created by ggu on 23 Jan 2015 Detailled comment
 *
 */
public final class TalendCodeProjectUtil {
	
	private static Logger log = Logger.getLogger(TalendCodeProjectUtil.class);

    /**
     * a temp maven java project, actually only used for compilation by jdt, any settings in pom.xml won't take affect.
     */
    public static IProject initCodeProject(IProgressMonitor monitor) throws Exception {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IProject codeProject = root.getProject(TalendMavenConstants.PROJECT_NAME);

        if (!codeProject.exists() || needRecreate(monitor, codeProject)) {
            // if existed, must delete it first, else when do CreateMavenCodeProject will cause problem. for .metadata
            if (codeProject.exists()) {
                if (codeProject.isOpen()) {
                    codeProject.close(monitor);
                }
                codeProject.delete(true, true, monitor);
            }
            CreateMavenCodeProject createProject = new CreateMavenCodeProject(codeProject, null, true) {

                @Override
                protected Model createModel() {
                    Model templateModel = new Model();
                    templateModel.setModelVersion("4.0.0"); //$NON-NLS-1$
                    templateModel.setGroupId("org.talend.temp.project"); //$NON-NLS-1$
                    templateModel.setArtifactId(TalendMavenConstants.PROJECT_NAME);
                    templateModel.setVersion(PomIdsHelper.getProjectVersion());
                    templateModel.setPackaging(TalendMavenConstants.PACKAGING_JAR);
                    //add parent for .java project to make it re-use the pom_project_template.xml defined maven plugin versions
                    Parent parent = new Parent();
                    parent.setArtifactId(PomIdsHelper.getProjectArtifactId());
                    String projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
                    parent.setGroupId(PomIdsHelper.getProjectGroupId(projectTechName));
                    parent.setVersion(PomIdsHelper.getProjectVersion(projectTechName));
                    parent.setRelativePath("../"+ projectTechName+"/"+TalendJavaProjectConstants.DIR_POMS);
                    templateModel.setParent(parent);

                    return templateModel;
                }

                @Override
                protected void afterCreate(IProgressMonitor monitor, IResource res) throws Exception {
                    IProject p = res.getProject();
                    if (!p.isOpen()) {
                        p.open(monitor);
                    }
                    addTalendNature(p, monitor);
                }
                
                @Override
                protected void createPomIfNotExist(IFile pomFile) throws CoreException{
                	//for .Java project delete the pom.xml file the first time when run preview to make sure the pom will be updated
                	if(pomFile != null && pomFile.exists()) {
                		try {
                			pomFile.delete(true, monitor);
                		}catch(Exception e) {
                			//continue if got any exception
                			log.error("delete pom.xml failed : "+ e.getMessage());
                		}
                	}
                	//in case delete failed
                	super.createPomIfNotExist(pomFile);
                }

            };
            createProject.setProjectLocation(root.getLocation().append(TalendMavenConstants.PROJECT_NAME));
            createProject.create(monitor);
            codeProject = createProject.getProject();
        }

        if (!codeProject.isOpen()) {
            codeProject.open(IProject.BACKGROUND_REFRESH, monitor);
        } else {
            if (!codeProject.isSynchronized(IProject.DEPTH_INFINITE)) {
                codeProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            }
        }
        return codeProject;
    }

    @SuppressWarnings("restriction")
    public static boolean needRecreate(IProgressMonitor monitor, IProject codeProject) throws CoreException {
        if (codeProject.exists()) { // exist the project for workspace metadata.

            // If the project is not existed physically (in disk). sometime, because delete it manually. Then finally,
            // will cause problem.
            if (!codeProject.getLocation().toFile().exists()) {
                return true;
            }

            try {
                // because some cases, the project is not opened.
                if (!codeProject.isOpen()) {
                    // if not opened, will have exception when check nature or such
                    codeProject.open(monitor);
                }
            } catch (Exception e) {
                return true;
            }

            codeProject.refreshLocal(IResource.DEPTH_ONE, monitor);

            // not java project
            if (!codeProject.hasNature(JavaCore.NATURE_ID)) {
                return true;
            }

            // like TDI-33044, when creating, kill the studio. the classpath file won't be created.
            if (!codeProject.getFile(IJavaProject.CLASSPATH_FILE_NAME).exists()) {
                return true;
            }

            // IJavaProject javaProject = JavaCore.create(codeProject);
            // javaProject.getRawClasspath(); //test the nature and classpath?

            // no maven nature.
            if (!codeProject.hasNature(IMavenConstants.NATURE_ID)) {
                return true;
            }

            // no pom
            if (!codeProject.getFile(TalendMavenConstants.POM_FILE_NAME).exists()) {
                return true;
            }
        }
        return false;
    }

    // public static IMarker[] getMavenMarks(IFile file) throws CoreException {
    // IMarker[] findMarkers = file.findMarkers(IMavenConstants.MARKER_CONFIGURATION_ID, true, IResource.DEPTH_ONE);
    // return findMarkers;
    // }

}
