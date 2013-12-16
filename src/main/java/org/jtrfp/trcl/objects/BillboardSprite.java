package org.jtrfp.trcl.objects;

import java.awt.Dimension;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TR;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;


public class BillboardSprite extends WorldObject
	{
	Dimension dim;
	
	public BillboardSprite(TR tr){super(tr);}
	@Override
	protected void recalculateTransRotMBuffer()
		{
		this.setHeading(getTr().getRenderer().getCamera().getLookAtVector().negate());
		this.setTop(getTr().getRenderer().getCamera().getUpVector());
		super.recalculateTransRotMBuffer();
		}//end recalculateTransRotMBuffer()
	
	public void setBillboardSize(Dimension dim)
		{this.dim=dim;}
	
	public void setTexture(TextureDescription desc, boolean useAlpha)
		{
		if(dim==null)throw new NullPointerException("Billboard size must be non-null. (did you forget to set it?)");
		Triangle[] tris= Triangle.quad2Triangles(
				new double[]{-.5*dim.getWidth(),.5*dim.getWidth(),.5*dim.getWidth(),-.5*dim.getWidth()}, //X
				new double[]{-.5*dim.getHeight(),-.5*dim.getHeight(),.5*dim.getHeight(),.5*dim.getHeight()}, 
				new double[]{0,0,0,0}, 
				new double[]{0,1,1,0}, //U
				new double[]{0,0,1,1}, 
				desc, 
				RenderMode.DYNAMIC);
		tris[0].setAlphaBlended(useAlpha);
		tris[1].setAlphaBlended(useAlpha);
		Model m = new Model(false);
		m.addTriangles(tris);
		setModel(m.finalizeModel());
		}
	}//end BillboardSprite
