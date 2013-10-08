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
package com.ritolaaudio.trcl.flow;

import java.io.File;

import javax.swing.JOptionPane;

import com.ritolaaudio.trcl.TR;
import com.ritolaaudio.trcl.file.VOXFile;

public class Game
	{
	private TR tr;
	private VOXFile vox;
	private int levelIndex=0;
	private String playerName;
	private Difficulty difficulty;
	
	public Game(){}
	
	public Game(TR tr, VOXFile vox)
		{
		setTr(tr);
		setVox(vox);
		setupNameWithUser();
		}//end constructor
	
	private void setupNameWithUser()
		{
		setPlayerName((String)JOptionPane.showInputDialog(tr.getFrame(),
				"Callsign:","Pilot Registration",JOptionPane.PLAIN_MESSAGE,
				null,null,"Councilor"));
		String difficulty=(String)JOptionPane.showInputDialog(tr.getFrame(),
				"Difficulty:","Pilot Registration",JOptionPane.PLAIN_MESSAGE,
				null,new String[]{"Easy","Normal","Hard","Furious"},"Normal");
		if(difficulty.contentEquals("Easy"))
			{setDifficulty(Difficulty.EASY);}
		if(difficulty.contentEquals("Normal"))
			{setDifficulty(Difficulty.NORMAL);}
		if(difficulty.contentEquals("Hard"))
			{setDifficulty(Difficulty.HARD);}
		if(difficulty.contentEquals("Furious"))
			{setDifficulty(Difficulty.FURIOUS);}
		}//end setupNameWithUser()
	
	public void save(File fileToSaveTo)
		{
		//TODO
		}

	/**
	 * @return the tr
	 */
	public TR getTr()
		{
		return tr;
		}

	/**
	 * @param tr the tr to set
	 */
	public void setTr(TR tr)
		{
		this.tr = tr;
		}

	/**
	 * @return the vox
	 */
	public VOXFile getVox()
		{
		return vox;
		}

	/**
	 * @param vox the vox to set
	 */
	public void setVox(VOXFile vox)
		{
		this.vox = vox;
		}

	/**
	 * @return the levelIndex
	 */
	public int getLevelIndex()
		{
		return levelIndex;
		}

	/**
	 * @param levelIndex the levelIndex to set
	 */
	public void setLevelIndex(int levelIndex)
		{
		this.levelIndex = levelIndex;
		}

	/**
	 * @return the playerName
	 */
	public String getPlayerName()
		{
		return playerName;
		}

	/**
	 * @param playerName the playerName to set
	 */
	public void setPlayerName(String playerName)
		{
		this.playerName = playerName;
		}
	
	enum Difficulty
		{
		EASY,
		NORMAL,
		HARD,
		FURIOUS
		}

	/**
	 * @return the difficulty
	 */
	public Difficulty getDifficulty()
		{
		return difficulty;
		}

	/**
	 * @param difficulty the difficulty to set
	 */
	public void setDifficulty(Difficulty difficulty)
		{
		this.difficulty = difficulty;
		}
	}//end Game
