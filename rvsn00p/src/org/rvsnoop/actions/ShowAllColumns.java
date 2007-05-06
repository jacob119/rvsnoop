/*
 * Class:     ShowAllColumns
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.RecordLedgerFormat;

import rvsnoop.ui.UIManager;

/**
 * Show all columns in the message ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class ShowAllColumns extends RvSnoopAction {

    static { NLSUtils.internationalize(ShowAllColumns.class); }

    private static final long serialVersionUID = 6978433037411082384L;

    public static final String COMMAND = "showAllColumns";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;

    public ShowAllColumns(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        UIManager.INSTANCE.getRecordLedger().getTableFormat().setColumns(RecordLedgerFormat.ALL_COLUMNS);
    }

}