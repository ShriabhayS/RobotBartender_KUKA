package application;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;

public class TestGripperInt extends RoboticsAPIApplication {
    private RG2Modbus gripper;

    @Override
    public void initialize() {
        // Connect to gripper on startup
        gripper = new RG2Modbus("160.69.69.120", 502);
        try {
            gripper.connect();
            getLogger().info("Connected to RG2 gripper.");
        } catch (Exception e) {
            getLogger().error("Gripper connection failed: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            int choice = getApplicationUI().displayModalDialog(
                ApplicationDialogType.QUESTION,
                "Open or Close Gripper?",
                "Open", "Close"
            );
            if (choice == 0) {
                gripper.openGripper();
                getLogger().info("Sent OPEN");
            } else {
                gripper.closeGripper();
                getLogger().info("Sent CLOSE");
            }
        } catch (Exception e) {
            getLogger().error("Gripper command failed: " + e.getMessage());
        }
    }
}
