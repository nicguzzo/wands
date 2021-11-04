#!/bin/sh

ver=$1

cp versions/gradle.properties_$ver gradle.properties
cp versions/fabric.mod.json_$ver fabric/src/main/resources/fabric.mod.json
cp versions/mods.toml_$ver forge/src/main/resources/META-INF/mods.toml

if [ "$ver" = "1.16.5" ] 
then
  cp versions/fabric.mod.json_aux_$ver common/src/main/resources/fabric.mod.json  
fi
