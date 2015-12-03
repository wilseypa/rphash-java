import sys

fin = file(sys.argv[1],'rb')
fout =  file(sys.argv[2],'w')
flbl =  file(sys.argv[3],'w')

s = fin.readline()
dx = len(s[:-1].split('\t'))-1
dy = 0
#counting vectors
while not s == "":
	dy+=1
	s = fin.readline()

fin.close()

fout.write(str(dy)+'\n')
fout.write(str(dx)+'\n')
flbl.write(str(dy)+'\n')
flbl.write('1\n')

#reopen file
fin =  file(sys.argv[1],'rb')
s = fin.readline()
while not s == "":
	vec = s[:-1].split('\t')
	flbl.write(vec[-1]+'\n')
	vec = vec[:-1]

	for v in vec:
		fout.write(v+'\n')
	s = fin.readline()
fout.close()
fin.close()
flbl.close()

