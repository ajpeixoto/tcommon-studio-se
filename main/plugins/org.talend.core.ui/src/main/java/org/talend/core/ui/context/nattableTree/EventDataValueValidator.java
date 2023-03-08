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
package org.talend.core.ui.context.nattableTree;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.talend.core.model.process.IContextManager;
import org.talend.core.ui.context.ContextTreeTable.ContextTreeNode;
import org.talend.core.ui.i18n.Messages;
import org.talend.core.ui.utils.ContextTypeValidator;

public class EventDataValueValidator extends DataValidator {

    private IDataProvider dataProvider;

    private IContextManager manager;

    EventDataValueValidator(IDataProvider bodyDataProvider, IContextManager manager) {
        this.dataProvider = bodyDataProvider;
        this.manager = manager;
    }

    @Override
    public boolean validate(int columnIndex, int rowIndex, Object newValue) {
        ContextTreeNode rowNode = ((GlazedListsDataProvider<ContextTreeNode>) dataProvider).getList().get(rowIndex);
        String dataType = ContextNatTableUtils.getCurrentContextDataType(rowNode.getTreeData(), rowIndex);
        boolean isValid = ContextTypeValidator.isMatchType(dataType, newValue);
        if (!isValid) {
            throw new ValidationFailedException(Messages.getString("ContextValidator.ParameterValueNotMatch")); //$NON-NLS-1$
        }
        return isValid;
    }
}
