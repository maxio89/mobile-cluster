#!/bin/sh
tar xzfv boot-0.1.tgz
cp sigar boot-0.1/bin/ -R
sed -i 's/-Xms1024M/-Xms128M/g' boot-0.1/bin/boot
sed -i 's/-Xmx1024M/-Xmx128M/g' boot-0.1/bin/boot
sed -i "s/-XX:MaxPermSize=256M/-Djava.library.path=.\/sigar -Dakka.remote.netty.tcp.hostname=192.168.0.$1 -Dcasbah-journal.mongo-journal-url=\"mongodb:\/\/192.168.0.14:27017\/store.messages\"/g" boot-0.1/bin/boot