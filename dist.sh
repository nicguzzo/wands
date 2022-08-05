#!/bin/bash

VERSION=2.6.1_beta

modloader=(fabric forge)
mcvers=(1.16.5 1.17.1 1.18.1 1.18.2 1.19 1.19.1)
mkdir -p dist
rm -f dist/*
for v in ${mcvers[@]}; do
	for m in ${modloader[@]}; do
	  lm=`echo "$m" | tr '[:upper:]' '[:lower:]'`
	  #cp wands$v/$m/build/libs/BuildingWands_mc$v-${VERSION}-$lm.jar dist
	  echo " "
	  echo "================="
	  echo "$lm $v"

	  if [ -d ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/ ]; then
	  	rm -f ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/*
	  	cp ~/minecraft/wands_deps/${m}/${v}/*.jar ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/
	  	if [ -f wands$v/$m/build/libs/BuildingWands_mc$v-${VERSION}-$lm.jar ]; then
	  		cp wands$v/$m/build/libs/BuildingWands_mc$v-${VERSION}-$lm.jar ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/
	  	fi
	  	ls -1 ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/
	  fi
	  echo "================="
	done
done


