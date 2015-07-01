#!/bin/bash

if [ ! -e '/etc/yum.repos.d/epel-apache-maven.repo' ]
then

	curl -o /etc/yum.repos.d/epel-apache-maven.repo https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo
fi

yum -y install apache-maven

mkdir -p /root/.m2/
cp /root/hdp22-twitter-demo/settings.xml /root/.m2/

#mkdir /usr/share/maven
#cd /usr/share/maven
#wget http://mirrors.koehn.com/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
#tar xvzf apache-maven-3.2.5-bin.tar.gz
#ln -s /usr/share/maven/apache-maven-3.2.5/ /usr/share/maven/latest
#echo 'M2_HOME=/usr/share/maven/latest' >> ~/.bashrc
#echo 'M2=$M2_HOME/bin' >> ~/.bashrc
#echo 'PATH=$PATH:$M2' >> ~/.bashrc
#export M2_HOME=/usr/share/maven/latest
#export M2=$M2_HOME/bin
#export PATH=$PATH:$M2


