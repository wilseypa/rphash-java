package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;

public class KMeans2 implements Clusterer {

	class PointND
	{

	  private int dimension;                // number of coordinates of a point
	  private float[] coordinates;         // the coordinates of a point

	  /**
	   * Create a point centered at the origin of the specific dimension
	  **/
	  public PointND(int dimension)
	  {
	    this.dimension=dimension;
	    coordinates=new float[dimension];
	  }

	  
	  public PointND(float[] data)
	  {
	    this.dimension=data.length;
	    coordinates=data;
	  }
	  /**
	   * Create a new point identical to point p
	  **/
	  public PointND(PointND p)
	  {
	    this.dimension=p.getDimension();
	    this.coordinates=new float[dimension];
	    for (int i=0;i<dimension;i++)
	      this.coordinates[i]=p.getCoordinate(i);
	  }

	  /**
	   * Create a new point identical to point p
	  **/
	  public void setToOrigin()
	  {
	    for (int i=0;i<dimension;i++)
	      coordinates[i]=0;
	  }

	  /**
	   * Return the euclidian norm of this point
	  **/
	  public float norm()
	  {
	    float sum=0;
	    for (int i=0;i<dimension;i++)
	      sum=(float) (sum + Math.pow(coordinates[i],2));
	    return (float) Math.sqrt(sum);
	  }

	  /**
	   * Add point p to this point
	  **/
	  public void add(PointND p)
	  {
	    for (int i=0;i<dimension;i++)
	      coordinates[i]=coordinates[i] + p.getCoordinate(i);
	  }

	  /**
	   * Subtract point p to this point
	  **/
	  public void subtract(PointND p)
	  {
	    for (int i=0;i<dimension;i++)
	      coordinates[i]=coordinates[i] - p.getCoordinate(i);
	  }

	  /**
	   * Multiply this point by a scalar
	  **/
	  public void multiply(float scalar)
	  {
	    for (int i=0;i<dimension;i++)
	      coordinates[i]=scalar * coordinates[i];
	  }

	  /**
	   * Exponentiate this point by exp
	  **/
	  public void pow(float exp)
	  {
	    for (int i=0;i<dimension;i++)
	      coordinates[i]=(float) Math.pow(coordinates[i],exp);
	  }

	  /**
	   * Compute the euclidian distance of this point to point p2
	  **/
	  public float dist(PointND p2)
	  {
	    PointND p1=new PointND(this);
	    p1.subtract(p2);
	    return p1.norm();
	  }

	  /**
	   * Return the coordinate of maximum value of this point
	  **/
	  public float max()
	  {
	    float value;
	    float max=coordinates[0];
	    for (int i=1;i<dimension;i++)
	    {
	      value=coordinates[i];
	      if (value > max)
	        max=value;
	    }
	    return max;
	  }

	 /**
	  * Return the probability of this point given it is normally distributed with a
	  * a diagonal covariance matrix of coefficients sigma
	 **/
	 public float normal(PointND mean,PointND sigma)
	  {
	    float mahalanobis;
	    float productSigma=1;
	    PointND temp=new PointND(this);
	    temp.subtract(mean);
	    // compute the product of the deviations and the mahalanobis distance
	    for (int i=0;i<dimension;i++)
	    {
	      productSigma=sigma.getCoordinate(i) * productSigma;
	      temp.setCoordinate( i, temp.getCoordinate(i)/sigma.getCoordinate(i) );
	    }
	    mahalanobis=(float) Math.pow(temp.norm(),2);
	    return (float) ( 1.0f/( Math.pow((2*Math.PI),dimension/2.0f) * productSigma )
	            * Math.exp(-1.0f/2 * mahalanobis) );
	  }

	  /**
	   * Return coordinate i of this point
	  **/
	  public float getCoordinate(int i)
	  {
	    return coordinates[i];
	  }

	  /**
	   * Set coordinate[i] to the specified value
	  **/
	  public void setCoordinate(int i,float value)
	  {
	    coordinates[i]=value;
	  }

	  /**
	   * Return the number of coordinates of this point
	  **/
	  public int getDimension()
	  {
	    return dimension;
	  }

