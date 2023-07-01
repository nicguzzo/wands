#!/bin/bash
mod="wands"
pushd "versions"
  if [ "$1" == "" ]; then
    mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
  else
    mcvers=( $1 )
  fi
  for v in ${mcvers[@]}; do  
    pushd mc$v      
        #./gradlew --no-daemon --parallel build &>../../build_log_${mod}_mc${v} || exit
        if [ "$v" == "1.20" ]; then
          echo "copying netherite_wand_post_1_20.json" 
          cp ../../recipes/netherite_wand_post_1_20.json common/src/main/resources/data/wands/recipes/netherite_wand.json
        else
          echo "copying netherite_wand_pre_1_20.json"
          cp ../../recipes/netherite_wand_pre_1_20.json  common/src/main/resources/data/wands/recipes/netherite_wand.json
        fi
        ./gradlew build || exit
    popd
  done
popd