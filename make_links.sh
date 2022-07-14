#!/bin/bash

mcvers=(1.16.5 1.17.1 1.18.1 1.18.2 1.19)
for v in ${mcvers[@]}; do  
  pushd wands$v
    mkdir -p "common/src/main/java/net/nicguzzo"
    pushd "common/src/main"
      pushd "java/net/nicguzzo"
        ln -s ../../../../../../../src wands
      popd
      pushd "resources"
        ln -s  ../../../../../assets
        ln -s  ../../../../../data
      popd
    popd
    mkdir -p "fabric/src/main/java/net/nicguzzo/wands"    
    pushd "fabric/src/main/java/net/nicguzzo/wands"
      ln -s  ../../../../../../../../fabric
    popd
    mkdir -p "forge/src/main/java/net/nicguzzo/wands"
    pushd "forge/src/main/java/net/nicguzzo/wands"
      ln -s  ../../../../../../../../forge      
    popd
  popd
done
