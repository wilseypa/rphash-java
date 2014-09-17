RPHash
======

Random Projection Hash For Scalable Data Clustering for the MapReduce Programming Model

Software Accompaniment of my current dissertation proposal work found here:
https://github.com/leecarraher/nsf_proposal

* Very simple comparison test
run.sh builds and runs the RPHash Algorithm on random gaussian clusters of 
varying dimension.


* Distributed how to Run
a master lxc server can be downloaded here. 
username:ubuntu 
password:ubuntu
ubuntu has sudo access

cd /var/lib/lxc

sudo tar -jxf master.tar.bz2

# change if you want, but shouldn't matter as long as you keep your containers 
# behind a firewall.

sudo lxc-start -n master1 -d
sudo lxc-attach -n master1
# on master
shutdown now -r

# For single system deployments do not launch this container directly, instead 
# created delta/snapshot containers of this one. Change /etc/hadoop/masters and
# /etc/hadoop/slaves to match your # desired configuration. .ssh keys will all 
# be the same so no need to update them.
# contains my public key, which would give me access to a container of your's 
# that is not behind a # firewall. delete it if your containers are public!
# create containers slaves[1-7] as desired

sudo lxc-start -n master1 -d
sudo lxc-start -n slave1 -d
...


#build and copy to master1 (assumes you have master1 running)
cd MRPipes
make MRPIPES

#
sudo lxc-attach -n master1

su ubuntu
#start hadoop
start-all.sh
jps # should have 6 entries, hadoop namenode -format if namenod is missing, 
    # then "start-all.sh" again

#create default directories for ubuntu user
hdfs dfs -mkdir -p /users/hadoop/bin
hdfs dfs -mkdir -p /users/hadoop/data

#copy files to hadoop distributed file system
hdfs dfs -put SOME_LOCAL_DATAFILE.mat data
hdfs dfs -put MRRPHash bin

#run it
hadoop pipes -D hadoop.pipes.java.recordreader=true  -D \
hadoop.pipes.java.recordwriter=true -input data  -output MRRPHash-out -program\
 /bin/MRRPHash
...

#check results
hdfs dfs -ls -r MRRPHash-out/
hdfs dfs -head MRRPHash-out/WHATEVER_OUTPUT_FROM_ABOVE







