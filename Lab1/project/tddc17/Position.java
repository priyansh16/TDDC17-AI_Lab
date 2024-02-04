package tddc17;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Position {
    private int x;
    private int y;

    private Position parent;

    public Position getParent() {
        return parent;
    }

    public void setParent(Position parent) {
        this.parent = parent;
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }


    public int getY() {
        return y;
    }


    List<Position> getNeighbors()
    {
        //return a list of sorrounding positions
        var neigh = new ArrayList<Position>();

        //check for boundaries
        if(x+1 < 30 && y<30 && y>=0 && x>=0)
            neigh.add(new Position(x+1, y));
        if(x-1 >= 0 && y<30 && y>=0 && x<30)
            neigh.add(new Position(x-1, y));
        if(x < 30 && y+1<30 && y+1 >= 0 && x>=0)
            neigh.add(new Position(x, y+1));
        if(x < 30 && y-1<30 && y-1 >= 0 && x>=0)
            neigh.add(new Position(x, y-1));

        return neigh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString() {
    	return "(" + x + ", " + y + ")";
    }
}
