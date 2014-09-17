RPHash
======

Random Projection Hash For Scalable Data Clustering for the MapReduce Programming Model

Software Accompaniment of my current dissertation proposal work found
[here](https://github.com/leecarraher/nsf_proposal) 


* Very simple comparison test
run.sh builds and runs the RPHash Algorithm on random gaussian clusters of 
varying dimension.


* Distributed how to Run

a 64bit hadoop lxc container with 4 nodes can be downloaded [here](http://homepages.uc.edu/~carrahle/cluster.tar.bz2 "Hadoop Containers")

* username:ubuntu 
* password:ubuntu
* ubuntu has sudo access

`cd /var/lib/lxc`

`sudo tar -jxf cluster.tar.bz2`

> change if you want, but shouldn't matter as long as you keep your containers 
> behind a firewall.

`sudo lxc-start -n master1 -d`

> Do not launch the 'master' container directly, instead 
> launch the 'master1' and 'slave[1-3]' delta/snapshots.
> the ssh keys are shared, but your /etc/hosts file will likely need to be changed
> so the master1 node can contact the slave nodes.

`sudo lxc-start -n master1 -d`

`sudo lxc-start -n slave1 -d`

`sudo lxc-start -n slave2 -d`

`sudo lxc-start -n slave3 -d`

...



> as user ubuntu start hadoop

`start-all.sh`

`jps` > should have 6 entries, if namenode is not among them, 

`hadoop namenode -format` > among them then "start-all.sh" again

`start-all.sh`

`jps`

> Should look like this
|703 DataNode|
|854 SecondaryNameNode|
|1630 NameNode|
|1009 ResourceManager|
|2026 Jps|
|1122 NodeManager|

* Manual Run
> create default hadoop directories for ubuntu user
`hdfs dfs -mkdir -p /user/hadoop/bin`
`hdfs dfs -mkdir -p /user/hadoop/data`

> enter Map Reduce RP Hash Directory
`cd MRRPHash`

> copy files to hadoop distributed file system
`hdfs dfs -put ik2_10_100_10 data`

`hdfs dfs -put mrhash bin` > rp hash

`hdfs dfs -put kmeans bin` > kmeans for testing


> run it

`hadoop pipes -D hadoop.pipes.java.recordreader=true  -D \
hadoop.pipes.java.recordwriter=true -input data  -output MRRPHash-out -program\
 /bin/MRRPHash `

* Automatic

`python filemaker.py`

`sh runner.sh PROGRAM_NAME`


> check results
`hdfs dfs -ls -r mrhash-out/`

`hdfs dfs -head mrhash-out/`







