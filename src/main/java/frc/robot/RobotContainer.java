package frc.robot;

import java.util.HashMap;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.commands.FollowPathWithEvents;
import com.pathplanner.lib.commands.PPRamseteCommand;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DutyCycle;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Constants.JoystickConstants;
import frc.robot.Constants.XboxConstants;
import frc.robot.commands.SetArmAngleCmd;
import frc.robot.commands.drivetrain.ArcadeDriveCmd;
import frc.robot.commands.intake.CollectPieceCmd;
import frc.robot.commands.intake.IntakeForGivenTime;
import frc.robot.commands.elevator.SetElevatorPositionCmd;
import frc.robot.subsystems.LED;
import frc.robot.subsystems.arm.Arm;
import frc.robot.subsystems.arm.ArmIO;
import frc.robot.subsystems.arm.RealArm;
import frc.robot.subsystems.arm.SimArm;
import frc.robot.subsystems.drivetrain.DriveIO;
import frc.robot.subsystems.drivetrain.DriveTrain;
import frc.robot.subsystems.drivetrain.RealDrive;
import frc.robot.subsystems.drivetrain.SimDrive;
import frc.robot.subsystems.elevator.Elevator;
import frc.robot.subsystems.elevator.ElevatorIO;
import frc.robot.subsystems.elevator.RealElevator;
import frc.robot.subsystems.elevator.SimElevator;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeIO;
import frc.robot.subsystems.intake.RealIntake;
import frc.robot.subsystems.intake.SimIntake;
import frc.robot.util.DriveTurnControls;
import frc.robot.util.LEDController;


/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot
 * (including subsystems, commands, and button mappings) should be declared
 * here.
 */

public class RobotContainer {

    // The robot's subsystems
    public static DriveTrain driveTrain;
    public static LED led = new LED();
    // public final static Arm arm = new Arm();
    // public static final Intake intake = new Intake();
    public static Arm arm;
    public static Intake intake;
    public static Elevator elevator;
    
    public static MechanismLigament2d elevatorMechanism;
    public static MechanismLigament2d armMechanism;

    // Joysticks
    public static final Joystick joystick = new Joystick(JoystickConstants.JOYSTICK_PORT);
    public static final Joystick xbox = new Joystick(XboxConstants.XBOX_PORT);

    private DriveTurnControls driveTurnControls = new DriveTurnControls(xbox);
    //private Command extendElevator = new SetElevatorPositionCmd(elevator, 1);
    //private Command middleElevator = new SetElevatorPositionCmd(elevator, .5);
    //private Command retractElevator = new SetElevatorPositionCmd(elevator, Constants.ElevatorConstants.MIN_ELEVATOR_HEIGHT);
    private Command setElevatorSpeedUp;
    private Command setElevatorSpeedDown;
    private Command stopElevator;
    private Command collectPiece;
    private Command dropCone;

    // private Command bigIntake = new InstantCommand(() -> intake.intakeBothArms(), intake);
    // private Command leftOnly = new InstantCommand(() -> intake.intakeLeft(), intake);
    // private Command rightOnly = new InstantCommand(() -> intake.intakeRight(), intake);

    //it broke :( owo
    private Command noSpin;
    private Command spinIn;
    private Command spitOut;  
    
    private Command moveArmUp;
    private Command moveArmDown;
    private Command armDefaultCmd;
    private Command moveArmHalfway;

