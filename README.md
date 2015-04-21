RPHash
======

Random Projection Hash For Scalable Data Clustering for the MapReduce Programming Model

Software Accompaniment of my current dissertation proposal work found
[here](https://github.com/leecarraher/nsf_proposal) 


Running the RPHash algorithm
=============================
5 Variants of RPHash and 2 version of kmeans, and agglomerative clustering are available from the RPHash jar. Each can be called independently, or as a set.
When run without a specific clustering type, the simple RPHash algorithm is run and the outputfile is the specified output file.
When run as a set, each set of discovered centroids is stored in the given outputfile with a descriptive suffix, ie "smpl","3stg"... appended to it.

* Note: The algorithm tries to guess a good energy constellation for decoding the leech lattice based on the variance of the data, however it works best between -1,1  so scaling may help considerably.

* usage: 

> java -jar RPHash.jar InputFile k OutputFile [simple(default),3stage,multiRP,multiProj,redux, kmeans, pkmeans, agglom]

* \*.mat file format:
<pre>
vectors
dimensions
x\_0\_0
x\_0\_1
...
x\_0\_\#dimensions
x\_1_0
...
x\_\#vectors_\#dimensions
</pre>

* example:
Begin Example InputFile.mat
=======================================
<pre>
5
4
1.0
2.0
3.0
4.0
1.1
2.1
3.1
4.1
1.2
2.2
3.2
4.2
1.3
2.3
3.3
4.3
1.4
2.4
3.4
4.4
</pre>
-------------End Example InputFile.mat------------

* Simple Usage:
    > 
    java -jar RPHash.jar InputFile.mat 2 out.mat

* Generates: 

out.mat

* Specific Clusterer:
    > 
    java -jar RPHash.jar InputFile.mat 2 out.mat redux

* Generates: 

 out.mat.itrdx

* Multiple Clusterers:
    > 
    java -jar RPHash.jar InputFile.mat 2 out.mat redux simple 3stage multiRP kmeans

* Generates:

 out.mat.itrdx
 
 out.mat.smpl
 
 out.mat.3stg
 
 out.mat.multirp
 
 out.mat.kmeans



Very simple comparison test
===========================
run.sh builds and runs the RPHash Algorithm on random gaussian clusters of 
varying dimension.

Parallel Deployment (WiP)
=========================

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







|      |                   |
| ---- |:-----------------:|
| 703  | DataNode          |
| 854  | SecondaryNameNode |
| 1630 | NameNode          |
| 1009 | ResourceManager   |
| 2026 | Jps               |
| 1122 | NodeManager       |

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

`hadoop pipes -D hadoop.pipes.java.recordreader=true  -D
hadoop.pipes.java.recordwriter=true -input data  -output MRRPHash-out -program
 /bin/MRRPHash `

* Automatic

`python filemaker.py`

`sh runner.sh PROGRAM_NAME`


> check results

`hdfs dfs -ls -r mrhash-out/`

`hdfs dfs -head mrhash-out/`







