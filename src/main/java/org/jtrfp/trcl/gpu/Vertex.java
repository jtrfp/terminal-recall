package org.jtrfp.trcl.gpu;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Triangle;

public class Vertex {
    private ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    private Vector3D position;
    private Vector3D normal;
    
    public Vector3D getNormal(){
	if(normal!=null)return normal;
	Vector3D result = Vector3D.ZERO;
	for(Triangle triangle:triangles){
	    result = result.add(triangle.getCentroidNormal());
	}//end for(triangles)
	return result.scalarMultiply(1./(double)triangles.size());
    }//end getNormal()

    /**
     * @return the position
     */
    public Vector3D getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public Vertex setPosition(Vector3D position) {
        this.position = position;
        return this;
    }

    /**
     * @param normal the normal to set
     */
    public Vertex setNormal(Vector3D normal) {
        this.normal = normal;
        return this;
    }
    
    public Vertex addTriangle(Triangle triangleToAdd){
	triangles.add(triangleToAdd);
	return this;
    }

    public Vertex removeTriangle(Triangle triangle) {
	triangles.remove(triangle);
	return this;
    }
}//end Vertex
