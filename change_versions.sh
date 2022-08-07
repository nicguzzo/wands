#!/bin/bash

VERSION=2.6.2
TYPE=release
mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
sed -i "s/VERSION=.*/VERSION=${VERSION}_${TYPE}/" dist.sh
for v in ${mcvers[@]}; do  
  sed -i "s/mod_version=.*/mod_version=$VERSION/" mc$v/gradle.properties
  sed -i "s/release_type=.*/release_type=$TYPE/" mc$v/gradle.properties
done