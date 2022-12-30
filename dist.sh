#!/bin/bash

VERSION=2.6.4_release
mod="BuildingWands"
modloader=(fabric forge)

instances=~/minecraft/testing_instances
deps=~/minecraft/wands_deps

pushd "versions"
	mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
	for v in ${mcvers[@]}; do
		for m in ${modloader[@]}; do
		  lm=`echo "$m" | tr '[:upper:]' '[:lower:]'`
		  echo " "
		  echo "================="
		  echo "$lm $v"

		  if [ -d $instances/test_${m}_${v}/.minecraft/mods/ ]; then	  	
		  	cp $deps/${m}/${v}/*.jar $instances/test_${m}_${v}/.minecraft/mods/
		  	if [ -f mc$v/$m/build/libs/${mod}_mc$v-${VERSION}-$lm.jar ]; then
		  		rm $instances/test_${m}_${v}/.minecraft/mods/${mod}*
		  		cp mc$v/$m/build/libs/${mod}_mc$v-${VERSION}-$lm.jar $instances/test_${m}_${v}/.minecraft/mods/
		  	fi
		  	ls -1 $instances/test_${m}_${v}/.minecraft/mods/
		  fi
		  echo "================="
		done
	done

popd