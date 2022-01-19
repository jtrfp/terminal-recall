/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.conf;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jtrfp.trcl.conf.ui.BasicSettingsPanel;
import org.jtrfp.trcl.conf.ui.BeanEditor;
import org.jtrfp.trcl.conf.ui.FeatureConfigurationUI;
import org.jtrfp.trcl.conf.ui.NoApplicablePropertiesException;
import org.jtrfp.trcl.conf.ui.ObjectEditorUI;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.FeaturesImpl.FeatureNotFoundException;
import org.jtrfp.trcl.core.FeaturesImpl.FeatureTargetMismatchException;
import org.jtrfp.trcl.gui.TRBeanUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

public class FeatureConfigFrame {

    @Getter(lazy = true)
    private final JFrame rootFrame = initialize();
    @Getter
    private Object featureRoot;
    @Getter
    private long timeOfLastRefreshRequestMillis;
    @Getter(lazy=true)
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    private JPanel featureEditPanel;
    private CardLayout cardLayout;
    private boolean populated = false;
    @Getter @Setter
    private JFrame rootWindow;
    private final Collection<ObjectEditorUI<?>> userInterfaces = new ArrayList<>(128);
    @Getter(lazy=true)
    private final FeaturesSettingsInstructionsPanel instructionsPanel = new FeaturesSettingsInstructionsPanel();
    
