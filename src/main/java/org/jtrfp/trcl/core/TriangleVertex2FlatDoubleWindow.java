package org.jtrfp.trcl.core;

public class TriangleVertex2FlatDoubleWindow {
    private final TriangleVertexWindow parentWindow;
    public TriangleVertex2FlatDoubleWindow(TriangleVertexWindow parentWindow){
	this.parentWindow=parentWindow;
    }
    
    public static enum Variable{
	X(0),
	Y(1),
	Z(2),
	U(3),
	V(4);
	private int idOffset;
	private Variable(int idOffset){
	    this.idOffset=idOffset;
	}
	protected int getIDOff(){return idOffset;}
	public int getFlatID(int vertexID){
	    return vertexID*Variable.values().length+getIDOff();
	}
    }//end Variable
    
    public void set(int id, double val){
	final int vid=id/5;
	id%=5;
	switch (id){
	    case 0:
		parentWindow.setX(vid,(short)val);
		break;
	    case 1:
		parentWindow.setY(vid,(short)val);
		break;
	    case 2:
		parentWindow.setZ(vid,(short)val);
		break;
	    case 3:
		parentWindow.setU(vid,(short)val);
		break;
	    case 4:
		parentWindow.setV(vid,(short)val);
		break;
	}//end switch(...)
    }//end set(...)
}//end Triangle...Window
