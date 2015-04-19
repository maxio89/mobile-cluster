#!/bin/sh
#First argument is IP of rpi and second argument is IP of database host
tar xzfv boot-0.1.tgz
cp sigar boot-0.1/bin/ -R
sed -i 's/-Xms1024M/-Xms128M/g' boot-0.1/bin/boot
sed -i 's/-Xmx1024M/-Xmx256M/g' boot-0.1/bin/boot
sed -i "s/-XX:MaxPermSize=256M/-Djava.library.path=.\/sigar -Dakka.remote.netty.tcp.hostname=$1 -Dakka.cluster.seed-nodes.0=akka.tcp:\/\/application@$1:2551 -Dakka.cluster.seed-nodes.1=akka.tcp:\/\/application@192.168.0.13:2551 -Dakka.cluster.seed-nodes.2=akka.tcp:\/\/application@192.168.0.12:2551 -Dakka.cluster.seed-nodes.3=akka.tcp:\/\/application@192.168.0.15:2551 -Dakka.cluster.seed-nodes.4=akka.tcp:\/\/application@192.168.0.16:2551 -Dcasbah-journal.mongo-journal-url=\"mongodb:\/\/$2:27017\/store.messages\"/g" boot-0.1/bin/boot