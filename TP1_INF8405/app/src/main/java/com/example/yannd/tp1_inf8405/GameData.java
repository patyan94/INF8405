package com.example.yannd.tp1_inf8405;

/**
 * Created by yannd on 2016-02-07.
 */
public class GameData {
    private GameData()
    {

    }

    private static GameData instance = new GameData();

    public static GameData getInstance()
    {	return instance;
    }

    public void setLevel77(int level){
        levels77 = level;
    }
    public void setLevel88(int level){
        levels88 = level;
    }
    public void setGridSize(int size)
    {
        gridSize = size;
    }
    public int getLevelsUnlocked77(){
        return levels77;
    }
    public int getLevelsUnlocked88()
    {
        return levels88;
    }
    public int getGridSize()
    {
        return  gridSize;
    }

    private int gridSize;
    private int levels77 = 1;
    private int levels88 = 1;
}
