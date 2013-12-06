/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

public class Triangle
        {
        double [] x = new double[3];
        double [] y = new double[3];
        double [] z = new double[3];
       
        double [] u = new double[3];
        double [] v = new double[3];
       
        RenderMode renderMode;
        private boolean isAlphaBlended=false;
       
        TextureDescription texture;

        /**
         * Converts supplied quad coordinates to a pair of triangles in clockwise order, top-left being index zero.
         *
         */
        public static Triangle [] quad2Triangles(double [] x, double [] y, double [] z, double [] u, double [] v, TextureDescription textureToUse, RenderMode mode)
                {
                Triangle [] result = new Triangle[2];
               
                int vtx=0;
                int qvx;
                //TOP LEFT (0)
                qvx=0;
                Triangle t;
                t=new Triangle();
                t.setTexture(textureToUse);
                t.setRenderMode(mode);
                t.x[vtx]=x[qvx];t.y[vtx]=y[qvx];t.z[vtx]=z[qvx];
                t.u[vtx]=u[qvx]; t.v[vtx]=v[qvx];
                //BOTTOM LEFT (3)
                qvx=3;
                vtx++;
                t.x[vtx]=x[qvx];t.y[vtx]=y[qvx];t.z[vtx]=z[qvx];
                t.u[vtx]=u[qvx]; t.v[vtx]=v[qvx];
                //BOTTOM RIGHT (2)
                qvx=2;
                vtx++;
                t.x[vtx]=x[qvx];t.y[vtx]=y[qvx];t.z[vtx]=z[qvx];
                t.u[vtx]=u[qvx]; t.v[vtx]=v[qvx];
               
                result[0]=t;
               
                t = new Triangle();
                t.setTexture(textureToUse);
                t.setRenderMode(mode);
               
                vtx=0;
                //TOP LEFT (0)
                qvx=0;
                t.x[vtx]=x[qvx];t.y[vtx]=y[qvx];t.z[vtx]=z[qvx];
                t.u[vtx]=u[qvx]; t.v[vtx]=v[qvx];
                //TOP RIGHT (1)
                qvx=1;
                vtx++;
                t.x[vtx]=x[qvx];t.y[vtx]=y[qvx];t.z[vtx]=z[qvx];
                t.u[vtx]=u[qvx]; t.v[vtx]=v[qvx];
                //BOTTOM RIGHT (2)
                vtx++;
                qvx=2;
                t.x[vtx]=x[qvx];t.y[vtx]=y[qvx];t.z[vtx]=z[qvx];
                t.u[vtx]=u[qvx]; t.v[vtx]=v[qvx];
               
                result[1]=t;
               
                return result;
                }
       
        /**
         * @return the x
         */
        public double[] getX()
                {
                return x;
                }

        /**
         * @param x the x to set
         */
        public void setX(double[] x)
                {
                this.x = x;
                }

        /**
         * @return the y
         */
        public double[] getY()
                {
                return y;
                }

        /**
         * @param y the y to set
         */
        public void setY(double[] y)
                {
                this.y = y;
                }

        /**
         * @return the z
         */
        public double[] getZ()
                {
                return z;
                }

        /**
         * @param z the z to set
         */
        public void setZ(double[] z)
                {
                this.z = z;
                }

        /**
         * @return the u
         */
        public double[] getU()
                {
                return u;
                }

        /**
         * @param u the u to set
         */
        public void setU(double[] u)
                {
                this.u = u;
                }

        /**
         * @return the v
         */
        public double[] getV()
                {
                return v;
                }

        /**
         * @param v the v to set
         */
        public void setV(double[] v)
                {
                this.v = v;
                }

        /**
         * @return the renderMode
         */
        public RenderMode getRenderMode()
                {
                return renderMode;
                }

        /**
         * @param renderMode the renderMode to set
         */
        public void setRenderMode(RenderMode renderMode)
                {
                this.renderMode = renderMode;
                }

        /**
         * @return the texture
         */
        public TextureDescription getTexture()
                {
                return texture;
                }

        /**
         * @param texture the texture to set
         */
        public void setTexture(TextureDescription texture)
                {
                this.texture = texture;
                }
       
        public String toString()
                {
                //new RuntimeException().printStackTrace();
                return "TRIANGLE {"+x[0]+", "+y[0]+", "+z[0]+"} , {"+x[1]+", "+y[1]+", "+z[1]+"} , {"+x[2]+", "+y[2]+", "+z[2]+"}\n"+
                                "\tUV: {"+u[0]+", "+v[0]+" }"+"  {"+u[1]+", "+v[1]+" }"+" {"+u[2]+", "+v[2]+" }";
                }

		/**
		 * @return the isAlphaBlended
		 */
		public boolean isAlphaBlended()
			{
			return isAlphaBlended;
			}

		/**
		 * @param isAlphaBlended the isAlphaBlended to set
		 */
		public void setAlphaBlended(boolean isAlphaBlended)
			{
			this.isAlphaBlended = isAlphaBlended;
			}
        }//Triangle

