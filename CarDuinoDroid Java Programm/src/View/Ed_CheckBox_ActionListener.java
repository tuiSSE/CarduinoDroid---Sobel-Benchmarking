package View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import Controller.Controller_Computer;

public class Ed_CheckBox_ActionListener implements ActionListener {
	private Controller_Computer _controllerComputer;
	
	public Ed_CheckBox_ActionListener(Controller_Computer controller_Computer) {
		_controllerComputer = controller_Computer;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
        AbstractButton abstractButton = (AbstractButton) actionEvent
                .getSource();
            boolean isSelected = abstractButton.getModel().isSelected();
            _controllerComputer.camera_settings.send_change_filter(isSelected);
	}

}
