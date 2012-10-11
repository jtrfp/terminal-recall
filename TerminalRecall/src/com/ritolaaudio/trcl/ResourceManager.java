/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import jtrfp.common.FileLoadException;
import jtrfp.common.act.ActColor;
import jtrfp.common.internal.act.ActDataLoader;
import jtrfp.common.internal.raw.RawDataLoader;
import jtrfp.common.internal.tex.TexDataLoader;
import jtrfp.common.pod.PodFile;
import jtrfp.common.raw.IRawData;

import com.ritolaaudio.trcl.file.CLRFile;
import com.ritolaaudio.trcl.file.LVLFile;

public class ResourceManager
	{
	PodFile pod;
	public ResourceManager(File fileToUse)
		{
		if(fileToUse==null)throw new NullPointerException("fileToUse should not be null.");
		pod = new PodFile(fileToUse);
		}//end ResourceManager
	
	public LVLFile getLVL(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		System.out.println("Getting level "+name);
		return new LVLFile(pod.getData().
				findEntry("LEVELS\\"+name).
				getInputStreamFromPod());
		}//end getLVL
	
	public static void releaseTextures(Texture [] tex)
		{
		for(Texture t:tex)
			{t.free();}
		}//end releaseTextures()
	
	public Texture [] getTextures(String texFileName, String actFileName, ColorProcessor proc) throws IOException, FileLoadException, IllegalAccessException
		{
		String [] files = getTEXListFile(texFileName);
		Texture [] result = new Texture[files.length];
		Color [] palette = getPalette(actFileName);
		for(int i=0; i<files.length;i++)
			{result[i]=getRAWAsTexture(files[i],palette,proc);}
		return result;
		}//end loadTextures(...)
	
	public Texture getRAWAsTexture(String name, Color [] palette, ColorProcessor proc) throws IOException, FileLoadException, IllegalAccessException
		{
		Color [][] img = getRAWImage(name,palette,proc);
		return new Texture(img);
		}
	
	public Color [][] getRAWImage(String name, Color [] palette, ColorProcessor proc) throws IOException, FileLoadException, IllegalAccessException
		{
		IRawData dat = RawDataLoader.load(pod.getData().
				findEntry("ART\\"+name).
				getInputStreamFromPod());
		
		Color [][] result = new Color[dat.getWidth()][dat.getHeight()];
		for(int z=0; z<dat.getHeight(); z++)
			{
			for(int x=0; x<dat.getWidth(); x++)
				{
				result[x][z]=proc.process(palette[dat.getValueAt(x, z)]);//backwards workaround for bug.
				}//end for(x)
			}//end for(z)
		return result;
		}//end getRAWImage
	
	public AltitudeMap getRAWAltitude(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		return new RawAltitudeMapWrapper(RawDataLoader.load(pod.getData().
				findEntry("DATA\\"+name).
				getInputStreamFromPod()));
		}//end getRAWAltitude
	
	public Texture [][] getTerrainTextureGrid(String name, Texture [] texturePalette) throws IOException, FileLoadException, IllegalAccessException
		{
		final CLRFile	dat = new CLRFile(pod.getData().
						findEntry("DATA\\"+name).
						getInputStreamFromPod());
		
		Texture result[][] = new Texture[dat.getSideLength()][dat.getSideLength()];
		for(int z=0; z<dat.getSideLength(); z++)
			{
			for(int x=0; x<dat.getSideLength(); x++)
				{
				result[x][z]=texturePalette[dat.valueAt(x, z)];
				}//end for(x)
			}//end for(z)
		return result;
		}//end getRAWAltitude
	
	public String [] getTEXListFile(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		return TexDataLoader.load(pod.getData().
				findEntry("DATA\\"+name).
				getInputStreamFromPod()).getTextureNames();
		}//end getTEXListFile
	
	public Color [] getPalette(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		ActColor [] actColors= ActDataLoader.load(pod.getData().
				findEntry("ART\\"+name).
				getInputStreamFromPod()).getColors();
		
		Color [] result = new Color[actColors.length];
		for(int i=0; i<result.length;i++)
			{
			result[i]=new Color(actColors[i].getComponent1(),actColors[i].getComponent2(),actColors[i].getComponent3());
			}
		return result;
		}//end getTEXListFile
	}//end ResourceManager
