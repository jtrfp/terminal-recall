package org.jtrfp.trcl.game;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.springframework.stereotype.Component;

@Component
public class CustomLVLMenuItemFactory implements FeatureFactory<TVF3Game> {
    private MenuSystem menuSystem;

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	return new CustomLVLMenuItem();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return CustomLVLMenuItem.class;
    }
    
    public class CustomLVLMenuItem implements Feature<TVF3Game> {
	private final String [] CUSTOM_LVL_PATH = new String[]{"Game","Run Custom .LVL"};
	private final CustomLVLListener customLVLListener = new CustomLVLListener();
	private TVF3Game target;
	private RunStateListener runStateListener = new RunStateListener();
	private WeakPropertyChangeListener weakRunStateListener;
	private volatile boolean destructed = false;

	@Override
	public void apply(TVF3Game target) {
	    setTarget(target);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, CUSTOM_LVL_PATH);
	    menuSystem.addMenuItemListener(customLVLListener, CUSTOM_LVL_PATH);
	    final TR tr = target.getTr();
	    weakRunStateListener = new WeakPropertyChangeListener(runStateListener,tr);
	    target.getTr().addPropertyChangeListener(TRFactory.RUN_STATE, weakRunStateListener);
	}

	public void destruct(TVF3Game target) {
	    destructed = true;
	    final MenuSystem menuSystem = getMenuSystem();
	    getTarget().getTr().removePropertyChangeListener(TRFactory.RUN_STATE, weakRunStateListener);
	    menuSystem.removeMenuItem(CUSTOM_LVL_PATH);
	    setTarget(null);
	}
	
	private class CustomLVLListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		//Show the CustomLVLDialog
		showLVLDialog();
	    }//end actionPerformed()
	}//end CustomLVLListener
	
	private void showLVLDialog(){
	    final CustomLVLDialog dialog = new CustomLVLDialog();
	    dialog.setTvF3Game(getTarget());
	    dialog.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
	    dialog.setVisible(true);
	}

	protected TVF3Game getTarget() {
	    return target;
	}

	protected void setTarget(TVF3Game target) {
	    this.target = target;
	}
	
	private class RunStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(destructed)
		    return;
		final Object newValue = evt.getNewValue();
		getMenuSystem().setMenuItemEnabled(newValue instanceof Game.GameLoadedMode,CUSTOM_LVL_PATH);
	    }//end propertyChange(...)
	}//end RunStateListener()
	
	public MenuSystem getMenuSystem() {
	    if(menuSystem == null){
	        final Frame frame = getTarget().
	        	                getTr().
	        	                getRootWindow();
	        menuSystem = Features.get(frame, MenuSystem.class);
	        }
	    return menuSystem;
	    }
    }//end CustomLVLMenuItem
}//end CustomLVLMenuItemFactory
