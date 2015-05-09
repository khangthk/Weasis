/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package br.com.animati.texture.mpr3dview.tool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerEvent.EVENT;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.AnnotationsLayer;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.graphic.model.AbstractLayer;
import org.weasis.core.ui.graphic.model.AbstractLayer.Identifier;
import org.weasis.dicom.codec.DicomImageElement;

import bibliothek.gui.dock.common.CLocation;
import br.com.animati.texture.mpr3dview.GUIManager;
import br.com.animati.texture.mpr3dview.internal.Messages;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

public class DisplayTool extends PluginTool implements SeriesViewerListener {

    public static final String DICOM_ANNOTATIONS = Messages.getString("DisplayTool.dicom_ano"); //$NON-NLS-1$

    public static final String BUTTON_NAME = Messages.getString("DisplayTool.display"); //$NON-NLS-1$

    private final JCheckBox applyAllViews = new JCheckBox(Messages.getString("DisplayTool.btn_apply_all"), true); //$NON-NLS-1$
    private final CheckboxTree tree;
    private boolean initPathSelection;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("rootNode", true); //$NON-NLS-1$

    private DefaultMutableTreeNode dicomInfo;
    private DefaultMutableTreeNode drawings;
    private TreePath rootPath;
    private JPanel panel_foot;

    public DisplayTool(String pluginName) {
        super(BUTTON_NAME, pluginName, PluginTool.Type.TOOL, 10);
        dockable.setTitleIcon(new ImageIcon(DisplayTool.class.getResource("/icon/16x16/display.png"))); //$NON-NLS-1$
        setDockableWidth(210);

        tree = new CheckboxTree();
        initPathSelection = false;
        setLayout(new BorderLayout(0, 0));
        iniTree();

    }

