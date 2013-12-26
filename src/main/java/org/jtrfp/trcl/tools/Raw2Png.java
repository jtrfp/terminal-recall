package org.jtrfp.trcl.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jtrfp.jtrfp.act.ActColor;
import org.jtrfp.jtrfp.act.ActFile;
import org.jtrfp.trcl.SpecialRAWDimensions;
import org.jtrfp.trcl.file.RAWFile;

public class Raw2Png {

    /**
     * @param args
     * @since Dec 26, 2013
     */
    public static void main(String[] args) {
	try{
	    File srcDir=new File(args[0]);
	    File destDir = new File(args[1]);
	    File actFile = new File(args[2]);
	    int xLen,yLen;
	    ArrayList<Color>palette = new ArrayList<Color>();
	    for(File srcFile:srcDir.listFiles()){
		if(srcFile.getName().endsWith(".RAW")){
    		RAWFile raw;
    		ActFile act = new ActFile(actFile);
        		raw = new RAWFile(new FileInputStream(srcFile));
        		if(raw.getSideLength()==0){
        			System.out.println("Error: Image is 0x0 pixels in size.");
        			System.exit(1);
        			}
        		for(ActColor color:act.getData().getColors())
        			{Color c = new Color(((byte)color.getComponent1())&0xFF,((byte)color.getComponent2())&0xFF,((byte)color.getComponent3())&0xFF);
        			palette.add(c);
        			}//end for(colors)
        		//System.out.println("Sidelength="+raw.getSideLength());
        		Dimension dims = SpecialRAWDimensions.getSpecialDimensions(raw.getRawBytes().length);
        		System.out.println("dims: "+dims);
        		xLen=(int)dims.getWidth();
        		yLen=(int)dims.getHeight();
        		byte [] bytes = raw.getRawBytes();
        		BufferedImage img = new BufferedImage(xLen,yLen,BufferedImage.TYPE_INT_RGB);
        		final Graphics g = img.getGraphics();
        		for(int y=0; y<yLen; y++){
        		    for(int x=0; x<xLen; x++){
        			g.setColor(palette.get(bytes[x+y*xLen]&0xFF));
        			g.fillRect(x, y, 1, 1);
        		    }//end fir(z)
        		}//end for(y)
        		ImageIO.write(img, "png", new File(destDir.getAbsolutePath()+"/"+srcFile.getName()+".png"));
		}//end if(.RAW)
	    }//end for(files)
	}//end try{}
	catch(Exception e){
	    e.printStackTrace();
	    System.out.println("USAGE: Raw2Png [source directory with .RAW files] [dest directory for PNGs] [ACT file for palette]");
	}
    }

}
