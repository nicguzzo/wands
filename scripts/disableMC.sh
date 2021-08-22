#!/bin/bash
ver=$1
file=$2
sed -i -e  "s/\/\/beginMC$ver/\/\*\/\/beginMC$ver/g" -e  "s/\/\/endMC$ver/\/\/endMC$ver\*\//g" $file