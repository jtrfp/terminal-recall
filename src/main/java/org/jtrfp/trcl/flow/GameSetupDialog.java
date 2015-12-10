package org.jtrfp.trcl.flow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jtrfp.trcl.flow.TVF3Game.Difficulty;

public class GameSetupDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    JComboBox cbDifficulty = new JComboBox();
    private JTextField txtCallsign;
    private Difficulty difficulty;
    private String     callSign;
    private boolean    beginMission;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	try {
	    GameSetupDialog dialog = new GameSetupDialog();
	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    dialog.setVisible(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Create the dialog.
     */
    public GameSetupDialog() {
    	setTitle("Pilot Registration");
    	setPreferredSize(new Dimension(355,120));
	setBounds(100, 100, 450, 300);
	getContentPane().setLayout(new BorderLayout());
	contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	getContentPane().add(contentPanel, BorderLayout.CENTER);
	GridBagLayout gbl_contentPanel = new GridBagLayout();
	gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0};
	gbl_contentPanel.rowHeights = new int[]{0, 0};
	gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
	gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
	contentPanel.setLayout(gbl_contentPanel);
	{
		JPanel rightPanel = new JPanel();
		{
		    GridBagConstraints gbc_rightPanel = new GridBagConstraints();
		    gbc_rightPanel.fill = GridBagConstraints.BOTH;
		    gbc_rightPanel.gridx = 2;
		    gbc_rightPanel.gridy = 0;
		    contentPanel.add(rightPanel, gbc_rightPanel);
			GridBagLayout gbl_rightPanel = new GridBagLayout();
			gbl_rightPanel.columnWidths = new int[]{0};
			gbl_rightPanel.rowHeights = new int[]{0, 0};
			gbl_rightPanel.columnWeights = new double[]{Double.MIN_VALUE};
			gbl_rightPanel.rowWeights = new double[]{0.0, 0.0};
			rightPanel.setLayout(gbl_rightPanel);
		}
		JPanel idPanel = new JPanel();
		idPanel.setBorder(new TitledBorder(null, "Identification", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_idPanel = new GridBagConstraints();
		gbc_idPanel.insets = new Insets(0, 0, 0, 5);
		gbc_idPanel.fill = GridBagConstraints.BOTH;
		gbc_idPanel.gridx = 1;
		gbc_idPanel.gridy = 0;
		contentPanel.add(idPanel, gbc_idPanel);
		GridBagLayout gbl_idPanel = new GridBagLayout();
		gbl_idPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_idPanel.rowHeights = new int[]{0, 0, 0};
		gbl_idPanel.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_idPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		idPanel.setLayout(gbl_idPanel);
		{
			JLabel lblCallSign = new JLabel("Call Sign:");
			lblCallSign.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblCallSign = new GridBagConstraints();
			gbc_lblCallSign.anchor = GridBagConstraints.WEST;
			gbc_lblCallSign.insets = new Insets(0, 0, 5, 5);
			gbc_lblCallSign.gridx = 0;
			gbc_lblCallSign.gridy = 0;
			idPanel.add(lblCallSign, gbc_lblCallSign);
		}
		{
			txtCallsign = new JTextField();
			txtCallsign.setText("Councilor");
			GridBagConstraints gbc_txtCallsign = new GridBagConstraints();
			gbc_txtCallsign.insets = new Insets(0, 0, 5, 5);
			gbc_txtCallsign.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtCallsign.gridx = 1;
			gbc_txtCallsign.gridy = 0;
			idPanel.add(txtCallsign, gbc_txtCallsign);
			txtCallsign.setColumns(10);
		}
		{
		JButton okButton = new JButton("Begin Mission");
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.anchor = GridBagConstraints.NORTH;
		gbc_okButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_okButton.insets = new Insets(0, 0, 5, 0);
		gbc_okButton.gridx = 2;
		gbc_okButton.gridy = 0;
		rightPanel.add(okButton, gbc_okButton);
		okButton.setActionCommand("OK");
		okButton.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().contentEquals("OK")){
			    setDifficulty((Difficulty)cbDifficulty.getSelectedItem());
			    setCallSign(txtCallsign.getText());
			    setBeginMission(true);
			    setVisible(false);
			    dispose();
			}//end if(OK)
		    }//end actionPerformed(...)
		});
		getRootPane().setDefaultButton(okButton);
		}
		{
			JLabel lblDifficulty = new JLabel("Difficulty:");
			lblDifficulty.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblDifficulty = new GridBagConstraints();
			gbc_lblDifficulty.insets = new Insets(0, 0, 0, 5);
			gbc_lblDifficulty.anchor = GridBagConstraints.WEST;
			gbc_lblDifficulty.gridx = 0;
			gbc_lblDifficulty.gridy = 1;
			idPanel.add(lblDifficulty, gbc_lblDifficulty);
		}
		{
			cbDifficulty.setModel(new DefaultComboBoxModel(Difficulty.values()));
			cbDifficulty.setSelectedIndex(1);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 0, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 1;
			idPanel.add(cbDifficulty, gbc_comboBox);
		}
		{
		JButton cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.NORTH;
		gbc_cancelButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_cancelButton.gridx = 2;
		gbc_cancelButton.gridy = 1;
		rightPanel.add(cancelButton, gbc_cancelButton);
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e) {
			setBeginMission(false);
			setVisible(false);
			dispose();
		    }});
		}
	}
	{
	    JPanel buttonPane = new JPanel();
	    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    getContentPane().add(buttonPane, BorderLayout.SOUTH);
	}
	pack();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public boolean isBeginMission() {
        return beginMission;
    }

    public void setBeginMission(boolean beginMission) {
        this.beginMission = beginMission;
    }

}
