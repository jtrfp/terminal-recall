/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.obj;

import java.awt.Dimension;
import java.lang.ref.WeakReference;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.LimitedLifeSpan;
import org.jtrfp.trcl.beh.ProjectileBehavior;
import org.jtrfp.trcl.beh.phy.Velocible;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.ModelingType;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileBillboard extends BillboardSprite implements Projectile {
    public static final long LIFESPAN_MILLIS=4500;
    private WeakReference<WorldObject> objectOfOrigin = new WeakReference<WorldObject>(null);
    public ProjectileBillboard(TR tr,Weapon w,Texture textureToUse,ExplosionType explosionType,String debugName) {
	super(tr,debugName);
	addBehavior(new ProjectileBehavior(this,w.getDamage(),explosionType,w.isHoning()));
	ModelingType.BillboardModelingType mt = (ModelingType.BillboardModelingType)w.getModelingType();
	this.setBillboardSize(new Dimension((int)(mt.getBillboardSize().getWidth()/TR.crossPlatformScalar),(int)(mt.getBillboardSize().getHeight()/TR.crossPlatformScalar)));
	this.setTexture(textureToUse, true);
    }
    public void reset(double [] newPos, Vector3D newVelocity, WorldObject objectOfOrigin){
	this.objectOfOrigin= new WeakReference<WorldObject>(objectOfOrigin);
	probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	setHeading(newVelocity.normalize());
	setPosition(newPos[0],newPos[1],newPos[2]);
	setVisible(true);
	setActive(true);
	probeForBehavior(Velocible.class).setVelocity(newVelocity);
	probeForBehavior(ProjectileBehavior.class).reset(newVelocity.normalize(),newVelocity.getNorm());
    }//end reset()
    @Override
    public WorldObject getObjectOfOrigin() {
	return objectOfOrigin.get();
    }//end getObjectOfOrigin()
}//end ProjectileBillboard
