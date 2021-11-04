#!/bin/bash


find ./ -name *.java -exec ./scripts/disableAll.sh {} \;

#1.17.1 to 1.16.5

#find ./ -name *.java -exec ./scripts/enableMC.sh '1_16_5' {} \;
#cp gradle.properties_1.16.5 gradle.properties

#1.16.5 to 1.17.1 
#find ./ -name *.java -exec ./scripts/enableMC.sh '1_17_1' {} \;
#cp gradle.properties_1.17.1 gradle.properties
