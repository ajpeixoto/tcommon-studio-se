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

public abstract class AbsReference implements IReference {

    private IReference parent;

    private List<IReference> references;

    private List<String> uses;

    public AbsReference() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public IReference getParent() {
        return parent;
    }

    public void setParent(IReference parent) {
        this.parent = parent;
    }

    @Override
    public List<IReference> getReferences() {
        return references;
    }

    public void setReferences(List<IReference> children) {
        this.references = children;
    }

    @Override
    public List<String> getUses() {
        return uses;
    }

    public void setUses(List<String> uses) {
        this.uses = uses;
    }


}
