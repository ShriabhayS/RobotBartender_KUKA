# KUKA LBR iiwa + RG2 Gripper Test

## Files
- `application/Rg2Modbus.java` → Minimal Modbus-TCP driver for RG2.
- `application/TestGripperInt.java` → Sunrise app to connect + open/close gripper.

## Setup
1. Connect RG2 gripper to Ethernet (same subnet as robot).
   - Example IP: `160.69.69.120`
   - Port: `502` (Modbus TCP)
2. Put both files in the same Sunrise Workbench project (`/src/application/`).
3. Build & deploy to the robot controller.

## Run
- On the SmartPad, select **TestGripperInt**.
- When prompted:  
  - Press **Open** → gripper opens  
  - Press **Close** → gripper closes  

## Notes
- Edit IP/port in `TestGripperInt.java` if uses a different config.  
- if it doesnt work the manual has to be checked for the certain commands the gripper uses
- Look up the OnRobot RG2 Modbus TCP manual 
Find the holding registers table.
Note the exact addresses for:
- Width / position
- Force
- Speed
- Command register
- Status register
edit and update in RG2Modbus.java