    public RobotContainer() {
        DriveIO driveIO;
        ElevatorIO elevatorIO;
        ArmIO armIO;
        IntakeIO intakeIO;
        

     
        // implemented drivio interface 
        if (RobotBase.isSimulation()) {
            driveIO = new SimDrive();
            elevatorIO = new SimElevator();
            armIO = new SimArm();
            intakeIO = new SimIntake();
        } else {
            driveIO = new RealDrive();
            elevatorIO = new RealElevator();
            armIO = new RealArm();
            intakeIO = new RealIntake();
        }

        driveTrain = new DriveTrain(driveIO);
        elevator = new Elevator(elevatorIO);
        arm = new Arm(armIO);
        intake = new Intake(intakeIO);

        DriverStation.silenceJoystickConnectionWarning(true);
        // Configure the button bindings
        

        setElevatorSpeedUp = new RunCommand(() -> elevator.setSpeed(0.2), elevator);
        setElevatorSpeedDown = new RunCommand(() -> elevator.setSpeed(-0.2), elevator);
        stopElevator = new InstantCommand(() -> elevator.setSpeed(0), elevator);
        collectPiece = new CollectPieceCmd(intake);
        dropCone  = new InstantCommand(() -> intake.drop(), intake);

        noSpin = new RunCommand(() -> intake.setMotor(0), intake);
        spinIn = new RunCommand(() -> intake.setMotor(Constants.IntakeConstants.INTAKE_IN_SPEED), intake);
        spitOut = new RunCommand(() -> intake.setMotor(Constants.IntakeConstants.INTAKE_OUT_SPEED), intake);    
    
        moveArmUp = new InstantCommand(() -> {arm.setTargetAngle(Math.PI/4);});
        moveArmDown = new InstantCommand(() -> {arm.setTargetAngle(-Math.PI/4 * 3);});
        armDefaultCmd = new SetArmAngleCmd(arm);
        moveArmHalfway = new InstantCommand(() -> {arm.setTargetAngle(-Math.PI/4);});

        configureButtonBindings();

        // Configure default commands
        driveTrain.setDefaultCommand(
            new ArcadeDriveCmd(driveTrain,
                () -> -driveTurnControls.getDrive(),
                () -> driveTurnControls.getTurn()));        

        intake.setDefaultCommand(noSpin);
        elevator.setDefaultCommand(stopElevator);
        // arm.setDefaultCommand(armDefaultCmd);
        
        //Makes a mechanism (lines to show elevator and arm) in simulator
        //Team colors!
        Mechanism2d mech = new Mechanism2d(1, 1);
        MechanismRoot2d root = mech.getRoot("root", 0.3, 0);
        elevatorMechanism = root.append(new MechanismLigament2d("elevator", Constants.ElevatorConstants.MIN_ELEVATOR_HEIGHT, 50));
        elevatorMechanism.setColor(new Color8Bit(0, 204, 255));
        elevatorMechanism.setLineWeight(20);
        armMechanism = elevatorMechanism.append(new MechanismLigament2d("arm", Constants.ArmConstants.ARM_LENGTH, 140));
        armMechanism.setColor(new Color8Bit(200, 0, 216));
        SmartDashboard.putData("Mech2d", mech);

    }

    private void configureButtonBindings() {
        new JoystickButton(joystick,14).whileTrue(moveArmUp);
        new JoystickButton(joystick, 13).whileTrue(moveArmDown);
        new JoystickButton(joystick, 2).whileTrue(moveArmHalfway);
        new JoystickButton(joystick,3).whileTrue(setElevatorSpeedUp);
        new JoystickButton(joystick,4).whileTrue(setElevatorSpeedDown);
        // new JoystickButton(joystick,6).whileTrue(dropCone);
        // new JoystickButton(joystick,7).whileTrue(collectPiece);
        // new JoystickButton(joystick,8).whileTrue(spinIn);
        // new JoystickButton(joystick,9).whileTrue(spitOut);
        // new JoystickButton(joystick,11).whileTrue(new InstantCommand(() -> intake.closeRight(), intake));
        // new JoystickButton(joystick,12).whileTrue(new InstantCommand(() -> intake.openRight(), intake));
    }

