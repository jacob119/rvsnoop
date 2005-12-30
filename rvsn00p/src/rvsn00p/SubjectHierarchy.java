//:File:    SubjectHierarchy.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;


/**
 * A hierarchy of rendezvous subjects.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class SubjectHierarchy extends DefaultTreeModel {

    private static SubjectHierarchy instance = new SubjectHierarchy();
    
    private static final long serialVersionUID = -3629858078509052804L;
    
    private static final Pattern SPLITTER = Pattern.compile("\\.");

    private SubjectElement noSubjectElement;
    
    /**
     * Get the singleton subject hierarchy instance.
     * 
     * @return
     */
    public static SubjectHierarchy getInstance() {
        return instance;
    }

    private SubjectHierarchy() {
        super(new SubjectElement());
    }

    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }
    
    public void addRecord(Record record) {
        final SubjectElement element = record.getSubject();
        element.incNumRecordsHere();
        if (MsgType.ERROR.equals(record.getType()))
            element.setErrorHere(true);
        final TreeNode[] nodes = element.getPath();
        for (int i = 0, imax = nodes.length; i < imax; ++i)
            nodeChanged(nodes[i]);
    }

    private void fireSelectionChanged() {
        final EventListener[] listeners = listenerList.getListeners(ActionListener.class);
        if (listeners == null || listeners.length == 0) return;
        final ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Node Selection Changed");
        for (int i = 0, imax = listeners.length; i < imax; ++i)
            ((ActionListener) listeners[i]).actionPerformed(event);
    }
    
    private synchronized SubjectElement getNoSubjectElement() {
        if (noSubjectElement == null)
            noSubjectElement = insertNewChild((SubjectElement) getRoot(), "[No Subject!]", 0);
        return noSubjectElement;
    }
    
    /**
     * Convert a subject string into a subject element object.
     * <p>
     * The element will be created if it does not already exists.
     * 
     * @param subject The subject as a string.
     * @return The <code>SubjectElement</code> representing the subject.
     */
    public SubjectElement getSubjectElement(String subject) {
        if (subject == null || subject.length() == 0)
            return getNoSubjectElement();
        SubjectElement current = (SubjectElement) getRoot();
        final String[] subjectElts = SPLITTER.split(subject);
        final String[] pathElts = new String[subjectElts.length + 1];
        pathElts[0] = (String) current.getUserObject();
        System.arraycopy(subjectElts, 0, pathElts, 1, subjectElts.length);
        // Skipping the root node...
        WALK_PATH: for (int i = 1, imax = pathElts.length; i < imax; ++i) {
            final String element = pathElts[i];
            final int numChildren = current.getChildCount();
            // If there are no children just add a child and continue.
            if (numChildren == 0) {
                current = insertNewChild(current, element, 0);
            } else {
                SCAN_CHILDREN: for (int j = 0; j < numChildren; ++j) {
                    final SubjectElement child = (SubjectElement) current.getChildAt(j);
                    final int compared = element.compareTo(child.getElementName());
                    // If this child already exists scan it.
                    if (compared == 0) {
                        current = child;
                        continue WALK_PATH;
                    } else if (compared < 0) {
                        // If we have found a child which should be sorted after this then
                        // we should insert the new child here and immeniately scan it.
                        current = insertNewChild(current, element, j);
                        continue WALK_PATH;
                    }
                }
                // If we have reached the end of the children with no match then append
                // a new child and scan it.
                current = insertNewChild(current, element, numChildren);
            }
        }
        return current;
    }

    private SubjectElement insertNewChild(final SubjectElement current, final String element, final int index) {
        final SubjectElement newChild = new SubjectElement(current, element);
        insertNodeInto(newChild, current, index);
        return newChild;
    }

    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * Resets all counters and error flags in the hierarchy.
     */
    public void reset() {
        final Enumeration nodes = ((SubjectElement) getRoot()).depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            final SubjectElement current = (SubjectElement) nodes.nextElement();
            current.reset();
            nodeChanged(current);
        }
    }

    /**
     * Set the selction on a given node and all descendants.
     * 
     * @param node The root of the subtree to set.
     * @param selected The new selection value.
     */
    public void setAllSelected(SubjectElement node, boolean selected) {
        final Enumeration descendants = node.depthFirstEnumeration();
        while (descendants.hasMoreElements())
            updateElement((SubjectElement) descendants.nextElement(), selected);
        fireSelectionChanged();
    }

    /**
     * Set the selection on a given node.
     * <p>
     * If <code>selection</code> is <code>false</code> then this also
     * unselects all descendants, if it is <code>true</code> then this also
     * selects the path to root.
     * 
     * @param node The node to select from.
     * @param selected the new selection value.
     */
    public void setSelected(SubjectElement node, boolean selected) {
        if (node.isSelected() == selected) return;
        // Select parents or deselect children?
        if (selected) {
            final TreeNode[] nodes = node.getPath();
            // Skip the 0-index root node.
            for (int i = nodes.length - 1; i != 0; --i)
                updateElement((SubjectElement) nodes[i], selected);
            fireSelectionChanged();
        } else {
            setAllSelected(node, false);
        }
    }

    private void updateElement(final SubjectElement current, boolean selected) {
        if (current.isSelected() != selected) {
            current.setSelected(selected);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    nodeChanged(current);
                }
            });
        }
    }

}