    @Getter(lazy=true)
    private final JTree tree = new JTree(getRootNode()) {
	protected void setExpandedState(TreePath path, boolean state) {
	    if( state )
		super.setExpandedState(path, state);
	}//end setExpandedState(...)
    };
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    FeatureConfigFrame window = new FeatureConfigFrame();
			SwingUtilities.invokeLater(()->{window.getRootFrame().setVisible(true);});
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }//end main()

    /**
     * Create the application.
     */
    public FeatureConfigFrame() {
	SwingUtilities.invokeLater(()->{initialize();});
    }

    /**
     * Initialize the contents of the frame.
     */
    private JFrame initialize() {
	featureEditPanel = new JPanel();
	cardLayout = new CardLayout(0, 0);
	final JFrame rootFrame = new JFrame();
	rootFrame.setTitle("Settings Editor");
	rootFrame.setBounds(100, 100, 900, 650);
	rootFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	
	final JTree tree = getTree();
	tree.setRootVisible(false);
	tree.setBackground(Color.black);
	
	JMenuBar menuBar = new JMenuBar();
	rootFrame.setJMenuBar(menuBar);
	
	JPanel panel = new JPanel();
	rootFrame.getContentPane().add(panel, BorderLayout.CENTER);
	panel.setLayout(new BorderLayout(0, 0));
	
	JSplitPane splitPane = new JSplitPane();
	panel.add(splitPane);
	
	JScrollPane treePane = new JScrollPane();
	splitPane.setLeftComponent(treePane);
	treePane.setMinimumSize(new Dimension(350,600));
	
	treePane.setViewportView(tree);
	
	JScrollPane featureEditPane = new JScrollPane();
	featureEditPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	splitPane.setRightComponent(featureEditPane);
	
	featureEditPane.setViewportView(featureEditPanel);
	featureEditPanel.setPreferredSize(new Dimension(500,600));
	featureEditPanel.setLayout(cardLayout);
	featureEditPanel.add(getInstructionsPanel(),"instructions");
	
	JPanel bottomPanel = new JPanel();
	rootFrame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	GridBagLayout gbl_bottomPanel = new GridBagLayout();
	gbl_bottomPanel.columnWidths = new int[]{213, 0, 0};
	gbl_bottomPanel.rowHeights = new int[]{25, 0};
	gbl_bottomPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
	gbl_bottomPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
	bottomPanel.setLayout(gbl_bottomPanel);
	
	JButton btnOk = new JButton("Apply and Close");
	GridBagConstraints gbc_btnOk = new GridBagConstraints();
	gbc_btnOk.anchor = GridBagConstraints.SOUTHWEST;
	gbc_btnOk.insets = new Insets(0, 0, 0, 5);
	gbc_btnOk.gridx = 0;
	gbc_btnOk.gridy = 0;
	bottomPanel.add(btnOk, gbc_btnOk);
	btnOk.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		applyChanges();
		rootFrame.setVisible(false);
	    }
	});
	
	JButton btnCancel = new JButton("Cancel");
	GridBagConstraints gbc_btnCancel = new GridBagConstraints();
	gbc_btnCancel.anchor = GridBagConstraints.SOUTHEAST;
	gbc_btnCancel.gridx = 1;
	gbc_btnCancel.gridy = 0;
	bottomPanel.add(btnCancel, gbc_btnCancel);
	btnCancel.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		rootFrame.setVisible(false);
	    }
	    
	});
	
	tree.setCellRenderer(new FeatureTreeCellRenderer());
	tree.addTreeSelectionListener(new TreeSelectionListener() {
	    @Override
	    public void valueChanged(TreeSelectionEvent e) {
		final TreePath path = e.getNewLeadSelectionPath();
		if( path == null )
		    return;//Nothing to do.
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
		if( node == null )
		    return;//Nothing to do.
		FeatureEntry selected = (FeatureEntry)(node.getUserObject());
		ObjectEditorUI<?> ui = selected.getUserInterface();
		if( ui != null )
		    ui.proposeRevertSettings(getTimeOfLastRefreshRequestMillis());
		cardLayout.show(featureEditPanel, selected.toString());
	    }});
	
	return rootFrame;
    }//end initialize()
    
    @Getter @EqualsAndHashCode
    private class FeatureEntry {
	private final Feature<?> feature;
	private Component editor;
	private boolean customEdited = false;
	private final ObjectEditorUI<?> userInterface;
	private final BasicSettingsPanel<?> basicSettingsPanel;
	private BeanEditor beanEditor;
	
	public FeatureEntry(Feature<?> feature) {
	    this.feature = feature;
	    this.basicSettingsPanel = new BasicSettingsPanel<>(feature);
	    this.userInterface = generateCustomInterface();
	}
	
	@Override
	public String toString() {
	    return feature.getClass().getSimpleName();
	}

	@SuppressWarnings("unchecked")
	private ObjectEditorUI<?> generateCustomInterface() {
	    try {
		final ObjectEditorUI<?> ui = Features.get(feature, FeatureConfigurationUI.class);
		return ui;
	    } catch( FeatureNotFoundException | FeatureTargetMismatchException e) {
		 try {//Try to use a BeanEditor
		    final BeanEditor beanEditor = new BeanEditor();
		    beanEditor.configure(null, ()->feature, (Set<Annotation>)Collections.EMPTY_SET, null);
		    this.beanEditor = beanEditor;
		    return beanEditor;
		    } catch(NoApplicablePropertiesException ee) {}
		}//end use BeanEditor
	    return null;
	}//end getUserInterface

	public Component getEditor() {
	    if( editor == null ) {
		final JPanel panel;
		editor = panel = new JPanel();
		final BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxLayout);
		//Basic settings
		final BasicSettingsPanel<?> bsp = getBasicSettingsPanel();
		bsp.setAlignmentX(0f);
		panel.add(bsp);
		final ObjectEditorUI<?> ui = getUserInterface();
		if( ui != null ) {
		    final JComponent customEditor = ui.getUIComponent();
		    customEditor.setAlignmentX(0f);
		    panel.add(customEditor);
		    customEdited = true;
		} else {//Try to use a BeanEditor
		    if(beanEditor != null)
			panel.add(beanEditor.getUIComponent());
		}
	    }//end if(null)
	    return editor;
	}//end getEditor()
    }//end FeatureEntry

    private void populateTree() {
	populateTreeRecursive(getRootNode(), featureRoot);

	SwingUtilities.invokeLater(()->{
	    final JTree tree = getTree();
	    ((DefaultTreeModel)tree.getModel()).reload();
	    for( int i = 0 ; i < tree.getRowCount(); i++ )
		tree.expandRow(i);
	});
	populated = true;
    }//end populateTree()
    
    private static class FeatureNameComparator implements Comparator<Feature<?>> {

	@Override
	public int compare(Feature<?> o1, Feature<?> o2) {
	    return o1.getClass().getSimpleName().compareToIgnoreCase(o2.getClass().getSimpleName());
	}
    }//end FeatureNameComparator
    
    private void populateTreeRecursive(DefaultMutableTreeNode node, Object feature) {
	final Set<Feature<?>> subFeatures = new HashSet<>();
	Features.getAllFeaturesOf(feature, subFeatures);
	subFeatures.stream().filter(x->!(x instanceof FeatureConfigurationUI)).sorted(new FeatureNameComparator()).forEach(feat->{
	    final FeatureEntry fe = new FeatureEntry(feat);
	    final Component editor = fe.getEditor();
	    featureEditPanel.add(editor, fe.toString());
	    final DefaultMutableTreeNode newTN = new DefaultMutableTreeNode(fe, true);
	    if(fe.getUserInterface() != null)
		userInterfaces.add(fe.getUserInterface());
	    userInterfaces.add(fe.getBasicSettingsPanel());
	    node.add(newTN);
	    populateTreeRecursive(newTN, feat);
	});;
    }//end populateTreeRecursive(...)

    public void refreshAndShow() {
	timeOfLastRefreshRequestMillis = System.currentTimeMillis();
	SwingUtilities.invokeLater(()->{
	    final JTree tree = getTree();
	    final JFrame rootFrame = getRootFrame();
	    ((DefaultTreeModel)tree.getModel()).reload();
	    for( int i = 0 ; i < tree.getRowCount(); i++ )
		tree.expandRow(i);
	    if(!populated)
		populateTree();
	    if( rootWindow != null)
		rootFrame.setLocation(rootWindow.getLocation());
	    tree.setSelectionRow(tree.getMaxSelectionRow());
	    cardLayout.show(featureEditPanel, "instructions");
	    rootFrame.setVisible(true);
	});
    }//end populateTreeRecursive(...)
    
    private void applyChanges() {
	for(ObjectEditorUI<?> ui : userInterfaces )
	    ui.proposeApplySettings();
    }//end applyChanges()
    
    private static class FeatureTreeCellRenderer implements TreeCellRenderer {
	private final JLabel label = new JLabel();
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean selected, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
	    //TODO: Bright font if has custom editor, italic if disabled, red font if error, yellow font if warning.
	    final FeatureEntry entry = (FeatureEntry)(((DefaultMutableTreeNode)value).getUserObject());
	    if( entry != null ) {
		Feature<?> feature = entry.getFeature();
		if( feature != null ) {
		    final Font font = label.getFont();
		    label.setFont(font.deriveFont(Font.BOLD));
		    label.setForeground(Color.white);
		    if( !entry.isCustomEdited())
			label.setForeground(label.getForeground().darker().darker());
		    label.setText(TRBeanUtils.camelCaseToSentence(feature.getClass().getSimpleName()));
		} //end if(feature !=null)
	    }//end if(entry != null)
	    //label.setBorder(selected?BorderFactory.createLineBorder(Color.red):null);//This causes the label to be smushed.
	    label.setBackground(selected?new Color(255,255,0,80):null);
	    label.setOpaque(selected);
	    return label;
	}//end getTreeCellRendererComponent
    }//end FeatureTreeCellRenderer

    public void setFeatureRoot(Features rootObject) {
	this.featureRoot = rootObject;
    }

}//end FeatureConfigWindow
