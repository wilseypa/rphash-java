INCLUDE = 
LIBDIR = 
SHELL = /bin/bash
CXXFLAGS =  -Os   -msse2 
CXXLD = gcc



DEPDIR = 

SOURCES =  *.c 
OBJECTS = IOUtils.o testUtils.o LSHDecoder.o leechArrayDecoder.o  RPHash.o  

LDADD = 
CXXLINK =  $(CXXLD) $(CXXFLAGS)  -o $@

all: RPHash 

RPHash :
	$(CXXLINK) $(LDADD) $(LIBS) RPHash.c

Obs: $(OBJECTS)
	$(CXXLINK) $(OBJECTS) $(LDADD) $(LIBS)


clean:
	-rm -rf *.o RPHash
