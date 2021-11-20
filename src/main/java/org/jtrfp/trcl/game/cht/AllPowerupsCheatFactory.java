/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.game.cht;

import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.KeyedExecutor;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.game.cht.AllAfterburnerCheatFactory.AllAfterburnerCheat;
import org.jtrfp.trcl.game.cht.AllAmmoCheatFactory.AllAmmoCheat;
import org.jtrfp.trcl.game.cht.FastAfterburnerCheatFactory.FastAfterburnerCheat;
import org.jtrfp.trcl.game.cht.InvincibilityCheatFactory.InvincibilityCheat;
import org.jtrfp.trcl.game.cht.InvisibilityCheatFactory.InvisibilityCheat;
import org.jtrfp.trcl.game.cht.ShieldRestoreCheatFactory.ShieldRestoreCheat;
import org.springframework.stereotype.Component;

@Component
public class AllPowerupsCheatFactory extends AbstractCheatFactory {
    private static final String CHEAT_NAME = "All Powerups";
    private static final String [] CELEBRATIONS = {"Jackpot!", "Have fun!"};

    protected AllPowerupsCheatFactory() {
	super(CHEAT_NAME);
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return AllPowerupsCheat.class;
    }
    
    public class AllPowerupsCheat extends AbstractCheatItem {
	
	public AllPowerupsCheat() {super();}

	@Override
	protected void invokeCheat() {
	    final TVF3Game game = this.getTarget();
	    new Thread(()->{
		try {
		    KeyedExecutor ex = TransientExecutor.getSingleton();
		    ex.execute(()->{Features.get(game, AllAfterburnerCheat.class).invokeCheat();});
		    Thread.sleep(250);
		    ex.execute(()->{Features.get(game, AllAmmoCheat.class).invokeCheat();});
		    Thread.sleep(250);
		    ex.execute(()->{Features.get(game, ShieldRestoreCheat.class).invokeCheat();});
		    Thread.sleep(250);
		    ex.execute(()->{Features.get(game, InvisibilityCheat.class).invokeCheat();});
		    Thread.sleep(250);
		    ex.execute(()->{Features.get(game, InvincibilityCheat.class).invokeCheat();});
		    Thread.sleep(250);
		    ex.execute(()->{
			final UpfrontDisplay disp = game.getUpfrontDisplay();
			disp.submitMomentaryUpfrontMessage(CELEBRATIONS[(int)Math.random() * CELEBRATIONS.length]);
		    });
		} catch(InterruptedException e) {}
	    }).start();
	}//end invokeCheat()
	
    }//end AllPowerupsCheat

}//end AllPowerupsCheatFactory
