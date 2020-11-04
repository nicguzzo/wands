package net.nicguzzo.common;

public enum MyDir{
    DOWN(0,1),
       UP(1,0),
       NORTH(2,3),
       SOUTH(3,2),
       WEST(4,5),
    EAST(5,4);
    private final int id;
    private final int idOpposite;
    private MyDir(int id, int idOpposite) {
        this.id = id;
        this.idOpposite = idOpposite;
    }
    public MyDir getOpposite(){
        return MyDir.values()[idOpposite];
    }
}