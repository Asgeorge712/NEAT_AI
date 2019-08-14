package com.paulgeorge.neat.snake;

import java.util.Random;

enum Direction {
	NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);

	int dir[];

	Direction(int... dir) {
		this.dir = dir;
	}

	public static Direction getRandom(Random r) {
		return values()[r.nextInt(values().length)];
	}

	public int getX() {
		return dir[0];
	}

	public int getY() {
		return dir[1];
	}
}
