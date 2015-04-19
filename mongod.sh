#!/bin/sh
#First argument is IP of database host
mongod --dbpath /home/piotrek/IdeaProjects/git/mobile-cluster/mongodb/ --bind_ip $1