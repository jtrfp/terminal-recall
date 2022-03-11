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

package org.jtrfp.trcl.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.TypeRunStateHandler;
import org.jtrfp.trcl.conf.ui.CheckboxUI;
import org.jtrfp.trcl.conf.ui.ComboBoxUI;
import org.jtrfp.trcl.conf.ui.ConfigByUI;
import org.jtrfp.trcl.conf.ui.ToolTip;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.GameResourcePODRegistry;
import org.jtrfp.trcl.core.GraphStabilizationListener;
import org.jtrfp.trcl.core.PackedTreeNode;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.shell.GameCampaignRegistryFactory.GameCampaignRegistry;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.shell.GameShellFactory.GameShellReady;
import org.springframework.stereotype.Component;

@Component
public class GameAutoLoaderFactory implements FeatureFactory<GameShell> {
    public static class GameAutoLoader implements Feature<GameShell>, GraphStabilizationListener {
	private static final GameCampaignData DEFAULT_GAME_CAMPAIGN_DATA = new GameCampaignData();
	static {
	    DEFAULT_GAME_CAMPAIGN_DATA.setName("None");
	}
	
	private boolean enabled = false;
	private boolean alreadyAutoStarted = false;
	private final DefaultMutableTreeNode campaignRoot = new DefaultMutableTreeNode(DEFAULT_GAME_CAMPAIGN_DATA);
	private final GameCampaignCollectionSink campaignCollectionSink = new GameCampaignCollectionSink();
	private DefaultMutableTreeNode selectedCampaign = campaignRoot;
	private TR tr;
	private GameShell target;
	private RootWindow rw;
	
	public GameAutoLoader() {}

	@Override
	public void apply(GameShell target) {
	    this.tr = target.getTr();
	    this.target = target;
	    final GameCampaignRegistry registry = Features.get(target, GameCampaignRegistry.class);
	    registry.getCampaigns().addTarget(campaignCollectionSink, true);
	}//end apply(...)

	@Override
	public void destruct(GameShell target) {
	    // TODO Auto-generated method stub
	    
	}

	@ToolTip(text="Selected campaign (if any) is automatically run if enabled.")
	@ConfigByUI(editorClass=CheckboxUI.class)
	public boolean isEnabled() {
	    return enabled;
	}

	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	}
	
	@ToolTip(text="Selects which campaign to automatically run when the program starts.")
	@ConfigByUI(editorClass=ComboBoxUI.class)
	public DefaultMutableTreeNode getSelectedCampaign() {
	    return selectedCampaign;
	}//end getGameCampaignToLoad()
	
	public void setSelectedCampaign(DefaultMutableTreeNode node) {
	    System.out.println("setSelectedCampaign "+node);
	    if( Objects.equals(node, this.selectedCampaign))
		return;
	    this.selectedCampaign = node;
	    
	    proposeAutoStart();
	}//end setSelectedCampaign()
	
	public PackedTreeNode getPackedSelectedCampaign() {
	    final PackedTreeNode result = new PackedTreeNode();
	    result.setNode(selectedCampaign);
	    result.setRoot(campaignRoot);
	    return result;
	}
	
	private void proposeAutoStart() {
	    if( alreadyAutoStarted )
		return;
	    if( selectedCampaign != null && !alreadyAutoStarted && tr.getRunState() instanceof GameShellReady)
		performAutoStart();
	}//end reEvaluateAutoStartState()
	
	private void performAutoStart() {
	    alreadyAutoStarted = true;
	    SwingUtilities.invokeLater(()-> {
		if( !isEnabled() )
		    return;
		final GameCampaignData selectedCampaignData = (GameCampaignData)(getSelectedCampaign().getUserObject());

		if( selectedCampaignData == null || getSelectedCampaign() == campaignRoot )
		    return;

		if(selectedCampaignData.getPodURIs().isEmpty()) {
		    JOptionPane.showMessageDialog(rw, "This game campaign's resource data specifies no POD sources.\n"
			    + "To specify POD sources, navigate to File->Features->GameCampaignRegistry\n...and add POD URIs by editing the item `"+selectedCampaignData.getName()+"`", 
			    "No PODs Specified",
			    JOptionPane.WARNING_MESSAGE);
		    return;
		}

		GameAutoLoader.this.rw = Features.get(tr, RootWindow.class);
		final ResourceManager rm = tr.getResourceManager();
		System.out.println("entered run state. Selected campaign: "+selectedCampaignData);

		rm.clearCaches();
		rm.setPodRegistry(new GameResourcePODRegistry(selectedCampaignData));
		TRFactory.nuclearGC();
		final String voxURI = selectedCampaignData.getVoxURI();
		VOXFile vox = null;
		if(voxURI != null) {
		    if(voxURI.length() > 0)
			try {vox = rm.getVOXFile(voxURI);}
		    catch(FileLoadException | IOException | IllegalAccessException ex) {
			JOptionPane.showMessageDialog(rw, 
				"Could not load custom VOX URI; attempting to to use a default VOX.\n"+voxURI+"\nMessage was: "+ex.getLocalizedMessage(), 
				"VOX Load Error", 
				JOptionPane.WARNING_MESSAGE);
		    }
		}//end if(!null)
		GameAutoLoader.this.target.newGame(vox, selectedCampaignData.getGameVersion());
	    });
	}
	
