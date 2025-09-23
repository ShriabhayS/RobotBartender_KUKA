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
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.roboticsAPI.geometricModel.math.Vector;


public class motiontest extends RoboticsAPIApplication {
    @Inject private LBR robot; 
    @Inject private MediaFlangeIOGroup mF;
    @Inject @Named("RobotiqGripper") private Tool gripper_tool;
    @Inject private ITaskLogger logger;	
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

        logger.info("Initializing Motion Test Mode");
//        try {
//            OPCUA.SetUp();
//            OPCUA.ServerUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        gripper_tool.attachTo(robot.getFlange());
        mF.setLEDBlue(true);
        ThreadUtil.milliSleep(500);
//        mF.setLEDBlue(false);
		mF.setLEDBlue(false);
		logger.info("Initialising Gripper...");
		try {
		gripper.initalise();
		gripper.setSpeed(255);
		gripper.setForce(255); // Some ratio between 0 and 255, 0 - 200N?
		} catch (Exception e) {
			logger.error("Initialise error: " + e.getMessage());
		}
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
//    	while (true){
//	    	
//    	}
//        OPCUA.ServerUpdate();
        logger.info("Entering Main Program");



        // Publish current joint positions every cycle
//        double[] joints = robot.getCurrentJointPosition().getInternalArray();
//            OPCUA.setJointPositions(joints);

        try {
//            logger.info("Calibration frame");
//            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration")).setJointVelocityRel(0.25));
//            ThreadUtil.milliSleep(3000);
            logger.info("Going start frame");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Start")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Closing gripper");
            gripper.close();
            ThreadUtil.milliSleep(2000);
            logger.info("Pre_approach_1");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Pre_approach_1")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Pre_approach_2");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Pre_approach_2")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Approaching");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Approach")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Opening bottle");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Open")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Exiting");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Exit")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Back to start");
            gripper_tool.move(ptp(getApplicationData().getFrame("/Opener_calibration/Start")).setJointVelocityRel(0.15));
            ThreadUtil.milliSleep(2000);
            logger.info("Opening gripper");
            gripper.open();
            
//            gripper_tool.move(ptp().setJointVelocityRel(0.25));
//            gripper.open();
//            ThreadUtil.milliSleep(1000);
//            gripper_tool.getFrame("/Home").move(linRel(0, 0, -100));
//            logger.info("Gripper_tool lin rel relative to Home");
//            ThreadUtil.milliSleep(5000);
//            robot.move(linRel(0, 0, 100 , getApplicationData().getFrame("/Home")));
//            logger.info("Robot lin rel relative to home");
//            gripper.close();
//            logger.info("Moving back to approach frame. Check inconsistency");
//            ThreadUtil.milliSleep(2000);
//            gripper_tool.move(ptp().setJointVelocityRel(0.25));
//            ThreadUtil.milliSleep(2000);
            
//            gripper_tool.move(ptp());
            
        } catch (Exception e) {
            logger.error("Program 1 pick failed: " + e.getMessage());
        }
        logger.info("Program 1 Complete");
                        
                
//                // TODO: Here, write code to move gripper back (can be relative movement (linrel)) and to a specified point
//                try {
//                    gripper_tool.move(linRel(0, 0, 80, 0, 0, 0).setCartVelocity(0.06));
//                } catch (Exception e) {
//                    logger.warn("Relative retract skipped: " + e.getMessage());
//                }
//                try {
//                    ObjectFrame home = getApplicationData().getFrame("/Home");
//                    gripper_tool.move(ptp(home).setJointVelocityRel(0.25));
//                } catch (Exception e) {
//                    logger.warn("Home move skipped: " + e.getMessage());
//                }
//                // TODO: Next step is opening the bottle at the bottle opener section
//                try {
//                    ObjectFrame opApproach = getApplicationData().getFrame("/Opener/Approach");
//                    ObjectFrame opWork= getApplicationData().getFrame("/Opener/Work");
//                    ObjectFrame opExit= getApplicationData().getFrame("/Opener/Exit");
//
//                    gripper_tool.move(ptp(opApproach).setJointVelocityRel(0.25));
//                    gripper_tool.move(lin(opWork).setCartVelocity(0.15));
//                    ThreadUtil.milliSleep(500);
//                    gripper_tool.move(lin(opExit).setCartVelocity(0.15));
//                } catch (Exception e) {
//                    logger.warn("Bottle opener step skipped: " + e.getMessage());
//                }
//                // TODO: Final step, place bottle at hand-over area :)
//                try {
//                    ObjectFrame hApproach = getApplicationData().getFrame("/HandOver/Approach");
//                    ObjectFrame hPlace = getApplicationData().getFrame("/HandOver/Place");
//                    ObjectFrame hExit= getApplicationData().getFrame("/HandOver/Exit");
//
//                    gripper_tool.move(ptp(hApproach).setJointVelocityRel(0.25));
//                    gripper_tool.move(lin(hPlace).setCartVelocity(0.15));
//                    gripper.open();
//                    ThreadUtil.milliSleep(150);
//                    gripper_tool.move(lin(hExit).setCartVelocity(0.15));
//                    gripper.close();
//                } catch (Exception e) {
//                    logger.warn("Hand-over step skipped: " + e.getMessage());
//                }

    }
}
