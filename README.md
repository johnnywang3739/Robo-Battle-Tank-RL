
# Robo Battle Tank Reinforcement Learning

## Description

This project is a reinforcement learning (RL) based battle tank simulation. It uses Q-learning and Neural Networks to train a tank agent to compete in a battle environment. The agent learns from its experiences and improves its performance over time.


## Usage

The project contains several Java classes that implement different components of the reinforcement learning system:

- **Experience.java**: Manages the experience replay mechanism used to train the neural network.
- **LUT.java**: Implements a Lookup Table for Q-learning.
- **MyAgent.java**: The main agent class that interacts with the environment.
- **MyAgent_NN.java**: An extension of MyAgent that uses a Neural Network for decision making.
- **NeuralNet.java**: Implements a simple feedforward neural network.
- **RobotState.java**: Represents the state of the robot in the environment.

### Running a Simulation
You can run a battle simulation using the main interface. The agent will use either a Lookup Table (LUT) or Neural Network (NN) to make decisions and compete in the battle environment.

## Files and Directories

- **`src/Experience.java`**: Contains the logic for experience replay, a crucial part of the reinforcement learning process.
- **`src/LUT.java`**: Implements the Q-learning Lookup Table (LUT) used by the agent.
- **`src/MyAgent.java`**: Defines the agent's behavior using traditional Q-learning.
- **`src/MyAgent_NN.java`**: Extends `MyAgent` to use a Neural Network for decision making.
- **`src/NeuralNet.java`**: Contains the implementation of a simple neural network used by `MyAgent_NN`.
- **`src/RobotState.java`**: Manages the state of the robot during simulation.
- **`src/Interface/`**: Contains various interfaces used throughout the project, such as `LUTInterface` and `NeuralNetInterface`.

## Reinforcement Learning

The project implements a reinforcement learning approach using Q-learning and Neural Networks:
- **Q-learning**: A model-free RL algorithm that the agent uses to learn the optimal action-value function.
- **Neural Networks**: Used as function approximators in place of the traditional Lookup Table in Q-learning.
