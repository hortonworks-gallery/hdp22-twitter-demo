# User configured section

# Ambari configuration
host='sandbox.hortonworks.com:8080'
user='admin'
pass='admin'

# End user configured section


# Sandbox hostname override - if the user doesn't edit this file, and runs on sandbox, we can make it work with known values
if [ `hostname` = sandbox.hortonworks.com ]; then
        cluster='Sandbox'
		host='sandbox.hortonworks.com:8080'
fi

output=`curl -u $user:$pass -i -H 'X-Requested-By: ambari'  http://$host/api/v1/clusters`
cluster=`echo $output | sed -n 's/.*"cluster_name" : "\([^\"]*\)".*/\1/p'`
