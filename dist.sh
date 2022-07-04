#!/bin/bash

VERSION=2.5.3

modloader=(fabric forge)
mcvers=(1.16.5 1.17.1 1.18.1 1.18.2 1.19)
mkdir -p dist
for v in ${mcvers[@]}; do
	for m in ${modloader[@]}; do
	  lm=`echo "$m" | tr '[:upper:]' '[:lower:]'`
	  cp wands$v/$m/build/libs/BuildingWands_mc$v-${VERSION}-$lm.jar dist
	done
done