	  /**
	   * Return the coordinate array of this point
	  **/
	  public float[] getCoordinates()
	  {
	    return coordinates;
	  }

	  /**
	   * Return a string representation of this point
	  **/
	  public String toString()
	  {
	    String s="" + coordinates[0];
	    for (int i=1;i<dimension;i++)
	      s=s + " " + coordinates[i];
	    return s;
	  }



	}
	
	  private int n;                          // number of instances to classify
	  private int d;                          // number of coordinates of each point
	  private int k;                          // number of clusters
	  private PointND[] mu;                   // coordinate of means mu[j] of each cluster j
	  private Vector[] w;                     // holds the points classified into each class w[j]
	  private PointND[] sigma;                // holds the standard deviation of each class i
	  private float[] prior;                 // holds the prior of each class i
	  private float logLikelihood;           // holds the log likelihood of each of the k Gaussians
	  private float MDL;                     // the minimum description length of the model
	  private int numIterations;

	  private List<float[]> centroids;


	  public KMeans2(int getk, List<float[]> centroids) {
		  PointND[] data = new PointND[centroids.size()];
		  for(int i = 0;i<centroids.size();i++){
			  data[i] = new PointND(centroids.get(i));
		  }
		  this.centroids = null;
		  init(data,getk);
	}

	/**
	   * Intialize the parameters of the k-means algorithm
	   * Randomly assign a point in x to each mean mu[j]
	  **/
	  private void init(PointND[] x,int k)
	  {
	    this.n=x.length;
	    this.d=x[0].getDimension();
	    this.k=k;
	    this.mu=new PointND[k];
	    this.w=new Vector[k];
	    this.numIterations=0;
	    this.sigma=new PointND[k];
	    this.prior=new float[k];

	    // randomly assign a point in x to each mean mu[j]
	    PointND randomPoint;
	    for (int j=0;j<k;j++)
	    {
	      randomPoint=x[(int)(Math.random()*(n-1))];
	      mu[j]=new PointND(randomPoint);
	      // each prior and standard deviation are set to zero
	      sigma[j]=new PointND(d);
	      prior[j]=0;
	    }
	  }

	  /**
	   * Runs the k-means algorithm with k clusters on the set of instances x
	   * Then find the quality of the model
	  **/
	  public void run(PointND[] x,int k,float epsilon)
	  {
	    float maxDeltaMeans=epsilon+1;
	    PointND[] oldMeans=new PointND[k];
	    // initialize n,k,mu[j]
	    init(x,k);
	    // iterate until there is no change in mu[j]
	    while (maxDeltaMeans > epsilon)
	    {
	      // remember old values of the each mean
	      for (int j=0;j<k;j++)
	      {
	        oldMeans[j]=new PointND(mu[j]);

	      }

	      // classify each instance x[i] to its nearest class
	      // first we need to clear the class array since we are reclassifying
	      for (int j=0;j<k;j++)
	      {
	        w[j]=new Vector();        // could use clear but then have to init...
	      }

	      for (int i=0;i<n;i++)
	      {
	        classify(x[i]);
	      }
	      // recompute each mean
	      computeMeans();
	      // compute the largest change in mu[j]
	      maxDeltaMeans=maxDeltaMeans(oldMeans);
	      numIterations++;
	    }
	  }




	  /**
	   * Classifies the point x to the nearest class
	  **/
	  private void classify(PointND x)
	  {
	    float dist=0;
	    float smallestDist;
	    int nearestClass;

	    // compute the distance x is from mean mu[0]
	    smallestDist=x.dist(mu[0]);
	    nearestClass=0;

	    // compute the distance x is from the other classes
	    for(int j=1;j<k;j++)
	    {
	      dist=x.dist(mu[j]);
	      if (dist<smallestDist)
	      {
	        smallestDist=dist;
	        nearestClass=j;
	      }
	    }
	    // classify x into class its nearest class
	    w[nearestClass].add(x);
	  }

