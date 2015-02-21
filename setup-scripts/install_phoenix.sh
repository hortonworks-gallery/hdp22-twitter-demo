#install phoenix (only needed one time)
HBASE_LIB=/usr/hdp/2.2.0.0-2041/hbase/lib
cd
wget http://mirror.olnevhost.net/pub/apache/phoenix/phoenix-4.1.0/bin/phoenix-4.1.0-bin.tar.gz
tar -zxvf phoenix-4.1.0-bin.tar.gz
cp phoenix-4.1.0-bin/hadoop2/phoenix-4.1.0-server-hadoop2.jar $HBASE_LIB
chmod 644 $HBASE_LIB/phoenix-4.1.0-server-hadoop2.jar
ln -s $HBASE_LIB/phoenix-4.1.0-server-hadoop2.jar $HBASE_LIB/phoenix.jar
chmod ugo+rx /root
chmod ugo+rx /root/phoenix-4.1.0-bin
chmod ugo+rx /root/phoenix-4.1.0-bin/hadoop2
chmod ugo+r /root/phoenix-4.1.0-bin/hadoop2/*
chown -R root:root /root/phoenix-4.1.0-bin
rm /root/phoenix-4.1.0-bin.tar.gz
