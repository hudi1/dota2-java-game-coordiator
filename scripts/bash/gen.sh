#!/bin/bash

BASE="$HOME/programy/git/dota-bot"

run()
{
	cd $1
	mvn exec:exec -P db2mo
	if [ $? -ne 0 ]; then
		exit 1
	fi
	mvn exec:exec -P db2me
	if [ $? -ne 0 ]; then
		exit 1
	fi
	mvn exec:exec -P mo2ja
	if [ $? -ne 0 ]; then
		exit 1
	fi
}


run $BASE/"dota-dao"

