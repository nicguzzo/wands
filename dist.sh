#!/bin/bash

VERSION=2.6.3_release
mod="BuildingWands"
modloader=(fabric forge)
mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
for v in ${mcvers[@]}; do
	for m in ${modloader[@]}; do
	  lm=`echo "$m" | tr '[:upper:]' '[:lower:]'`
	  echo " "
	  echo "================="
	  echo "$lm $v"

	  if [ -d ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/ ]; then	  	
	  	cp ~/minecraft/wands_deps/${m}/${v}/*.jar ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/
	  	if [ -f mc$v/$m/build/libs/${mod}_mc$v-${VERSION}-$lm.jar ]; then
	  		rm ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/${mod}*
	  		cp mc$v/$m/build/libs/${mod}_mc$v-${VERSION}-$lm.jar ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/
	  	fi
	  	ls -1 ~/minecraft/testing_instances/test_${m}_${v}/.minecraft/mods/
	  fi
	  echo "================="
	done
done


