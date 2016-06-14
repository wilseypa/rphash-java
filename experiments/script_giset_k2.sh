#!/bin/bash


for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=cauchypstable numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelcauchypstableD$(($l*8))p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py LabelcauchypstableD$(($l*8))p$(($k))b$(($j))sl.csv RPHash,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelcauchypstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=cauchypstable numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelcauchypstableD$(($l*8))p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py LabelcauchypstableD$(($l*8))p$(($k))b$(($j))cl.csv RPHash,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelcauchypstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=cauchypstable numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelcauchypstableD$(($l*8))p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py LabelcauchypstableD$(($l*8))p$(($k))b$(($j))al.csv RPHash,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelcauchypstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=cauchypstable numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelcauchypstableD$(($l*8))p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py LabelcauchypstableD$(($l*8))p$(($k))b$(($j))km.csv RPHash,cauchypstable,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelcauchypstableD$(($l*8))*.csv
 		done 
	done 
done



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=levypstable numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabellevypstableD$(($l*8))p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,levypstable,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py LabellevypstableD$(($l*8))p$(($k))b$(($j))sl.csv RPHash,levypstable,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabellevypstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=levypstable numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabellevypstableD$(($l*8))p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,levypstable,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py LabellevypstableD$(($l*8))p$(($k))b$(($j))cl.csv RPHash,levypstable,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabellevypstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=levypstable numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabellevypstableD$(($l*8))p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,levypstable,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py LabellevypstableD$(($l*8))p$(($k))b$(($j))al.csv RPHash,levypstable,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabellevypstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=levypstable numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabellevypstableD$(($l*8))p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,levypstable,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py LabellevypstableD$(($l*8))p$(($k))b$(($j))km.csv RPHash,levypstable,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabellevypstableD$(($l*8))*.csv
 		done 
	done 
done 


for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=gaussianpstable numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))sl.csv RPHash,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelgaussianpstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=gaussianpstable numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))cl.csv RPHash,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelgaussianpstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=gaussianpstable numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))al.csv RPHash,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelgaussianpstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=gaussianpstable numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py LabelgaussianpstableD$(($l*8))p$(($k))b$(($j))km.csv RPHash,gaussianpstable,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelgaussianpstableD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=sphere numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelsphereD$(($l*8))p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,sphere,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py LabelsphereD$(($l*8))p$(($k))b$(($j))sl.csv RPHash,sphere,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelsphereD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=sphere numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelsphereD$(($l*8))p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,sphere,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py LabelsphereD$(($l*8))p$(($k))b$(($j))cl.csv RPHash,sphere,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelsphereD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=sphere numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelsphereD$(($l*8))p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,sphere,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py LabelsphereD$(($l*8))p$(($k))b$(($j))al.csv RPHash,sphere,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelsphereD$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=10 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  dimparameter=$(($l*8)) decodertype=sphere numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelsphereD$(($l*8))p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,sphere,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py LabelsphereD$(($l*8))p$(($k))b$(($j))km.csv RPHash,sphere,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelsphereD$(($l*8))*.csv
 		done 
	done 
done 




for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multileech numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelmultileechD$(($l*24))p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multileech,D$(($l*24)),p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py LabelmultileechD$(($l*24))p$(($k))b$(($j))sl.csv RPHash,multileech,D$(($l*24)),p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelmultileechD$(($l*24))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multileech numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelmultileechD$(($l*24))p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multileech,D$(($l*24)),p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py LabelmultileechD$(($l*24))p$(($k))b$(($j))cl.csv RPHash,multileech,D$(($l*24)),p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelmultileechD$(($l*24))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multileech numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelmultileechD$(($l*24))p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multileech,D$(($l*24)),p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py LabelmultileechD$(($l*24))p$(($k))b$(($j))al.csv RPHash,multileech,D$(($l*24)),p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelmultileechD$(($l*24))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multileech numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > LabelmultileechD$(($l*24))p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multileech,D$(($l*24)),p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py LabelmultileechD$(($l*24))p$(($k))b$(($j))km.csv RPHash,multileech,D$(($l*24)),p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv LabelmultileechD$(($l*24))*.csv
 		done 
	done 
done 




