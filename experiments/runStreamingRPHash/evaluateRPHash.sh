#!/bin/bash

cat dataset.csv | rev | cut -d, -f2- | rev >unlabeledDataset.csv   # Remove the class labels from dataset.csv
mv unlabeledDataset.csv 2D.csv
python 2D_to_1D.py 2D.csv
java -jar RPHash.jar 1D.txt 10 out streaming parallel=true dimparameter=16 decodertype=sphere numProjections=7 numBlur=4 offlineclusterer=kmeans streamduration=1000
#java -jar RPHash.jar 1D.txt 6 out streamingkmeans streamduration=1000
python SplitInputFile.py 1D.txt

n=20  # Set the number of horizons

# Run streaming LabelData 20 times
for i in $(seq 1 $n)
do
  java -jar LabelData.jar out_round$(($i*1000)).RPHashStream SplitInputFile$i.txt RPHashLabels$i.txt
  #java -jar LabelData.jar out_round$(($i*1000)).StreamingKmeans SplitInputFile$i.txt SKMLabels$i.txt
done
#java -jar LabelData.jar out_round19996.RPHashStream SplitInputFile20.txt RPHashLabels20.txt

paste -d, RPHashLabels{1..20}.txt > RawRPHashLabels.csv
python remTop2AddOne.py RawRPHashLabels.csv RPHashLabels.csv
#paste -d, SKMLabels{1..20}.txt > SKMLabels.csv

rm *.txt *.RPHashStream 2D.csv RawRPHashLabels.csv
Rscript ScalabilityTest_StreamingRPHash.R
