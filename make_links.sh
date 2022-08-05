#!/bin/bash

mcvers=(1.16.5 1.17.1 1.18.1 1.18.2 1.19 1.19.1)
for v in ${mcvers[@]}; do  
  pushd wands$v
    mkdir -p "common/src/main/java/net/nicguzzo"
    pushd "common"
      rm build.gradle
      ln -s ../../common/build.gradle
    popd
    pushd "common/src/main"
      pushd "java/net/nicguzzo"
        rm wands
        ln -s ../../../../../../../common/src wands
      popd
      pushd "resources"
        rm assets
        rm data
        ln -s  ../../../../../assets
        ln -s  ../../../../../data
      popd
    popd
    mkdir -p "fabric/src/main/java/net/nicguzzo/wands"    
    pushd "fabric/"
      rm build.gradle
      ln -s  ../../fabric/build.gradle
    popd
    pushd "fabric/src/main/java/net/nicguzzo/wands"
      rm fabric
      ln -s  ../../../../../../../../fabric/src fabric
    popd
    mkdir -p "forge/src/main/java/net/nicguzzo/wands"
    pushd "forge/"      
      rm build.gradle
      ln -s  ../../forge/build.gradle
    popd
    pushd "forge/src/main/java/net/nicguzzo/wands"      
      rm forge
      ln -s  ../../../../../../../../forge/src forge      
    popd
  popd
done
