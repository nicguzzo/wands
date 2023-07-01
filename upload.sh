#!/bin/bash
pushd "versions"
#	mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`

	if [ "$1" == "" ]; then
    	mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
  	else
	    mcvers=( $1 )
  	fi
	for v in ${mcvers[@]}; do
		pushd mc$v
			if [ "$v" == "1.20" ]; then
	          echo "copying netherite_wand_post_1_20.json" 
	          cp ../../recipes/netherite_wand_post_1_20.json common/src/main/resources/data/wands/recipes/netherite_wand.json
	        else
	          echo "copying netherite_wand_pre_1_20.json"
	          cp ../../recipes/netherite_wand_pre_1_20.json  common/src/main/resources/data/wands/recipes/netherite_wand.json
	        fi
	        ./gradlew build || exit
			./gradlew --no-daemon publishUnified
		popd
	done
popd
