package org.jtrfp.trcl.game;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.flow.TransientExecutor;

public class CustomLVLDialog extends JFrame {
	private JTextField lvlEntryField;
	private String [] lvlData = new String[1];
	private TVF3Game tvF3Game;
	
	public static void main(String [] args){
	    new CustomLVLDialog().setVisible(true);
	}//end main(...)
	
	public CustomLVLDialog() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(CustomLVLDialog.class.getResource("/org/freedesktop/tango/32x32/actions/document-open.png")));
		setTitle("Custom LVL");
		setSize(300,170);
		
		JPanel rootPanel = new JPanel();
		getContentPane().add(rootPanel, BorderLayout.NORTH);
		rootPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel bottomPanel = new JPanel();
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.setVerticalAlignment(SwingConstants.BOTTOM);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		bottomPanel.setLayout(new BorderLayout(0, 0));
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setHorizontalAlignment(SwingConstants.RIGHT);
		bottomPanel.add(cancelButton, BorderLayout.EAST);
		cancelButton.setVerticalAlignment(SwingConstants.BOTTOM);
		bottomPanel.add(okButton, BorderLayout.WEST);
		
		JPanel dataEntryPanel = new JPanel();
		getContentPane().add(dataEntryPanel, BorderLayout.CENTER);
		
		JTextArea txtrEnterTheName = new JTextArea();
		txtrEnterTheName.setWrapStyleWord(true);
		txtrEnterTheName.setLineWrap(true);
		txtrEnterTheName.setRows(3);
		txtrEnterTheName.setEditable(false);
		dataEntryPanel.add(txtrEnterTheName);
		txtrEnterTheName.setText("Enter the name of the LVL file to use, " +
				"not including the DATA directory.\n\n" +
				"To use a java resource, precede with `java://`\n" +
				"i.e. `java://directory/directory/file.txt`");
		
		lvlEntryField = new JTextField();
		lvlEntryField.setText("TERRAN.LVL");
		dataEntryPanel.add(lvlEntryField);
		lvlEntryField.setColumns(20);
		
		okButton.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent evt) {
			final String result = lvlEntryField.getText();
			setVisible(false);
			dispose();
			if(result.length()>0){
			    final TVF3Game game = getTvF3Game();
			    final Executor executor = TransientExecutor.getSingleton();
			    synchronized(executor){
			    executor.execute(new Runnable(){
				@Override
				public void run() {
				    try{
				    game.abortCurrentMission();
			            game.setLevelDirect(result);
				    game.doGameplay();
				    } catch(final Exception e){
					SwingUtilities.invokeLater(new Runnable(){
					    @Override
					    public void run() {
						JOptionPane.showMessageDialog(null, e.getMessage());
					    }});
				    }//end Exception()
				}});
			    }
			}//end if(actual result
		    }});
		
		cancelButton.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent evt) {
			lvlData[0] = "";
			synchronized(lvlData){
			 lvlData.notifyAll();}
			setVisible(false);
			dispose();
		    }});
	}//end constructor
	
	public String waitForResult(){
	    synchronized(lvlData){
		while(lvlData[0]==null)
		    try{lvlData.wait();}
		catch(InterruptedException e)
		    {e.printStackTrace();}
	    }//end sync lvlData
	    return getResult();
	}//end waitForResult()
	
	public String getResult(){
	    return lvlData[0];
	}

	public TVF3Game getTvF3Game() {
	    return tvF3Game;
	}

	public void setTvF3Game(TVF3Game tvF3Game) {
	    this.tvF3Game = tvF3Game;
	}
}//end CustomLVLDialog
