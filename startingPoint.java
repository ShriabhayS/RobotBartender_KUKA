package application;

import javax.inject.Inject;
import com.kuka.task.ITaskLogger;
import javax.inject.Named;
import com.kuka.common.ThreadUtil;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

public class startingPoint extends RoboticsAPIApplication {
    private LBR lbr;

    // Gripper
    @Inject
    private Gripper_test gripper;
    
    // OPCUA
    @Inject
	private OPCUA_Client_Control1 OPCUA;
    
    // TCP
	@Inject
	@Named("Bartender")
	private Tool Bartender;
    
    // Logger
    @Inject
	private ITaskLogger logger;
    
    private float currentWidth = -1;
    private float currentForce = -1;
    
    @Override
    public void initialize() {
    	// ARM
        lbr = getContext().getDeviceFromType(LBR.class);
        
        Bartender.attachTo(lbr.getFlange());
        // GRIPPER
        float[] initialisationResults = gripper.initialise();
		currentWidth = initialisationResults[0];
	    currentForce = initialisationResults[1];
	    gripper.setOffset(3);
        gripper.close();
        
        // OPCUA
        try {
			OPCUA.SetUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			OPCUA.ServerUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void run() {
    	try{
    	logger.info("Entering Main Program");
    	OPCUA.ServerUpdate();
    	
    	while(OPCUA.Connected){
    		OPCUA.ServerUpdate();
    		OPCUA.setReady(true);
    		if (OPCUA.Connected==false){break;}
    		if(OPCUA.Start==true){
    			// Switch statements here should grab drink and return to a common point
    			switch(OPCUA.int1){
				case 0:
					logger.error("Program 0 Started");
					break;
				case 1: // Drink A
					OPCUA.setReady(false);
					logger.info("Getting Drink A Started");
					
					gripper.open();
					
					getLogger().info("Moving to DrinkA...");
			        Bartender.move(ptp(getApplicationData().getFrame("/DrinkA")).setJointVelocityRel(0.2));
			        
			        
			        getLogger().info("Moving to P1...");
			        Bartender.move(ptp(getApplicationData().getFrame("/DrinkA/P1")).setJointVelocityRel(0.2));
			        
			        gripper.readOffset();
			        gripper.moveTo(575);
			        gripper.readWidth();
			        
			
			        getLogger().info("Moving to P2...");
			        Bartender.move(ptp(getApplicationData().getFrame("/DrinkA/P2")).setJointVelocityRel(0.2));
			        
//			        getLogger().info("Moving to P3...");
//			        Bartender.move(ptp().setJointVelocityRel(0.2));
			        
			        getLogger().info("Moving to P4...");
			        Bartender.move(ptp(getApplicationData().getFrame("/DrinkA/P4")).setJointVelocityRel(0.2));
			        
			        getLogger().info("Moving to P5...");
			        Bartender.move(ptp(getApplicationData().getFrame("/DrinkA/P5")).setJointVelocityRel(0.2));
			        
			        gripper.moveTo(700);
					
					break;
				case 2:	// Drink B
					OPCUA.setReady(false);
					logger.info("Getting Drink B Started");
					
					break;
				case 3: // Drink C
					OPCUA.setReady(false);
					logger.info("Getting Drink C Started");
					// Move to frames for D
					break;
				case 4: // Drink D
					OPCUA.setReady(false);
					logger.info("Getting Drink D Started");
					// Move to frames for D
					break;
				case 5: // Random?
					OPCUA.setReady(false);
//					gripper2F1.close();
//					gripper.move(ptp(0.0,0.785398,0.0,-1.13446,0.0,-0.436332,1.5708).setJointVelocityRel(0.2));//.setMode(springRobot));
//					m1 = gripper.moveAsync(positionHold(springRobot, -1, TimeUnit.SECONDS));
					while (OPCUA.Start == true){ // when does OPCUA.Start == false??
						OPCUA.ServerUpdate();
					}
//					m1.cancel();
					break;
				case 6: // OPCUA defined movement
					OPCUA.ServerUpdate();
					if (OPCUA.Connected==false){break;}
//					try{
					gripper.moveTo(500);
//					} catch (Exception e) {
//					}
					logger.info("Program 2 Complete");
				default:
					break;
				} 
    			
    			// Bottle opening and handing over code
    			
    			if (OPCUA.Connected==false){break;} 
				OPCUA.setEnd(true);
				OPCUA.setProgID(0);
				OPCUA.setStart(false);
				ThreadUtil.milliSleep(1500);
				OPCUA.setReady(true);
				ThreadUtil.milliSleep(1500);
//				mF.setLEDGreen(false);
				OPCUA.setEnd(false);
				logger.info("Communication Signals Reset");
    			
    		}
    		
    		
//	        // Frames under DrinkA
//	        ObjectFrame drinkA = getApplicationData().getFrame("/DrinkA");
//	        ObjectFrame p1 = getApplicationData().getFrame("/DrinkA/P1");
//	        ObjectFrame p2 = getApplicationData().getFrame("/DrinkA/P2");
//	        ObjectFrame p3 = getApplicationData().getFrame("/DrinkA/P3");
//	        ObjectFrame p4 = getApplicationData().getFrame("/DrinkA/P4");
//	        ObjectFrame p5 = getApplicationData().getFrame("/DrinkA/P5");
//	
//	        // Move sequence
//	        
	    	} 
    	
	    } catch (Exception e) {
				e.printStackTrace();
    	}
    }
}
