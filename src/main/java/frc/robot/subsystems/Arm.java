// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import frc.robot.Constants.ArmConstants;
import frc.robot.util.SimEncoder;

public class Arm extends SubsystemBase {
  /** Creates a new Arm. */
  private static CANSparkMax armMotorController;
  public static RelativeEncoder armEncoder;
  private SimEncoder armEncoderSim;
  private SingleJointedArmSim armSim;
  private static double current_pos = 0;
  private static double current_vel = 0;
  public final DoublePublisher armPositionPublisher;
  public final DoublePublisher armVelocityPublisher;

  public Arm() {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    // get the subtable called "datatable"
    NetworkTable datatable = inst.getTable("datatable");
    armMotorController = new CANSparkMax(ArmConstants.ARM_MOTOR_ID, MotorType.kBrushless);
    armMotorController.restoreFactoryDefaults();
    armEncoder = armMotorController.getEncoder();
    armMotorController.setIdleMode(CANSparkMax.IdleMode.kBrake);
    armMotorController.setInverted(false);
    armEncoder.setPosition(0);
    armPositionPublisher = datatable.getDoubleTopic("elevator Pos").publish();
    armVelocityPublisher = datatable.getDoubleTopic("elevator Vel").publish();
    if(RobotBase.isSimulation()) {
      armEncoderSim = new SimEncoder("Elevator");
      armSim = new SingleJointedArmSim(
        DCMotor.getNEO(1), //1 NEO motor on the climber
        10,
        SingleJointedArmSim.estimateMOI(ArmConstants.ARM_LENGTH, ArmConstants.ARM_MASS), 
        ArmConstants.ARM_LENGTH,
        0,
        ArmConstants.MAX_ARM_ANGLE,
        ArmConstants.ARM_MASS,
        true
      ); 
    }
  }

  @Override
  public void simulationPeriodic() {

    current_pos = armEncoder.getPosition();
    current_vel = armEncoder.getVelocity();
    armPositionPublisher.set(current_pos);
    armVelocityPublisher.set(current_vel);

    // sets input for elevator motor in simulation
    armSim.setInput(armMotorController.get() * RobotController.getBatteryVoltage());
    // Next, we update it. The standard loop time is 20ms.
    armSim.update(0.02);
    // Finally, we set our simulated encoder's readings
    armEncoderSim.setDistance(armSim.getAngleRads());
    // sets our simulated encoder speeds
    armEncoderSim.setSpeed(armSim.getVelocityRadPerSec());

    // SimBattery estimates loaded battery voltages
    RoboRioSim.setVInVoltage(BatterySim.calculateDefaultBatteryLoadedVoltage(armSim.getCurrentDrawAmps()));

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
  
  public void setSpeed(double speed) {
    armMotorController.set(speed);
    SmartDashboard.putNumber("ArmSpeed", speed);
  }
}
