#!/bin/bash

mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
for v in ${mcvers[@]}; do  
  pushd "mc$v/"
    ln -s ../src/build.gradle
    ln -s ../src/gradle
    ln -s ../src/gradlew
    ln -s ../src/settings.gradle
    ln -s ../versions/${v}_build.properties build.properties
    ln -s ../versions/${v}_gradle.properties gradle.properties
    pushd "common"
      ln -s ../../src/common/src
      ln -s ../../src/common/build.gradle  
    popd
    pushd "fabric"
      ln -s ../../src/fabric/src
      ln -s ../../src/fabric/build.gradle  
    popd
    pushd "forge"
      ln -s ../../src/forge/src
      ln -s ../../src/forge/build.gradle  
      ln -s ../../src/forge/gradle.properties
    popd
  popd
done
