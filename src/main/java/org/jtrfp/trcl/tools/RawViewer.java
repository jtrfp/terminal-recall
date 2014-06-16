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
package org.jtrfp.trcl.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jtrfp.jtrfp.act.ActColor;
import org.jtrfp.jtrfp.act.ActFile;
import org.jtrfp.trcl.SpecialRAWDimensions;
import org.jtrfp.trcl.file.RAWFile;

public class RawViewer extends JFrame
	{
	private static final int PIXEL_SIZE=4;
	ArrayList<Color>palette = new ArrayList();
	RAWFile raw;
	int xLen,yLen;
	public RawViewer(File RAWFile, File ACTFile)
		{
		super("RAW Viewer - "+RAWFile.getName());
		try
			{
			ActFile act = new ActFile(ACTFile);
			raw = new RAWFile(new FileInputStream(RAWFile));
			if(raw.getSideLength()==0)
				{
				System.out.println("Error: Image is 0x0 pixels in size.");
				System.exit(1);
				}
			for(ActColor color:act.getData().getColors())
				{Color c = new Color(((byte)color.getComponent1())&0xFF,((byte)color.getComponent2())&0xFF,((byte)color.getComponent3())&0xFF);
				palette.add(c);
				}//end for(colors)
			//System.out.println("Sidelength="+raw.getSideLength());
			this.getContentPane().add(new ImgPanel());
			Dimension dims = SpecialRAWDimensions.getSpecialDimensions(raw.getRawBytes().length);
			System.out.println("dims: "+dims);
			xLen=(int)dims.getWidth();
			yLen=(int)dims.getHeight();
			/*
			if((raw.getSideLength() & (raw.getSideLength()-1))!=0)
				{
				System.out.println("Detected non-power-of-two. Thinking some more...");
				
				double len = raw.getRawBytes().length;
				//Try various numbers and see if they come out whole
				for(double i=1; i<len; i++)
					{
					double fit = len/i;
					if(Math.abs(fit-Math.round(fit))<.0001)
						{
						xLen=(int)i;
						yLen=(int)fit;
						System.out.println("Possible size: "+xLen+"x"+yLen);
						}
					}//end for(len)
				
				if(raw.getSideLength()==143){xLen=320; yLen=64;}
				if(raw.getSideLength()==252){xLen=320; yLen=200;}
				}
			*/
			setSize(xLen*PIXEL_SIZE,yLen*PIXEL_SIZE);
			setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		catch(Exception e){e.printStackTrace();}
		}
	
	public static void main(String [] args)
		{
		File act=null;
		if(args.length<1 || args.length>2)
			{fail();}
		if(args.length==1)
			{//Auto-find an ACT
			if(!new File(args[0]).exists())fail();
			File directory = new File(args[0]).getParentFile();
			for(File f:directory.listFiles())
				{
				if(f.getName().toUpperCase().endsWith(".ACT") && f.length()>64)
					{
					String fileSimpleName=f.getName().substring(f.getName().indexOf("\\")+1, f.getName().length()-4);//Remove heading backslash if exists
					String testSimpleName=args[0].substring(args[0].indexOf("\\")+1, args[0].length()-4);
					System.out.println("fileSimpleName="+fileSimpleName+" testSimpleName="+testSimpleName);
					if(act==null)act=f; //By default, accept any old .ACT file in the same directory
					else if(fileSimpleName.contentEquals(testSimpleName))act=f; //If there is an ACT file of same name, favor it.
					}
				}//end for(directory.files)
			if(act==null)fail();
			}
		else act=new File(args[1]);
		System.out.println("Using act file "+act.getName());
		new RawViewer(new File(args[0]),act);
		}
	
	private static void fail()
		{
		System.out.println("USAGE: RawViewer [path_to_RAW_file] [OPTIONAL path_to_ACT_palette]");
		System.exit(1);
		}
	
	private class ImgPanel extends JPanel
		{
		@Override
		public void paint(Graphics g)
			{
			double xScale=(double)this.getWidth()/(double)xLen;
			double yScale=(double)this.getHeight()/(double)yLen;
			g.setColor(Color.BLACK);
			byte [] bytes = raw.getRawBytes();
			for(int i=0; i<bytes.length; i++)
				{
				int x=(int)Math.round((double)(i%xLen)*xScale);
				int y=(int)Math.round((double)(i/xLen)*yScale);
				g.setColor(palette.get(bytes[i] & 0xFF));
				g.fillRect(x, y, x+(int)Math.round(xScale), y+(int)Math.round(yScale));
				}//end for(i)
			}//end paint(...)
		}//end ImgPanel
	}//end RawViewer
