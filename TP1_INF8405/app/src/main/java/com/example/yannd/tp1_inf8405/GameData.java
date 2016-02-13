package com.example.yannd.tp1_inf8405;

/**
 * Created by yannd on 2016-01-28.
 *
 * This class represents the state of the current gaming session
 * The levels that are unlocked, and th current grid size
 */
public class GameData {
    // Constructor
    private GameData()
    {

    }

    // Singleton class< instance
    private static GameData instance = new GameData();
    public static GameData getInstance()
    {	return instance;
    }
    // Sets the last unlocked level for 7x7 grids
    public void setLevel77(int level){ levels77 = level; }
    // Sets the last unlocked level for 8x8 grids
    public void setLevel88(int level){
        levels88 = level;
    }
    // Sets the current grid size being played
    public void setGridSize(int size)
    {
        gridSize = size;
    }
    // Gets the last unlocked level for 7x7 grids
    public int getLevelsUnlocked77(){
        return levels77;
    }
    // Gets the last unlocked level for 8x8 grids
    public int getLevelsUnlocked88()
    {
        return levels88;
    }
    // Gets the current grid size being played
    public int getGridSize()
    {
        return  gridSize;
    }


    // the current grid size being played
    private int gridSize;
    // the last unlocked level for 7x7 grids
    private int levels77 = 1;
    // the last unlocked level for 8x8 grids
    private int levels88 = 1;
}
