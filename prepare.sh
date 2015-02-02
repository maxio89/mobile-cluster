#!/bin/sh
sbt "project backend" "compile"
sbt "project boot" "compile"
sbt "project boot" "stage"
sbt "project boot" "universal:packageZipTarball"
scp -i ~/.ssh/id_rsa_pi boot/target/universal/boot-0.1.tgz pi@192.168.0.13:/home/pi/
