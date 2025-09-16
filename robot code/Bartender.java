package application;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.World;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.sensorModel.ForceSensorData;
import com.kuka.task.ITaskLogger;
import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator.IgnoredChecks;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.math.Vector;


public class Bartender extends RoboticsAPIApplication {
    @Inject private LBR robot; 
    @Inject private MediaFlangeIOGroup mF;
    @Inject @Named("RobotiqGripper") private Tool gripper_tool;
    @Inject private ITaskLogger logger;	
    @Inject private OPCUA_Client_Control1 OPCUA;
	@Inject 
	private Gripper2F gripper;	//Access to Gripper info

    CartesianImpedanceControlMode springRobot;
    IMotionContainer m1;

    @Override
    public void initialize() {
//        springRobot = new CartesianImpedanceControlMode(); 
//        springRobot.parametrize(CartDOF.X).setStiffness(500);
//        springRobot.parametrize(CartDOF.Y).setStiffness(1000);
//        springRobot.parametrize(CartDOF.Z).setStiffness(200);
//        springRobot.parametrize(CartDOF.C).setStiffness(50);
//        springRobot.parametrize(CartDOF.B).setStiffness(50);
//        springRobot.parametrize(CartDOF.A).setStiffness(300);
//        springRobot.setReferenceSystem(World.Current.getRootFrame());
//        springRobot.parametrize(CartDOF.ALL).setDamping(0.4);

        logger.info("Initializing Automatic Mode");
        try {
            OPCUA.SetUp();
            OPCUA.ServerUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        gripper_tool.attachTo(robot.getFlange());
        mF.setLEDBlue(true);
        ThreadUtil.milliSleep(500);
        mF.setLEDBlue(false);
		mF.setLEDBlue(false);
		logger.info("Initalising Gripper...");
		gripper.initalise();
		gripper.setSpeed(255);
		gripper.setForce(255); // Some ratio between 0 and 255, 0 - 200N?
		ThreadUtil.milliSleep(100);
		mF.setLEDBlue(true);
		
        try {
            OPCUA.setEnd(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() throws Exception {
//    	while (true){
//	    	
//    	}
        OPCUA.ServerUpdate();
        logger.info("Entering Main Program");

        while (OPCUA.Connected) {
            OPCUA.ServerUpdate();
            OPCUA.setReady(true);

            // 🔹 Publish current joint positions every cycle
            double[] joints = robot.getCurrentJointPosition().getInternalArray();
//            OPCUA.setJointPositions(joints);

            if (!OPCUA.Connected) break;

            if (OPCUA.Start) {
                OPCUA.setReady(false);
//                OPCUA.setEnd(false);
            	
            	// TODO: Code to move gripper to the bottle holder

                switch (OPCUA.int1) {
                // TODO: Switch statements for each bottle, code here should just be to move to the relevant frame and grab bottle. Random legacy code below, can be changed
                    case 0:
                        logger.error("Program 0 Started");
                        break;

                    case 1: // Demo
                        logger.info("Program 1 Started");
                        logger.info("Bundaberg");
                        logger.info("Program 1 Complete");
                        break;

                    case 2: // Joint PTP from OPC UA commands
                        logger.info("Program 2 Started");
                        logger.info("Schweppes");
                        OPCUA.ServerUpdate();
                        if (!OPCUA.Connected) break;

//                        gripper_tool.move(ptp(
//                            Math.toRadians(OPCUA.Joi1),
//                            Math.toRadians(OPCUA.Joi2),
//                            Math.toRadians(OPCUA.Joi3),
//                            Math.toRadians(OPCUA.Joi4),
//                            Math.toRadians(OPCUA.Joi5),
//                            Math.toRadians(OPCUA.Joi6),
//                            Math.toRadians(OPCUA.Joi7)
//                        ).setJointVelocityRel(OPCUA.Vel));

                        logger.info("Program 2 Complete");
                        break;
                        
                    case 3:  // Moving to end effector absolute position 
                        logger.info("Program 3 Started");
                        logger.info("Coca-cola");
                        OPCUA.ServerUpdate();
                        if (!OPCUA.Connected) break;

                        try {
                            // Build absolute target frame relative to the base (World)
                            gripper.open();
                            ThreadUtil.milliSleep(2000);
                            gripper.close();

                          
                        } catch (Exception e) {
                            logger.error("Case 3 failed: " + e.getMessage());
                        }

                        logger.info("Program 3 Complete");
                        break;

                    case 5: // Move to calibration frame P23
                        logger.info("Program 5: Moving to calibration frame P23");
                        logger.info("Fever-tree");
                        OPCUA.setReady(false);

                        try {
//                            // Get P23 frame from Sunrise project
//                            ObjectFrame target = getApplicationData().getFrame("/P23");
//
//                            // Move robot to P23 with 20% joint speed
//                            gripper_tool.move(ptp(target).setJointVelocityRel(0.2));

                            logger.info("Successfully moved to calibration frame P23");

                        } catch (Exception e) {
                            logger.error("Case 5 failed: " + e.getMessage());
                        }

                        logger.info("Program 5 Complete");
                        break;
                }
                
                // TODO: Here, write code to move gripper back (can be relative movement (linrel)) and to a specified point
                
                // TODO: Next step is opening the bottle at the bottle opener section
                
                // TODO: Final step, place bottle at hand-over area :)

                // Program complete routine
                if (!OPCUA.Connected) break;
                OPCUA.setEnd(true);
//                OPCUA.setProgID(0);
                OPCUA.setStart(false);
                ThreadUtil.milliSleep(1500);
                OPCUA.setReady(true);
                ThreadUtil.milliSleep(1500);
                OPCUA.setEnd(false);
                logger.info("Communication Signals Reset");
            }
        }

        if (!OPCUA.Connected) {
            mF.setLEDBlue(false);
            logger.info("Shutting Down Automatic Mode");
            OPCUA.clientDisconnect();
        }
    }

}
