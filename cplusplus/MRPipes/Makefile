CC = g++
HADOOP_INSTALL = /opt/hadoop-2.4.1/
CPPFLAGS =  -m64 -Wall -I$(HADOOP_INSTALL)/include/



mrhash: MRRPHash.cpp 
	$(CC) $(CPPFLAGS) $<  -L$(HADOOP_INSTALL)/lib/native/ -lhadooppipes \
	-lhadooputils -lpthread -lcrypto -g -O2 -o $@	
	#scp -oStrictHostKeyChecking=no mrhash ubuntu@master1:/home/ubuntu/MRRPHash/

wordcount: wordcount.cpp 
	$(CC) $(CPPFLAGS) $<  -L$(HADOOP_INSTALL)/lib/native/ -lhadooppipes \
	-lhadooputils -lpthread -lcrypto -g -O2 -o $@	
	#scp -oStrictHostKeyChecking=no wordcount ubuntu@master1:/home/ubuntu/MRRPHash/	



clean:
	rm mrhash wordcount
