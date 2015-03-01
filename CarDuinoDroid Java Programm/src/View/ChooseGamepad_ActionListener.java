package View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Controller.Controller_Computer;
import Model.Log;

public class ChooseGamepad_ActionListener implements ActionListener {
	Controller_Computer controller_Computer;
	Log log;
	
	public ChooseGamepad_ActionListener(Controller_Computer controllercomputer, Log LOG){
		controller_Computer = controllercomputer;
		log = LOG;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

}
