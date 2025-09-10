package application;

import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.task.ITaskLogger;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

public class startingPointRelative extends RoboticsAPIApplication {

    private LBR lbr;

    @Inject @Named("Bartender")
    private Tool Bartender;

    @Inject
    private Gripper_test gripper;

    @Inject
    private OPCUA_Client_Control1 OPCUA;

    @Inject
    private ITaskLogger logger;

    private float currentWidth = -1;
    private float currentForce = -1;

    // --- Offsets for DrinkB, C, D relative to DrinkA ---
    private static class Pose6D {
        final double x,y,z,aDeg,bDeg,cDeg;
        Pose6D(double x,double y,double z,double a,double b,double c){
            this.x=x; this.y=y; this.z=z;
            this.aDeg=a; this.bDeg=b; this.cDeg=c;
        }
    }

    private final Pose6D OFF_A = new Pose6D(0,0,0,0,0,0);          // DrinkA (reference, taught)
    private final Pose6D OFF_B = new Pose6D(250,-180,0,0,0,0);     // DrinkB offset
    private final Pose6D OFF_C = new Pose6D(-220,140,0,0,0,0);     // DrinkC offset
    private final Pose6D OFF_D = new Pose6D(380,0,0,0,0,0);        // DrinkD offset

    private final boolean CALIBRATE_AT_EDGE = true;  // whether to perform simple calibration
    private final double HOVER_Z = 80.0;             // hover clearance in mm

    // Cached taught frames under DrinkA
    private ObjectFrame fA_root, fA_P1, fA_P2, fA_P4, fA_P5;

    @Override
    public void initialize() {
        // Arm setup
        lbr = getContext().getDeviceFromType(LBR.class);
        Bartender.attachTo(lbr.getFlange());

        // Gripper initialization
        float[] init = gripper.initialise();
        currentWidth = init[0];
        currentForce = init[1];
        gripper.setOffset(3);
        gripper.close();

        // Cache taught frames for DrinkA
        fA_root = getApplicationData().getFrame("/DrinkA");
        fA_P1   = getApplicationData().getFrame("/DrinkA/P1");
        fA_P2   = getApplicationData().getFrame("/DrinkA/P2");
        fA_P4   = getApplicationData().getFrame("/DrinkA/P4");
        fA_P5   = getApplicationData().getFrame("/DrinkA/P5");

        // OPC UA setup
        try {
            OPCUA.SetUp();
            OPCUA.ServerUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            logger.info("Entering Main Program");
            OPCUA.ServerUpdate();

            while (OPCUA.Connected) {
                OPCUA.ServerUpdate();
                OPCUA.setReady(true);
                if (!OPCUA.Connected) break;

                if (OPCUA.Start) {
                    switch (OPCUA.int1) {
                        case 1: runSequenceForDrink(OFF_A,'A'); break;
                        case 2: runSequenceForDrink(OFF_B,'B'); break;
                        case 3: runSequenceForDrink(OFF_C,'C'); break;
                        case 4: runSequenceForDrink(OFF_D,'D'); break;
                        default: logger.warn("Unhandled program ID: " + OPCUA.int1); break;
                    }

                    if (!OPCUA.Connected) break;

                    // Reset signals
                    OPCUA.setEnd(true);
                    OPCUA.setProgID(0);
                    OPCUA.setStart(false);
                    ThreadUtil.milliSleep(1500);
                    OPCUA.setReady(true);
                    ThreadUtil.milliSleep(1500);
                    OPCUA.setEnd(false);
                    logger.info("Communication Signals Reset");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helper methods for frame construction ---

    // Build a parent frame by applying offset to /DrinkA
    private Frame makeParentFromA(Pose6D off){
        Frame parent = fA_root.copyWithRedundancy();
        parent.transform(Transformation.ofDeg(off.aDeg, off.bDeg, off.cDeg, off.x, off.y, off.z));
        return parent;
    }

    // Local transform of a child relative to /DrinkA
    private Transformation localFromA(ObjectFrame child){
        return child.getTransformationFromParent();
    }

    // Build a child frame under the given parent using DrinkA’s subframe geometry
    private Frame childUnderParent(Frame parent,ObjectFrame A_child){
        Frame f = parent.copyWithRedundancy();
        f.transform(localFromA(A_child));
        return f;
    }

    // Simple hover offset above any frame
    private Frame hoverAbove(Frame base,double dz){
        Frame f = base.copyWithRedundancy();
        f.transform(Transformation.ofDeg(0,0,0,0,0,dz));
        return f;
    }

    // Simple calibration: operator jogs tip to touch holder edge, then I capture TCP as new parent
    private Frame calibrateParentByTouch(Frame approxParent,char drinkLetter) throws Exception {
        Frame hover = hoverAbove(approxParent,HOVER_Z);
        Bartender.move(ptp(hover).setJointVelocityRel(0.15));
        Bartender.move(ptp(approxParent).setJointVelocityRel(0.1));

        logger.info("[Calib " + drinkLetter + "] Jog tip to holder edge, waiting...");
        ThreadUtil.milliSleep(1500); // placeholder for operator confirmation

        Frame calibrated = Bartender.getDefaultMotionFrame().copyWithRedundancy();
        logger.info("[Calib " + drinkLetter + "] Parent pose captured.");

        Frame clear = hoverAbove(calibrated,HOVER_Z*0.5);
        Bartender.move(ptp(clear).setJointVelocityRel(0.15));
        return calibrated;
    }

    // --- Main sequence for picking a drink ---
    private void runSequenceForDrink(Pose6D offset,char drinkLetter) throws Exception {
        OPCUA.setReady(false);
        logger.info("Getting Drink " + drinkLetter + " Started");

        Frame parent = makeParentFromA(offset);
        if (CALIBRATE_AT_EDGE) {
            parent = calibrateParentByTouch(parent,drinkLetter);
        }

        // Approach sequence (reuse DrinkA’s subframes)
        gripper.open();
        Bartender.move(ptp(hoverAbove(parent,HOVER_Z)).setJointVelocityRel(0.2));

        Bartender.move(ptp(childUnderParent(parent,fA_P1)).setJointVelocityRel(0.2));
        gripper.readOffset();
        gripper.moveTo(575);
        gripper.readWidth();

        Bartender.move(ptp(childUnderParent(parent,fA_P2)).setJointVelocityRel(0.2));
        Bartender.move(ptp(childUnderParent(parent,fA_P4)).setJointVelocityRel(0.2));
        Bartender.move(ptp(childUnderParent(parent,fA_P5)).setJointVelocityRel(0.2));

        gripper.moveTo(700); // close/grasp
    }
}
