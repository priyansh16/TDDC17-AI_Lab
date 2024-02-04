package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.*;

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	public List<Position> currentPath = new ArrayList<>();
	public boolean backToHome = false;

	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
		{
			switch (agent_direction) {
				case MyAgentState.NORTH:
					agent_y_position--;
					break;
				case MyAgentState.EAST:
					agent_x_position++;
					break;
				case MyAgentState.SOUTH:
					agent_y_position++;
					break;
				case MyAgentState.WEST:
					agent_x_position--;
					break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}

	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 10;
	public MyAgentState state = new MyAgentState();

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0)
				state.agent_direction +=4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}


	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions>0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions==0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only moving forward.
		// START HERE - code below should be modified!

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);

		/*
		iterationCounter--;

		if (iterationCounter==0)
			return NoOpAction.NO_OP;
		*/

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean)p.getAttribute("bump");
		Boolean dirt = (Boolean)p.getAttribute("dirt");
		Boolean home = (Boolean)p.getAttribute("home");
		System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept)percept);
		if (bump) {
			switch (state.agent_direction) {
				case MyAgentState.NORTH:
					state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
					break;
				case MyAgentState.EAST:
					state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
					break;
				case MyAgentState.SOUTH:
					state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
					break;
				case MyAgentState.WEST:
					state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
					break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
		else if(!home)
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);

		state.printWorldDebug();


		// Next action selection based on the percept value
		if (dirt)
		{
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action=state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}
		else
		{
			Position nextPos = null;
			if(state.currentPath != null && !state.currentPath.isEmpty())
				nextPos = state.currentPath.get(0);

			if(nextPos != null && (state.agent_x_position == nextPos.getX() && state.agent_y_position == nextPos.getY() || state.world[nextPos.getX()][nextPos.getY()] == state.WALL))
			{
				if(state.backToHome && state.agent_x_position == 1 && state.agent_y_position == 1)
				{
					System.out.println("DONE");
					return NoOpAction.NO_OP;
				}

				state.currentPath.remove(0);
			}

			if(state.currentPath == null || state.currentPath.isEmpty())
			{
				state.currentPath = dfs(state.UNKNOWN);
				if(state.currentPath == null)
				{
					System.out.println("TIME TO GO HOME");
					state.backToHome = true;
					if(state.agent_x_position == 1 && state.agent_y_position == 1)
					{
						System.out.println("DONE");
						return NoOpAction.NO_OP;
					}
					else
						state.currentPath = dfs(state.HOME);
				}

				nextPos = state.currentPath.get(0);
			}

			System.out.println("CURRENT POS: (" + state.agent_x_position + ", " + state.agent_y_position+")");
			System.out.println("CURRENT PATH"+ state.currentPath);
			System.out.println("GOING TO: (" + nextPos.getX() + ", " + nextPos.getY()+")");

			int direction = getDirection(nextPos);
			
			if(state.agent_direction == direction)
			{
				//go forward
				state.agent_last_action=state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			}
			else return getRotation(direction);
			
		}
	}
	
	//returns the quickest rotation action to do based on the current direction to reduce 
	//the performance score loss
	private Action getRotation(int direction)
	{
		if(state.agent_direction == MyAgentState.NORTH && direction == MyAgentState.WEST || state.agent_direction == MyAgentState.WEST && direction == MyAgentState.SOUTH || state.agent_direction == MyAgentState.SOUTH && direction == MyAgentState.EAST || state.agent_direction == MyAgentState.EAST && direction == MyAgentState.NORTH)
		{
			//rotate left
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0)
				state.agent_direction +=4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		}
		else
		{
			//rotate right
			state.agent_direction = ((state.agent_direction+1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
	}

	//returns the right direction based on the current and next position
	private int getDirection(Position nextPos) {
		int x = nextPos.getX();
		int y = nextPos.getY();

		if (x == state.agent_x_position - 1 && y == state.agent_y_position)
			return MyAgentState.WEST;
		if (x == state.agent_x_position && y == state.agent_y_position - 1)
			return MyAgentState.NORTH;
		if (x == state.agent_x_position + 1 && y == state.agent_y_position)
			return MyAgentState.EAST;
		if (x == state.agent_x_position && y == state.agent_y_position + 1)
			return MyAgentState.SOUTH;

		return -1;
	}


	List<Position> dfs(int goal)
	{
		//dfs
		//create a queue
		Queue<Position> queue = new LinkedList<>();
		//create a set to keep track of visited nodes
		Set<Position> visited = new HashSet<>();
		//add the start node to the queue
		queue.add(new Position(state.agent_x_position, state.agent_y_position));
		//add the start node to the visited set
		visited.add(new Position(state.agent_x_position, state.agent_y_position));
		//while the queue is not empty
		while(!queue.isEmpty())
		{
			//remove the first node from the queue
			Position current = queue.remove();
			//if the current node is the goal node
			if(state.world[current.getX()][current.getY()] == goal)
			{
				//return the path to the goal node
				return getPath(current);
			}
			//for each neighbor of the current node
			for(Position neighbor : current.getNeighbors())
			{
				//if the neighbor is not visited
				if(!visited.contains(neighbor) && state.world[neighbor.getX()][neighbor.getY()] != state.WALL)
				{
					//add the neighbor to the visited set
					visited.add(neighbor);
					//add the neighbor to the queue
					queue.add(neighbor);
					//set the parent of the neighbor to the current node
					neighbor.setParent(current);
				}
			}
		}
		//return null if no path is found
		return null;
	}

	private List<Position> getPath(Position current) {
		//create a list to store the path
		List<Position> path = new ArrayList<>();
		//while the current node has a parent
		while(current.getParent() != null)
		{
			//add the current node to the path
			path.add(current);
			//set the current node to its parent
			current = current.getParent();
		}
		//reverse the path
		Collections.reverse(path);
		//return the path
		return path;
	}

}



public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
