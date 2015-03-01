package View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import Controller.Controller_Computer;

public class Statistics_CheckBox_ActionListener implements ActionListener {
	private Controller_Computer _controllerComputer;
	
	public Statistics_CheckBox_ActionListener(Controller_Computer controller_Computer) {
		_controllerComputer = controller_Computer;
	}
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		AbstractButton abstractButton = (AbstractButton) actionEvent
                .getSource();
            boolean isSelected = abstractButton.getModel().isSelected();
            if(isSelected){
            	_controllerComputer.network.receive_network_Statistics();
            }

	}

}