	public void setPackedSelectedCampaign(PackedTreeNode node) {//TODO: This has to be re-parsed each time.
	    final DefaultMutableTreeNode newNode = node.getNode();
	    //final Object toFind = newNode.getUserObject();
	    //final Iterator<TreeNode> it = campaignRoot.children().asIterator();
	    setSelectedCampaign(newNode);
	    /*
	    System.out.println("Searching for a node with user object matching "+toFind);
	    while(it.hasNext()) {
		final DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode)(it.next());
		if( Objects.equals(thisNode.getUserObject(), toFind) ) {
		    System.out.println("setting selectedCampaign to "+thisNode);
		    setSelectedCampaign(thisNode);
		}
	    }//end while(hasNext)
	    */
	}//end setPackedSelectedCampaign(...)

	private class GameCampaignCollectionSink implements Collection<GameCampaignData> {

	    @Override
	    public int size() {
		return campaignRoot.getChildCount();
	    }

	    @Override
	    public boolean isEmpty() {
		return campaignRoot.getChildCount() == 0;
	    }

	    @Override
	    public boolean contains(Object o) {
		final Iterator<TreeNode> it = campaignRoot.children().asIterator();
		while(it.hasNext()) {
		    final Object obj = ((DefaultMutableTreeNode)(it.next())).getUserObject();
		    if(Objects.equals(obj, o))
			return true;
		}
		
		return false;
	    }

	    @Override
	    public Iterator iterator() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Object[] toArray() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public boolean add(GameCampaignData e) {
		campaignRoot.add(new DefaultMutableTreeNode(e));
		proposeAutoStart();
		return true;
	    }

	    @Override
	    public boolean remove(Object o) {
		final Iterator<TreeNode> it = campaignRoot.children().asIterator();
		final ArrayList<TreeNode> toRemove = new ArrayList<>();
		while(it.hasNext()) {
		    final TreeNode node = it.next();
		    final Object obj = ((DefaultMutableTreeNode)(node)).getUserObject();
		    if(Objects.equals(obj, o)) {
			toRemove.add(node);
		    }//end if(equals)
		}//end while(hasNext)
		if(toRemove.isEmpty())
		    return false;
		else
		    for(TreeNode n : toRemove)
			campaignRoot.remove((DefaultMutableTreeNode)n);
		return false;
	    }//end remove(...)

	    @Override
	    public boolean containsAll(Collection c) {
		for(Object o : c) {
		    if( !contains(o) )
			return false;
		}
		return true;
	    }

	    @Override
	    public boolean addAll(Collection c) {
		for( Object o : c)
		    add((GameCampaignData)o);
		return true;
	    }

	    @Override
	    public boolean removeAll(Collection<?> c) {
		for( Object o : c)
		    remove(o);
		return true;
	    }

	    @Override
	    public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public void clear() {
		campaignRoot.removeAllChildren();
	    }
	    
	}//end GameCampaignCollectionSink

	@Override
	public void graphStabilized(Object target) {
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, new TypeRunStateHandler(GameShellReady.class) {

		@Override
		public void enteredRunState(Object oldState, Object newState) {
		    performAutoStart();
		}//end enteredRunState()

		@Override
		public void exitedRunState(Object oldState, Object newState) {}
	    });
	}//end graphStabilized(...)
	
    }//end GameAutoLoader

    @Override
    public Feature<GameShell> newInstance(GameShell target)
	    throws FeatureNotApplicableException {
	return new GameAutoLoader();
    }

    @Override
    public Class<GameShell> getTargetClass() {
	return GameShell.class;
    }

    @Override
    public Class<? extends Feature<GameShell>> getFeatureClass() {
	return GameAutoLoader.class;
    }
}//end GameAutoLoaderFactory
