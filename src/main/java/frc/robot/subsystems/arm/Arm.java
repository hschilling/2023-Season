// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.arm;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.RobotContainer;
import frc.robot.Constants.ArmConstants;
import frc.robot.util.PIDUtil;

public class Arm extends ProfiledPIDSubsystem {
  /** Creates a new Arm. */
  private ArmIO armIO;
  private double targetAngle = - Math.PI/2; 
  private static final double feedForward = 0.133;

  private static final double kpPos = 0.8;

  // Trapezoidal profile constants and variables
  private static final double max_vel = 1.5;  // rad/s
  private static final double max_accel = 2.7;  // rad/s/s
  private static final Constraints constraints = new Constraints(max_vel, max_accel);
  private static double gravityCompensation = 0.04;

  public Arm(ArmIO io) {
    super(new ProfiledPIDController(kpPos, 0, 0, constraints));
    armIO = io;
  }

  @Override
  public void periodic() {
    // Call periodic method in profile pid subsystem to prevent overriding
    super.periodic();
    armIO.periodicUpdate();

    SmartDashboard.putNumber("arm goal position", getGoal());
    SmartDashboard.putNumber("arm velocity", getEncoderSpeed()); 
    SmartDashboard.putNumber("arm postion", getEncoderPosition()); 
    RobotContainer.armMechanism.setAngle(Units.radiansToDegrees(getEncoderPosition()) - 50);
  
  }
  
  public double getEncoderPosition() {
    return armIO.getEncoderPosition();
  }

  public double getEncoderSpeed() {
    return armIO.getEncoderSpeed();
  }

  public void setSpeed(double speed) {
    armIO.setSpeed(speed);
    SmartDashboard.putNumber("arm speed", speed);
    System.out.println("arm speed" + speed);
  }

  public double getTargetAngle() {
    return targetAngle; 
  }

  public void setTargetAngle(double angle) {
    System.out.println("Set target to " + angle);
    targetAngle = angle; 
  }

  public void setSpeedGravityCompensation(double speed) {
    //armIO.setSpeed(speed + gravityCompensation * Math.cos(getEncoderPosition()));
    armIO.setSpeed(speed + gravityCompensation);
  }

  public double getArmCurrent() {
    return armIO.getArmCurrent();
  }

  @Override
  protected void useOutput(double output, State setpoint) {
    SmartDashboard.putNumber("arm setpoint pos", setpoint.position);
    SmartDashboard.putNumber("arm setpoint vel", setpoint.velocity);

    // Calculate the feedforward from the setpoint
    double speed = feedForward * setpoint.velocity;
    //accounts for gravity in speed
    speed += gravityCompensation * Math.cos(getEncoderPosition()); 
    // Add PID output to speed to account for error in arm
    speed += output;
    armIO.setSpeed(speed);
  }

  @Override
  protected double getMeasurement() {
    return armIO.getEncoderPosition();
  }

  public double getGoal() {
    return m_controller.getGoal().position;
  }

    // Checks to see if arm is within range of the setpoints
    public boolean atGoal() {
      return (PIDUtil.checkWithinRange(getGoal(), getMeasurement(), ArmConstants.ANGLE_TOLERANCE));
    }
  
  public void setPosition(double position) {
    armIO.setPosition(position);
  }
}
