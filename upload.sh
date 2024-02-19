#!/bin/bash
. ../wands_env_token
pushd "versions"
#	mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`

	if [ "$1" == "" ]; then
    	mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
  	else
	    mcvers=( $1 )
  	fi
	for v in ${mcvers[@]}; do
		pushd mc$v
			cp -av common/src/main/resources/${v}_wands.accesswidener common/src/main/resources/wands.accesswidener
			./gradlew --no-daemon publishUnified
		popd
	done
popd
