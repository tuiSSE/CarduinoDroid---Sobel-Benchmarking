package View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import Model.Log;

/** Methods for working with connect menu item.
*
* @author Benjamin L
* @version 11.06.2012.
*/
public class Connect_ActionListener implements ActionListener{
	
	GUI_Computer gui_computer;
	String Question;
	String Ip_adress;
	Log log;
	JLabel ip;
	
	String number_between_0_and_255 = "(([0-9]{1,2})|([01][0-9]{2})|(2((5[0-5])|([0-4][0-9]))))";
	String valid_ipv4 = "(" + number_between_0_and_255 + "\\.){3}" + number_between_0_and_255;
	
	/** 
	 * @param QUESTION		Question text by opening the connect window. 
	 * @param LOG			Log which is used.
	 * @param IP			Label which should show the actual connected IP.
	 * @param gui_Computer	The GUI which calls this constructor. 
	 */
	public Connect_ActionListener(String QUESTION, Log LOG, JLabel IP, GUI_Computer gui_Computer){
		Question = QUESTION;
		log = LOG;
		ip = IP;
		gui_computer = gui_Computer;
	}

	/** Opens a new window to enter the ip of the phone when pressing the connect menu item.
	 * 
	 * @param e			Event by pressing the connect menu item.
	 */
	public void actionPerformed(ActionEvent e) {
		//Auslesen des Textfeldes und prüfen ob leer, danach schauen ob Mindestlaenge
		if((Ip_adress = JOptionPane.showInputDialog(Question)) != null){
			if (Ip_adress.matches(valid_ipv4)){
				ip.setText(Ip_adress);
				//System.out.println(Ip_adress);
				log.writelogfile("Connect to: " + Ip_adress);
				gui_computer.controller_Computer.network.connect(Ip_adress);
			}
		}
	}
}
