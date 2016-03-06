#!/bin/bash

if [ ! -e '/etc/yum.repos.d/epel-apache-maven.repo' ]
then

	curl -o /etc/yum.repos.d/epel-apache-maven.repo https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo
fi

yum -y install apache-maven

#mkdir -p /root/.m2/
#cp /root/hdp22-twitter-demo/settings.xml /root/.m2/


