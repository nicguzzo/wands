#!/bin/bash


enableMC(){
	ver=$1
	file=$2
	sed -e  "s/\/\*\/\/beginMC$ver/\/\/beginMC$ver/g" -e  "s/\/\/endMC$ver\*\//\/\/endMC$ver /g" $file
}
disableMC(){
	ver=$1
	file=$2
	sed -e  "s/\/\/beginMC$ver/\/\*\/\/beginMC$ver/g" -e  "s/\/\/endMC$ver/\/\/endMC$ver\*\//g" $file
}

disableMC '1_16_5' './common/src/main/java/net/nicguzzo/wands/WandsMod.java'
enableMC '1_17_1' './common/src/main/java/net/nicguzzo/wands/WandsMod.java'

#find ./ -name *.java
