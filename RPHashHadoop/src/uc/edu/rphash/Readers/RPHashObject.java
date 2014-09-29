package uc.edu.rphash.Readers;

public interface RPHashObject {
	int k = 0;
	int n = 0;
	int dim = 0;
	int randomseed = 0;
	int hashmod = 0 ;
	float[] getNextVector();

}