    public Command getAutonomousCommand() {
        //PathPlannerTrajectory examplePath = PathPlanner.loadPath("Go Straight", new PathConstraints(1, 1));
        // This will load the file "Example Path.path" and generate it with a max
        // velocity of 4 m/s and a max acceleration of 3 m/s^2

        
        //driveTrain.field.getObject("goStraightTrajectory").setTrajectory(goStraight);

        //mirror if on red alliance
        boolean useAllianceColor = true;

        PathPlannerTrajectory twoPiecePath = PathPlanner.loadPath("Two-Cone Auton", new PathConstraints(1, 1));
        driveTrain.field.getObject("traj").setTrajectory(twoPiecePath);

        HashMap<String, Command> eventMap = new HashMap<>();
        eventMap.put("leftCommunity", new PrintCommand("Left community"));
        // eventMap.put("intake", new IntakeForGivenTime(intake, IntakeConstants.INTAKE_IN_SPEED, 2));
        
        Command eventTesting = 
        new SequentialCommandGroup(
            new InstantCommand(() -> {
                // Reset odometry for the first path you run during auto
                
                driveTrain.resetOdometry(twoPiecePath.getInitialPose());

            }, driveTrain),
        new PPRamseteCommand(
            twoPiecePath,
            () -> driveTrain.getPoseMeters(), // Pose supplier
            new RamseteController(),
            new SimpleMotorFeedforward(Constants.DriveConstants.ks,
                Constants.DriveConstants.kv,
                Constants.DriveConstants.ka),
            Constants.DriveConstants.kDriveKinematics, // DifferentialDriveKinematics
            () -> driveTrain.getWheelSpeedsMetersPerSecond(), // DifferentialDriveWheelSpeeds supplier
            new PIDController(0, 0, 0), // Left controller. Tune these values for your robot. Leaving them 0
                    // will only use feedforwards.
            new PIDController(0, 0, 0), // Right controller (usually the same values as left controller)
            (left, right) -> driveTrain.setMotorVoltage(left, right), // voltage
            driveTrain // Requires this drive subsystem
        ));

        FollowPathWithEvents twoPieceAuton = new FollowPathWithEvents(
            eventTesting,
            twoPiecePath.getMarkers(),
            eventMap
        );

        return new SequentialCommandGroup(
            new SetElevatorPositionCmd(elevator, 1),
            // new IntakeForGivenTime(intake, IntakeConstants.INTAKE_OUT_SPEED, 1),
            new SetElevatorPositionCmd(elevator, Constants.ElevatorConstants.MIN_ELEVATOR_HEIGHT),
            twoPieceAuton,
            new SetElevatorPositionCmd(elevator, 1.0),
            // new IntakeForGivenTime(intake, IntakeConstants.INTAKE_OUT_SPEED, 1),
            new SetElevatorPositionCmd(elevator, Constants.ElevatorConstants.MIN_ELEVATOR_HEIGHT)
        );
    
        // return new SequentialCommandGroup(
        //     new InstantCommand(() -> {
        //         // Reset odometry for the first path you run during auto
                
        //         driveTrain.resetOdometry(examplePath.getInitialPose());

        //     }, driveTrain),
        //     new PPRamseteCommand(
        //         examplePath,
        //         () -> driveTrain.getPoseMeters(), // Pose supplier
        //         new RamseteController(),
        //         new SimpleMotorFeedforward(Constants.DriveConstants.ks,
        //             Constants.DriveConstants.kv,
        //             Constants.DriveConstants.ka),
        //         Constants.DriveConstants.kDriveKinematics, // DifferentialDriveKinematics
        //         () -> driveTrain.getWheelSpeedsMetersPerSecond(), // DifferentialDriveWheelSpeeds supplier
        //         new PIDController(0, 0, 0), // Left controller. Tune these values for your robot. Leaving them 0
        //                 // will only use feedforwards.
        //         new PIDController(0, 0, 0), // Right controller (usually the same values as left controller)
        //         (left, right) -> driveTrain.setMotorVoltage(left, right), // voltage
        //         useAllianceColor, 
        //         driveTrain // Requires this drive subsystem
        //     ));

    }

    
}
