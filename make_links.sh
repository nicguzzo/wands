#!/bin/bash

#on windows: git bash only, enable sylinks on gitconfig, export MSYS=winsymlinks:nativestrict and run git bash console as admin.


mcvers=( "1.16.5" "1.17.1" "1.18.1" "1.18.2" "1.19" "1.19.1" "1.19.2" "1.19.3" "1.19.4" "1.20")
pushd "versions"
  for v in ${mcvers[@]}; do  
    rm -rf "mc$v/"
    mkdir  "mc$v/"
    pushd "mc$v/"            
      ln -s ../../src/build.gradle
      ln -s ../../src/gradle
      ln -s ../../src/gradlew
      ln -s ../../src/settings.gradle
      ln -s ../../versions/${v}_build.properties build.properties
      ln -s ../../versions/${v}_gradle.properties gradle.properties
      mkdir common
      pushd "common"
        ln -s ../../../src/common/src
        ln -s ../../../src/common/build.gradle  
      popd
      mkdir fabric
      pushd "fabric"
        ln -s ../../../src/fabric/src
        ln -s ../../../src/fabric/build.gradle  
      popd
      mkdir forge
      pushd "forge"
        ln -s ../../../src/forge/src
        ln -s ../../../src/forge/build.gradle  
        ln -s ../../../src/forge/gradle.properties
      popd
    popd
  done
popd