for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multie8 numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelmultie8D$(($l*8))p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multie8D,$(($l*8)),p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py Labelmultie8D$(($l*8))p$(($k))b$(($j))sl.csv RPHash,multie8,D$(($l*8)),p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelmultie8D$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multie8 numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelmultie8D$(($l*8))p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multie8,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py Labelmultie8D$(($l*8))p$(($k))b$(($j))cl.csv RPHash,multie8,D$(($l*8)),p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelmultie8D$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multie8 numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelmultie8D$(($l*8))p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multie8,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py Labelmultie8D$(($l*8))p$(($k))b$(($j))al.csv RPHash,multie8,D$(($l*8)),p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelmultie8D$(($l*8))*.csv
 		done 
	done 
done 



for (( l=1 ; l<=4 ; l++))
do	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true  innerdecodermultiplier=$l decodertype=multie8 numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelmultie8D$(($l*8))p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,multie8,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py Labelmultie8D$(($l*8))p$(($k))b$(($j))km.csv RPHash,multie8,D$(($l*8)),p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelmultie8D$(($l*8))*.csv
 		done 
	done 
done 







	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=leech numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelleechp$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,leech,D24,p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py Labelleechp$(($k))b$(($j))sl.csv RPHash,leech,D24,p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelleech*.csv
 		done 
	done 
 


	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=leech numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelleechp$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,leech,D24,p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py Labelleechp$(($k))b$(($j))cl.csv RPHash,leech,D24,p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelleech*.csv
 		done 
	done 




	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=leech numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelleechp$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,leech,D24,p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py Labelleechp$(($k))b$(($j))al.csv RPHash,leech,D24,p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelleech*.csv
 		done 
	done 




	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=leech numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labelleechp$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,leech,D24,p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py Labelleechp$(($k))b$(($j))km.csv RPHash,leech,D24,p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labelleech*.csv
 		done 
	done 
 




	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=e8 numprojections=$k numblur=$j offlineclusterer=singlelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labele8p$(($k))b$(($j))sl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,e8,D8,p$(($k)),b$(($j)),sl.csv

				python remTop2AddOne.py Labele8p$(($k))b$(($j))sl.csv RPHash,e8,D8,p$(($k)),b$(($j)),sl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labele8*.csv
 		done 
	done 
 


	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=e8 numprojections=$k numblur=$j offlineclusterer=completelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labele8p$(($k))b$(($j))cl.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,e8,D8,p$(($k)),b$(($j)),cl.csv

				python remTop2AddOne.py Labele8p$(($k))b$(($j))cl.csv RPHash,e8,D8,p$(($k)),b$(($j)),cl.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labele8*.csv
 		done 
	done 




	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=e8 numprojections=$k numblur=$j offlineclusterer=averagelink
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labele8p$(($k))b$(($j))al.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,e8,D8,p$(($k)),b$(($j)),al.csv

				python remTop2AddOne.py Labele8p$(($k))b$(($j))al.csv RPHash,e8,D8,p$(($k)),b$(($j)),al.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labele8*.csv
 		done 
	done 




	
	for (( k=1 ; k<=6 ; k++))
	do		

		for (( j=1 ; j<=4 ; j++))
		do


			for (( i=1 ; i<=6 ; i++))

   			do
    				java -jar RPHash18.jar  1D.txt 2 out$i multiproj   parallel=true   decodertype=e8 numprojections=$k numblur=$j offlineclusterer=kmeans
				mv metrics_time_memkb_wcsse.csv metrics_time_memkb_wcsse$i.csv

				java -jar LabelData.jar  out$i.RPHashMultiProj 1D.txt label$i.txt

			done 

				paste -d, label{1..6}.txt > Labele8p$(($k))b$(($j))km.csv

				paste -d, metrics_time_memkb_wcsse{1..6}.csv > outmeasures,e8,D8,p$(($k)),b$(($j)),km.csv

				python remTop2AddOne.py Labele8p$(($k))b$(($j))km.csv RPHash,e8,D8,p$(($k)),b$(($j)),km.csv

				rm label{1..6}.txt *.RPHashMultiProj metrics_time_memkb_wcsse{1..6}.csv Labele8*.csv
 		done 
	done 

 
