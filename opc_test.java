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


public class opc_test extends RoboticsAPIApplication {
    @Inject private LBR robot; 
    @Inject private MediaFlangeIOGroup mF;
    @Inject @Named("RobotiqGripper") private Tool gripper;
    @Inject private ITaskLogger logger;	
//    @Inject private OPCUA_Client_Control1 OPCUA;
	@Inject 
	private Gripper2F gripper2F1;	//Access to Gripper info

    CartesianImpedanceControlMode springRobot;
    IMotionContainer m1;

    @Override
    public void initialize() {
        springRobot = new CartesianImpedanceControlMode(); 
        springRobot.parametrize(CartDOF.X).setStiffness(500);
        springRobot.parametrize(CartDOF.Y).setStiffness(1000);
        springRobot.parametrize(CartDOF.Z).setStiffness(200);
        springRobot.parametrize(CartDOF.C).setStiffness(50);
        springRobot.parametrize(CartDOF.B).setStiffness(50);
        springRobot.parametrize(CartDOF.A).setStiffness(300);
        springRobot.setReferenceSystem(World.Current.getRootFrame());
        springRobot.parametrize(CartDOF.ALL).setDamping(0.4);

        logger.info("Initializing Automatic Mode");
//        try {
//            OPCUA.SetUp();
//            OPCUA.ServerUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        gripper.attachTo(robot.getFlange());
        mF.setLEDBlue(true);
        ThreadUtil.milliSleep(500);
        mF.setLEDBlue(false);
		mF.setLEDBlue(false);
		logger.info("Initalising Gripper...");
		gripper2F1.initalise();
		gripper2F1.setSpeed(255);
		ThreadUtil.milliSleep(100);
		mF.setLEDBlue(true);
//        try {
//            OPCUA.setEnd(false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void run() throws Exception {
    	while (true){
	    	logger.info("opening gripper");
	    	gripper2F1.open();
	    	ThreadUtil.milliSleep(2000);
	    	logger.info("closing gripper");
	    	gripper2F1.close();
	    	ThreadUtil.milliSleep(2000);
    	}
//        OPCUA.ServerUpdate();
//        logger.info("Entering Main Program");
//
//        while (OPCUA.Connected) {
//            OPCUA.ServerUpdate();
//            OPCUA.setReady(true);
//
//            // ðŸ”¹ Publish current joint positions every cycle
//            double[] joints = robot.getCurrentJointPosition().getInternalArray();
////            OPCUA.setJointPositions(joints);
//
//            if (!OPCUA.Connected) break;
//
//            if (OPCUA.Start) {
////                OPCUA.setReady(true);
////                OPCUA.setEnd(false);
//
//                switch (OPCUA.ProgID) {
//                    case 0:
//                        logger.error("Program 0 Started");
//                        break;
//
//                    case 1: // Demo
//                        logger.info("Program 1 Started");
//                        logger.info("Program 1 Complete");
//                        break;
//
//                    case 2: // Joint PTP from OPC UA commands
//                        logger.info("Program 2 Started");
//                        OPCUA.ServerUpdate();
//                        if (!OPCUA.Connected) break;
//
//                        gripper.move(ptp(
//                            Math.toRadians(OPCUA.Joi1),
//                            Math.toRadians(OPCUA.Joi2),
//                            Math.toRadians(OPCUA.Joi3),
//                            Math.toRadians(OPCUA.Joi4),
//                            Math.toRadians(OPCUA.Joi5),
//                            Math.toRadians(OPCUA.Joi6),
//                            Math.toRadians(OPCUA.Joi7)
//                        ).setJointVelocityRel(OPCUA.Vel));
//
//                        logger.info("Program 2 Complete");
//                        break;
////                        
////                    case 3:  // Moving to end effector absolute position 
////                        logger.info("Program 3 Started");
////                        OPCUA.ServerUpdate();
////                        if (!OPCUA.Connected) break;
////
////                        try {
////                            // Build absolute target frame relative to the base (World)
////                            Frame target = new Frame(
////                                OPCUA.PosX,   // X in mm
////                                OPCUA.PosY,   // Y in mm
////                                OPCUA.PosZ,   // Z in mm
////                                Math.toRadians(OPCUA.RotA),   // Orientation A in degrees
////                        		Math.toRadians(OPCUA.RotB),   // Orientation B in degrees
////                				Math.toRadians(OPCUA.RotC)    // Orientation C in degrees
////                            );
////                            target.setParent(World.Current.getRootFrame());
////
////                            // Move the robot absolutely to the target
////                            gripper.move(ptp(target).setBlendingCart(OPCUA.Vel));
////
////                            logger.info("Moved to target: X=" + OPCUA.PosX +
////                                        " Y=" + OPCUA.PosY +
////                                        " Z=" + OPCUA.PosZ +
////                                        " A=" + OPCUA.RotA +
////                                        " B=" + OPCUA.RotB +
////                                        " C=" + OPCUA.RotC);
////
////                          
////                        } catch (Exception e) {
////                            logger.error("Case 3 failed: " + e.getMessage());
////                        }
////
////                        logger.info("Program 3 Complete");
////                        break;
//
//                    case 5: // Move to calibration frame P23
//                        logger.info("Program 5: Moving to calibration frame P23");
//                        OPCUA.setReady(false);
//
//                        try {
//                            // Get P23 frame from Sunrise project
//                            ObjectFrame target = getApplicationData().getFrame("/P23");
//
//                            // Move robot to P23 with 20% joint speed
//                            gripper.move(ptp(target).setJointVelocityRel(0.2));
//
//                            logger.info("Successfully moved to calibration frame P23");
//
//                        } catch (Exception e) {
//                            logger.error("Case 5 failed: " + e.getMessage());
//                        }
//
//                        logger.info("Program 5 Complete");
//                        break;
//                }
//
//                // Program complete routine
//                if (!OPCUA.Connected) break;
//                OPCUA.setEnd(true);
////                OPCUA.setProgID(0);
//                OPCUA.setStart(false);
//                ThreadUtil.milliSleep(1500);
//                OPCUA.setReady(true);
//                ThreadUtil.milliSleep(1500);
//                OPCUA.setEnd(false);
//                logger.info("Communication Signals Reset");
//            }
//        }
//
//        if (!OPCUA.Connected) {
//            mF.setLEDBlue(false);
//            logger.info("Shutting Down Automatic Mode");
//            OPCUA.clientDisconnect();
//        }
//    }
//}

    }
}
