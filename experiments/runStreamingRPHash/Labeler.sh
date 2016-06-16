#!/bin/bash

mv dataset.csv 2D.csv
python 2D_to_1D.py 2D.csv
java -jar RPHash.jar 1D.txt 6 out streaming NumProjections=2 NumBlur=4 streamduration=1000
java -jar RPHash.jar 1D.txt 6 out streamingkmeans streamduration=1000
python SplitInputFile.py 1D.txt

n=20  #Set the number of horizons

# Run streaming LabelData 20 times
for i in $(seq 1 $n)
do
  java -jar LabelData.jar out_round$(($i*1000)).RPHashStream SplitInputFile$i.txt RPHashLabels$i.txt
  java -jar LabelData.jar out_round$(($i*1000)).StreamingKmeans SplitInputFile$i.txt SKMLabels$i.txt
done

paste -d, RPHashLabels{1..20}.txt > RPHashLabels.csv
paste -d, SKMLabels{1..20}.txt > SKMLabels.csv

rm *.txt *.RPHashStream *.StreamingKmeans 2D.csv
