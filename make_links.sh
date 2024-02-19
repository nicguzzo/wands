#!/bin/bash

#on windows: git bash only, run git bash console as admin , enable sylinks on gitconfig and export MSYS=winsymlinks:nativestrict

export MSYS=winsymlinks:nativestrict


if [ "$1" == "" ]; then
  mcvers=( "1.16.5" "1.17.1" "1.18.1" "1.18.2" "1.19" "1.19.1" "1.19.2" "1.19.3" "1.19.4" "1.20" "1.20.1" "1.20.2" "1.20.4")
else
  mcvers=( $1 )
fi

pushd "versions"
  for v in ${mcvers[@]}; do  
#    rm -rf "mc$v/"
    mkdir -p  "mc$v/"
    pushd "mc$v/"            
      ln -s ../../src/build.gradle
      ln -s ../../src/gradle
      ln -s ../../src/gradlew
      ln -s ../../src/settings.gradle
      ln -s ../../versions/${v}_build.properties build.properties
      ln -s ../../versions/${v}_gradle.properties gradle.properties
      mkdir -p common
      pushd "common"
        ln -s ../../../src/common/src
        ln -s ../../../src/common/build.gradle  
      popd
      mkdir -p fabric
      pushd "fabric"
        ln -s ../../../src/fabric/src
        ln -s ../../../src/fabric/build.gradle  
      popd
      mkdir -p forge
      pushd "forge"
        ln -s ../../../src/forge/src
        ln -s ../../../src/forge/build.gradle  
        ln -s ../../../src/forge/gradle.properties
      popd
    popd
  done
popd
