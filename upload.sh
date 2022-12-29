#!/bin/bash
pushd "versions"
	mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
	for v in ${mcvers[@]}; do
		pushd mc$v
			./gradlew --no-daemon publishUnified
		popd
	done
popd