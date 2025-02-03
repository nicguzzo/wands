package net.nicguzzo.wands.wand;

public interface WandMode {

    void place_in_buffer(Wand wand);
    default boolean action(Wand wand){return true;}
    default void randomize(){};

}
