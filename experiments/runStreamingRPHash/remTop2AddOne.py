import sys
infile = file(sys.argv[1])
outfile = file(sys.argv[2],'w')
infile.readline()
infile.readline()
row = infile.readline()
while not row=='':
    vals = row[:-1].split(',')
    for val in vals[:-1]:
        outfile.write(str(int(float(val))+1)+',')
    outfile.write(str(int(float(vals[-1]))+1))
    outfile.write('\n')
    row = infile.readline()

