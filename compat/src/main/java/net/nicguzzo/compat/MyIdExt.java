package net.nicguzzo.compat;

#if MC_VERSION >= 12111
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

public class MyIdExt {

    #if MC_VERSION >= 12111
      public Identifier res;
    #else
    public ResourceLocation res;
    #endif
    public MyIdExt() {
        res=null;
    }
  public MyIdExt(String str) {
    #if MC_VERSION < 12100
      res = new ResourceLocation(str);
    #else
    #if MC_VERSION >= 12111
      res = Identifier.withDefaultNamespace(str);
    #else
      res = ResourceLocation.withDefaultNamespace(str);
    #endif
    #endif
  }

  public MyIdExt(String namespace, String path) {
    #if MC_VERSION < 12100
      res = new ResourceLocation(namespace, path);
    #else
        #if MC_VERSION >= 12111
          res = Identifier.fromNamespaceAndPath(namespace,path);
        #else
          res =  ResourceLocation.fromNamespaceAndPath(namespace,path);
        #endif
    #endif
  }
  public void tryParse(String str){
        #if MC_VERSION >= 12111
            res= Identifier.tryParse(str);
        #else
            res= ResourceLocation.tryParse(str);
        #endif

  }
}