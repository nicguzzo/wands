#!/bin/bash

VERSION=2.6.1
TYPE=beta
mcvers=(1.16.5 1.17.1 1.18.1 1.18.2 1.19)
for v in ${mcvers[@]}; do  
  sed -i "s/mod_version=.*/mod_version=$VERSION/" wands$v/gradle.properties
  sed -i "s/release_type=.*/release_type=$TYPE/" wands$v/gradle.properties
done