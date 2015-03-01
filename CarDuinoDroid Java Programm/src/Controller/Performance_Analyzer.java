package Controller;

public class Performance_Analyzer {

	private Controller_Computer _controller_computer;

	public Performance_Analyzer(Controller_Computer controllerComputer) {
		_controller_computer = controllerComputer;
	}

	public long syncGlobalTime() {	
		long globalDiff = christiansAlgorithm(10);
		
		_controller_computer.log.writelogfile("timediff: " + String.valueOf(globalDiff));
		return globalDiff;
	}
	
	private long christiansAlgorithm(int iterations){
		long min = Long.MAX_VALUE;
		long serverTimeStamp2 = -1;
		long ownTimeStamp = 0;
		
		for(int i = 0; i<iterations; ++i){
			ownTimeStamp = System.currentTimeMillis();
		
			send_requestTimeStamp();
			
			while (serverTimeStamp2 == -1) {				
				serverTimeStamp2 = receive_requestedTimeStamp();
				if (System.currentTimeMillis() - ownTimeStamp > 500) {
					_controller_computer.log.writelogfile("Fail");
					break;
				}
			};
			
			long ownTimeStamp2 = System.currentTimeMillis();
		
			long rtt = ownTimeStamp2 - ownTimeStamp;
			long delta = rtt/2;
			long localMin = ownTimeStamp2 - (serverTimeStamp2 + delta);
			_controller_computer.log.writelogfile("rtt: " + String.valueOf(rtt));
			if(localMin < min){
				min = localMin;
			}
			
			serverTimeStamp2 = -1;
			
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}	
		return min;
	}

	private long receive_requestedTimeStamp() {
		long timeStamp = _controller_computer.network.receive_requestedTimeStamp();
		if (timeStamp != -1){
			_controller_computer.log.writelogfile("Timestamp received");
			return timeStamp;
		} else {
			_controller_computer.log.writelogfile("Could not receive Timestamp");
			return -1;
		}
	}

	private void send_requestTimeStamp() {
		if (_controller_computer.network.send_requestTimeStamp("1"))
		{_controller_computer.log.writelogfile("Timestamp requested");}
		else{_controller_computer.log.writelogfile("Could not request Timestamp");}
	}
	
}