    public void iniTree() {
        tree.getCheckingModel().setCheckingMode(CheckingMode.SIMPLE);

        dicomInfo = new DefaultMutableTreeNode(DICOM_ANNOTATIONS, true);
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.ANNOTATIONS, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.ANONYM_ANNOTATIONS, false));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.SCALE, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.IMAGE_ORIENTATION, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.WINDOW_LEVEL, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.ZOOM, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.ROTATION, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.FRAME, true));
        dicomInfo.add(new DefaultMutableTreeNode(AnnotationsLayer.PIXEL, true));
        rootNode.add(dicomInfo);
        drawings = new DefaultMutableTreeNode(ActionW.DRAW, true);
        drawings.add(new DefaultMutableTreeNode(AbstractLayer.MEASURE, true));
        rootNode.add(drawings);

        DefaultTreeModel model = new DefaultTreeModel(rootNode, false);
        tree.setModel(model);
        rootPath = new TreePath(rootNode.getPath());
        tree.addCheckingPath(rootPath);

        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setExpandsSelectedPaths(true);
        DefaultCheckboxTreeCellRenderer renderer = new DefaultCheckboxTreeCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);
        tree.setCellRenderer(renderer);
        tree.addTreeCheckingListener(new TreeCheckingListener() {

            @Override
            public void valueChanged(TreeCheckingEvent e) {
                if (!initPathSelection) {
                    TreePath path = e.getPath();
                    Object source = e.getSource();
                    boolean selected = e.isCheckedPath();
                    Object selObject = path.getLastPathComponent();
                    Object parent = null;
                    if (path.getParentPath() != null) {
                        parent = path.getParentPath().getLastPathComponent();
                    }

                    ImageViewerPlugin<DicomImageElement> container =
                        GUIManager.getInstance().getSelectedView2dContainer();
                    ArrayList<ViewCanvas<DicomImageElement>> views = null;
                    if (container != null) {
                        if (applyAllViews.isSelected()) {
                            views = container.getImagePanels();
                        } else {
                            views = new ArrayList<ViewCanvas<DicomImageElement>>(1);
                            ViewCanvas<DicomImageElement> view = container.getSelectedImagePane();
                            if (view != null) {
                                views.add(view);
                            }
                        }
                    }
                    if (views != null) {
                        if (rootNode.equals(parent)) {
                            if (dicomInfo.equals(selObject)) {
                                for (ViewCanvas<DicomImageElement> v : views) {
                                    if (selected != v.getInfoLayer().isVisible()) {
                                        v.getInfoLayer().setVisible(selected);
                                        v.getJComponent().repaint();
                                    }
                                }
                            } else if (drawings.equals(selObject)) {
                                for (ViewCanvas<DicomImageElement> v : views) {
                                    v.setDrawingsVisibility(selected);
                                }
                            }
                        } else if (dicomInfo.equals(parent)) {
                            if (selObject != null) {
                                for (ViewCanvas<DicomImageElement> v : views) {
                                    AnnotationsLayer layer = v.getInfoLayer();
                                    if (layer != null) {
                                        if (layer.setDisplayPreferencesValue(selObject.toString(), selected)) {
                                            v.getJComponent().repaint();
                                        }
                                    }
                                }
                                if (AnnotationsLayer.ANONYM_ANNOTATIONS.equals(selObject.toString())) {
                                    // Send message to listeners, only selected view
                                    ViewCanvas<DicomImageElement> v = container.getSelectedImagePane();
                                    Series series = (Series) v.getSeries();
                                    GUIManager.getInstance().fireSeriesViewerListeners(
                                        new SeriesViewerEvent(container, series, v.getImage(), EVENT.ANONYM));

                                }
                            }
                        } else if (drawings.equals(parent)) {
                            if (selObject instanceof DefaultMutableTreeNode) {
                                if (((DefaultMutableTreeNode) selObject).getUserObject() instanceof Identifier) {
                                    Identifier layerID =
                                        (Identifier) ((DefaultMutableTreeNode) selObject).getUserObject();
                                    for (ViewCanvas<DicomImageElement> v : views) {
                                        AbstractLayer layer = v.getLayerModel().getLayer(layerID);
                                        if (layer != null) {
                                            if (layer.isVisible() != selected) {
                                                layer.setVisible(selected);
                                                v.getJComponent().repaint();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        add(panel, BorderLayout.NORTH);
        panel.add(applyAllViews);

        expandTree(tree, rootNode);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        panel_foot = new JPanel();
        // To handle selection color with all L&Fs
        panel_foot.setUI(new javax.swing.plaf.PanelUI() {
        });
        panel_foot.setOpaque(true);
        panel_foot.setBackground(JMVUtils.TREE_BACKROUND);
        add(panel_foot, BorderLayout.SOUTH);
    }

    private void initPathSelection(TreePath path, boolean selected) {
        if (selected) {
            tree.addCheckingPath(path);
        } else {
            tree.removeCheckingPath(path);
        }
    }

    public void iniTreeValues(ViewCanvas view) {
        if (view != null) {
            initPathSelection = true;

            // Annotations node
            AnnotationsLayer layer = view.getInfoLayer();
            if (layer != null) {
                initPathSelection(getTreePath(dicomInfo), layer.isVisible());
                Enumeration en = dicomInfo.children();
                while (en.hasMoreElements()) {
                    Object node = en.nextElement();
                    if (node instanceof TreeNode) {
                        TreeNode checkNode = (TreeNode) node;
                        initPathSelection(getTreePath(checkNode), layer.getDisplayPreferences(node.toString()));
                    }
                }
            }

            // Drawings node
            Boolean draw = (Boolean) view.getActionValue(ActionW.DRAW.cmd());
            initPathSelection(getTreePath(drawings), draw == null ? true : draw);
            Enumeration en = drawings.children();
            while (en.hasMoreElements()) {
                Object node = en.nextElement();
                if (node instanceof DefaultMutableTreeNode
                    && ((DefaultMutableTreeNode) node).getUserObject() instanceof Identifier) {
                    DefaultMutableTreeNode checkNode = (DefaultMutableTreeNode) node;
                    AbstractLayer l = view.getLayerModel().getLayer((Identifier) checkNode.getUserObject());
                    if (l == null) {
                        // Remove from display if the layer does not exist any more
                        TreeNode parent = checkNode.getParent();
                        int index = parent.getIndex(checkNode);
                        checkNode.removeFromParent();
                        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                        dtm.nodesWereRemoved(parent, new int[] { index }, new TreeNode[] { checkNode });
                    } else {
                        initPathSelection(getTreePath(checkNode), l.isVisible());
                    }
                }
            }

            initPathSelection = false;
        }
    }

    private static TreePath getTreePath(TreeNode node) {
        List<TreeNode> list = new ArrayList<TreeNode>();
        list.add(node);
        TreeNode parent = node;
        while (parent.getParent() != null) {
            parent = parent.getParent();
            list.add(parent);
        }
        Collections.reverse(list);
        return new TreePath(list.toArray(new TreeNode[list.size()]));
    }

    @Override
    public Component getToolComponent() {
        return this;
    }

    public void expandAllTree() {
        tree.expandRow(4);
    }

    @Override
    protected void changeToolWindowAnchor(CLocation clocation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void changingViewContentEvent(SeriesViewerEvent event) {
        EVENT e = event.getEventType();
        if (EVENT.SELECT_VIEW.equals(e) && event.getSeriesViewer() instanceof ImageViewerPlugin) {
            iniTreeValues(((ImageViewerPlugin<?>) event.getSeriesViewer()).getSelectedImagePane());
        } else if (EVENT.TOOGLE_INFO.equals(e)) {
            TreeCheckingModel model = tree.getCheckingModel();
            model.toggleCheckingPath(new TreePath(dicomInfo.getPath()));
        } else if (EVENT.ADD_LAYER.equals(e)) {
            Object obj = event.getSharedObject();
            if (obj instanceof Identifier) {
                DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(obj, true);
                drawings.add(node);
                dtm.nodesWereInserted(drawings, new int[] { drawings.getIndex(node) });
                if (event.getSeriesViewer() instanceof ImageViewerPlugin
                    && node.getUserObject() instanceof Identifier) {
                    ViewCanvas<?> pane = ((ImageViewerPlugin<?>) event.getSeriesViewer()).getSelectedImagePane();
                    if (pane != null) {
                        AbstractLayer l = pane.getLayerModel().getLayer((Identifier) node.getUserObject());
                        if (l != null && l.isVisible()) {
                            tree.addCheckingPath(getTreePath(node));
                        }
                    }
                }
            }
        } else if (EVENT.REMOVE_LAYER.equals(e)) {
            Object obj = event.getSharedObject();
            if (obj instanceof Identifier) {
                Identifier id = (Identifier) obj;
                Enumeration en = drawings.children();
                while (en.hasMoreElements()) {
                    Object node = en.nextElement();
                    if (node instanceof DefaultMutableTreeNode
                        && id.equals(((DefaultMutableTreeNode) node).getUserObject())) {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) node;
                        TreeNode parent = n.getParent();
                        int index = parent.getIndex(n);
                        n.removeFromParent();
                        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                        dtm.nodesWereRemoved(parent, new int[] { index }, new TreeNode[] { n });
                    }
                }
            }
        }
    }

    private static void expandTree(JTree tree, DefaultMutableTreeNode start) {
        for (Enumeration children = start.children(); children.hasMoreElements();) {
            DefaultMutableTreeNode dtm = (DefaultMutableTreeNode) children.nextElement();
            if (!dtm.isLeaf()) {
                //
                TreePath tp = new TreePath(dtm.getPath());
                tree.expandPath(tp);
                //
                expandTree(tree, dtm);
            }
        }
        return;
    }

}
