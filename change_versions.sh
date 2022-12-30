#!/bin/bash

VERSION=2.6.4
TYPE=release
sed -i "s/VERSION=.*/VERSION=${VERSION}_${TYPE}/" dist.sh
pushd "versions"
  mcvers=`ls |grep -P "mc1\..+"|tr "\n" " "|sed 's/mc//g'`
  for v in ${mcvers[@]}; do  

    sed -i "s/mod_version=.*/mod_version=$VERSION/" ${v}_gradle.properties
    sed -i "s/release_type=.*/release_type=$TYPE/" ${v}_gradle.properties

    sed -i "s/mod_version=.*/mod_version=$VERSION/" mc${v}/gradle.properties
    sed -i "s/release_type=.*/release_type=$TYPE/" mc${v}/gradle.properties
  done
popd