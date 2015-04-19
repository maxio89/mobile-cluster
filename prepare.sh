#!/bin/sh
#First argument is IP of rpi
sbt "project backend" "compile"
sbt "project boot" "compile"
sbt "project boot" "stage"
sbt "project boot" "universal:packageZipTarball"
scp -i ~/.ssh/id_rsa_pi boot/target/universal/boot-0.1.tgz pi@$1:/home/pi/
