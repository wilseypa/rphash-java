CC = g++
HADOOP_INSTALL = /home/lee/Desktop/RPHash/lib/hadoop-2.4.1-src/hadoop-dist/target/hadoop-2.4.1
CPPFLAGS = -m64 -I$(HADOOP_INSTALL)/include/

wordcount: wordcount.cpp
	$(CC) $(CPPFLAGS) $< -Wall -L$(HADOOP_INSTALL)/lib/native/ -lhadooppipes \
	-lhadooputils -lpthread -lcrypto -g -O2 -o $@