	  /**
	   * Recompute mu[j] as the average of all points classified to the class w[j]
	  **/
	  private void computeMeans()
	  {
	    int numInstances;               // number of instances in each class w[j]
	    PointND instance;

	    // init the means to zero
	    for (int j=0;j<k;j++)
	      mu[j].setToOrigin();

	    // recompute the means of each cluster
	    for (int j=0;j<k;j++)
	    {
	      numInstances=w[j].size();
	      for (int i=0;i<numInstances;i++)
	      {
	        instance=(PointND) (w[j].get(i));
	        mu[j].add(instance);
	      }
	      mu[j].multiply(1.0f/numInstances);
	    }
	    
	  }

	  /**
	   * Compute the maximum change over each mean mu[j]
	  **/
	  private float maxDeltaMeans(PointND[] oldMeans)
	  {
	    float delta;
	    oldMeans[0].subtract(mu[0]);
	    float maxDelta=oldMeans[0].max();
	    for (int j=1;j<k;j++)
	    {
	      oldMeans[j].subtract(mu[j]);
	      delta=oldMeans[j].max();
	      if (delta > maxDelta)
	        maxDelta=delta;
	    }
	    return maxDelta;
	  }


	  /**
	   * Compute the standard deviation of the k Gaussians
	  **/
	  private void computeDeviation()
	  {
	    int numInstances;               // number of instances in each class w[j]
	    PointND instance;
	    PointND temp;

	    // set the standard deviation to zero
	    for (int j=0;j<k;j++)
	      sigma[j].setToOrigin();

	    // for each cluster j...
	    for (int j=0;j<k;j++)
	    {
	      numInstances=w[j].size();
	      for (int i=0;i<numInstances;i++)
	      {
	        instance=(PointND) (w[j].get(i));
	        temp=new PointND(instance);
	        temp.subtract(mu[j]);
	        temp.pow(2.0f);                        // (x[i]-mu[j])^2
	        temp.multiply(1.0f/numInstances);      // multiply by proba of having x[i] in cluster j
	        sigma[j].add(temp);                   // sum i (x[i]-mu[j])^2 * p(x[i])
	      }
	      sigma[j].pow( (1.0f/2f));                    // because we want the standard deviation
	    }
	  }

	  /**
	   * Compute the priors of the k Gaussians
	  **/
	  private void computePriors()
	  {
	    float numInstances;               // number of instances in each class w[j]
	    for (int j=0;j<k;j++)
	    {
	      numInstances=w[j].size()*(1.0f);
	      prior[j]=numInstances/n;
	    }
	  }

	  /**
	   * Assume the standard deviations and priors of each cluster have been computed
	  **/
	  private void computeLogLikelihood(PointND[] x)
	  {
	    float temp1=0;
	    float temp2=0;
	    PointND variance;
	    float ln2=(float) Math.log(2);
	    // for each instance x
	    for (int i=0;i<n;i++)
	    {
	      // for each cluster j
	      temp1=0;
	      for (int j=0;j<k;j++)
	      {
	        temp1=temp1 + ( x[i].normal(mu[j],sigma[j]) *  prior[j] );
	      }
	      temp2=(float) (temp2 + Math.log(temp1)/ln2);
	    }
	    logLikelihood=temp2;
	  }

	  /**
	   * Assume the log likelihood and priors have been computed
	  **/
	  private void computeMDL()
	  {
	    float temp=0;
	    float numInstances;
	    float ln2=(float) Math.log(2);
	    for (int j=0;j<k;j++)
	    {
	      numInstances=w[j].size();
	      for (int i=0;i<d;i++)
	      {
	        temp=(float) (temp - Math.log( sigma[j].getCoordinate(i)/Math.sqrt(numInstances) )/ln2);
	      }
	    }
	    MDL=temp - logLikelihood;
	  }

	  public float getMDL()
	  {
	    return MDL;
	  }

	public List<float[]> getCentroids() {
		float epsilon=0.01f;
		if(centroids == null) {
			run(mu, d, epsilon);
			centroids = new ArrayList<float[]>(k);
			for(int i = 0;i<k;i++)centroids.add(mu[i].coordinates);
		}
		
		return centroids;
	}

	@Override
	public RPHashObject getParam() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setWeights(List<Float> counts) {
		
	}

	@Override
	public void setData(List<float[]> centroids) {
	}

	@Override
	public void setK(int getk) {
		this.k = getk;
	}

}
