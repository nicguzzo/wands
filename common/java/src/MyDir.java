package net.nicguzzo.common;

public enum MyDir{
    DOWN(1),
    UP(0),
    NORTH(3),
    SOUTH(2),
    WEST(5),
    EAST(4);
    private final int idOpposite;
    private MyDir(int idOpposite) {
        this.idOpposite = idOpposite;
    }
    public MyDir getOpposite(){
        return MyDir.values()[idOpposite];
    }
}