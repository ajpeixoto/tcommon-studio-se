// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.metadata.ui.view;

import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Tree;
import org.talend.core.model.repository.IRepositoryViewObject;

public class ReferencesPanel extends Composite {

    private IObservableList<IReference> references;

    private Link title;

    private IRepositoryViewObject repoObj;

    private SashForm sash;

    private TreeViewer referenceTree;

    private TableViewer referenceList;

    public ReferencesPanel(Composite parent, int style) {
        super(parent, style);
        references = new WritableList<>();
        createControl(this);
    }

    public IObservableList<IReference> getReferences() {
        return references;
    }

    public void resetReferences() {
        references = new WritableList<>();
        referenceTree.setInput(references);
    }

    public void setRepositoryViewObject(IRepositoryViewObject repoObj) {
        this.repoObj = repoObj;
        if (this.repoObj == null) {
            this.title.setText("");
        } else {
            this.title.setText("References of <a>" + repoObj.getLabel() + "</a>");
        }
    }

    private void createControl(Composite panel) {
        panel.setLayout(new FormLayout());

        int margin = 10;
        title = new Link(panel, SWT.NONE);
        FormData fd = new FormData();
        fd.left = new FormAttachment(0, margin);
        fd.right = new FormAttachment(100, -margin);
        fd.top = new FormAttachment(0, margin);
        title.setLayoutData(fd);

        sash = new SashForm(panel, SWT.NONE);
        fd = new FormData();
        fd.left = new FormAttachment(title, 0, SWT.LEFT);
        fd.right = new FormAttachment(title, 0, SWT.RIGHT);
        fd.top = new FormAttachment(title, 10, SWT.BOTTOM);
        fd.bottom = new FormAttachment(100);
        sash.setLayoutData(fd);

        referenceTree = new TreeViewer(sash, SWT.BORDER);
        referenceList = new TableViewer(sash, SWT.BORDER);
        sash.setWeights(new int[] { 5, 2 });
        Tree tree = referenceTree.getTree();
        tree.setHeaderVisible(false);

        final IObservableFactory setFactory = new IObservableFactory() {

            @Override
            public IObservable createObservable(final Object target) {
                if (target instanceof WritableSet) {
                    return (IObservableSet) target;
                }
                return null;
            }
        };

        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider<IReference>(
                new ReferencesFactory(), new ReferencesTreeStructureAdvisor());

        referenceTree.setContentProvider(contentProvider);
        referenceTree.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IReference) {
                    List<String> uses = ((IReference) element).getUses();
                    String suffix = "";
                    if (uses != null) {
                        suffix = " (" + uses.size() + " uses)";
                    }
                    return ((IReference) element).getName() + suffix;
                }
                return super.getText(element);
            }
        });
        referenceTree.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                onReferenceTreeSelectionChange(event);
            }
        });

        referenceTree.setInput(references);

        referenceList.setContentProvider(ArrayContentProvider.getInstance());
        referenceList.setLabelProvider(new LabelProvider());
    }

    private void onReferenceTreeSelectionChange(SelectionChangedEvent event) {
        StructuredSelection selection = (StructuredSelection) event.getSelection();
        IReference ref = (IReference) selection.getFirstElement();
        List<String> uses = ref.getUses();
        if (uses != null) {
            referenceList.setInput(uses);
        }
    }

    private class ReferencesFactory implements IObservableFactory<Object, IObservableList<IReference>> {

        @Override
        public IObservableList<IReference> createObservable(Object input) {
            if (input instanceof IReference) {
                List<IReference> refs = ((IReference) input).getReferences();
                if (refs == null) {
                    return null;
                }
                return new WritableList<>(((IReference) input).getReferences(), IReference.class);
            } else if (input instanceof IObservableList) {
                return (IObservableList<IReference>) input;
            }
            return null;
        }
    }

    public class ReferencesTreeStructureAdvisor extends TreeStructureAdvisor {

        @Override
        public Object getParent(Object element) {
            if (element instanceof IReference) {
                return ((IReference) element).getParent();
            }
            return super.getParent(element);
        }
    }
}
