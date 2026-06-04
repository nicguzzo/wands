package net.nicguzzo.wands.compat;

//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?}else{
    /*import net.minecraft.resources.ResourceLocation;
*///? }

public class RcId{
    //? if >= 1.21.11 {
    private final Identifier id;
    private RcId(Identifier id) {
        this.id = id;
    }
    //?}else{
    /*private final ResourceLocation id;
    private RcId(ResourceLocation id) {
        this.id = id;
    }
    *///?}

    private RcId(String str) {
        //? if >= 1.21.11 {
          id = Identifier.withDefaultNamespace(str);
        //?}else{
          /*//? if >= 1.21 {
            id = ResourceLocation.withDefaultNamespace(str);
          //?}else{
            /^id = new ResourceLocation(str);
          ^///?}
        *///?}
    }
    private RcId(String ns,String str) {
        //? if >= 1.21.11 {
          id = Identifier.fromNamespaceAndPath(ns,str);
        //?}else{
          /*//? if >= 1.21 {
            id = ResourceLocation.fromNamespaceAndPath(ns,str);
          //?}else{
            /^id = new ResourceLocation(ns,str);
          ^///?}
        *///?}
    }
    public static RcId withDefaultNamespace(String str) {
        return new RcId(str);
    }
    public static RcId fromNamespaceAndPath(String ns,String str) {
        return new RcId(ns,str);
    }
    //? if >= 1.21.11 {
        
        static public RcId tryParse(String string) {
            return new RcId(Identifier.tryParse(string));
        }
        static public RcId parse(String string) {
            return new RcId(Identifier.parse(string));
        }
        public Identifier id() {
            return id;
        }
    //?}else{
       /*static public RcId tryParse(String string) {
          return new RcId(ResourceLocation.tryParse(string));
       }
       public ResourceLocation id() {
            return id;
        }
    *///?}
}
