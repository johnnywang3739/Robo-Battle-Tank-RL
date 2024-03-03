/**
 * Experience stored in replay memory
 * - Previous state
 * - Previous action
 * - Current reward
 * - Current state
 */
public class Experience {
    public RobotState previous_state;
    public RobotState.Action previous_action;
    public double current_reward;
    public RobotState current_state;
    // Constructor
    public Experience(RobotState prevState, RobotState.Action prevAction, double currReward, RobotState currState) {
        this.previous_state = prevState;
        this.previous_action = prevAction;
        this.current_reward = currReward;
        this.current_state = currState;
    }
}
