#!/bin/bash

mcvers=(1.16.5 1.17.1 1.18.1 1.18.2 1.19)
for v in ${mcvers[@]}; do  
  pushd wands$v
      ./gradlew clean >>../../build_log || exit
      ./gradlew build >>../../build_log|| exit
  popd
done