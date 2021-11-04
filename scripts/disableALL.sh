#!/bin/bash
file=$1
sed -i -e  "s/^ *\/\/beginMC/\/\*\/\/beginMC/g" -e  "s/^ *\/\/endMC/\/\/endMC\*\//g" $file
