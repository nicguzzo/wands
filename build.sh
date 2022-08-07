#!/bin/bash
mod="wands"
if [ "$1" == "" ]; then
  mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
else
  mcvers=( $1 )
fi
for v in ${mcvers[@]}; do  
  pushd mc$v      
      ./gradlew --no-daemon --parallel build &>../../build_log_${mod}_mc${v} || exit
  popd
done