package edu.uc.rphash.aging;

public class Decay implements Runnable {

	
	// public double value;   
	public double t;
	public double decayRate;
	
	
	@Override
	public void run() {
		
	}	

public static double  ExpDecayFormula ( Number halfLifeInSeconds , float t ) {

        Double decayRate = - Math.log(2) / halfLifeInSeconds.longValue() / 1000;

        Double expMultiplier =  Math.pow(Math.E, decayRate * t);
        return expMultiplier;
        
    }

public static double  LinearDecayFormula ( Number lifeTimeInSeconds , float t ) {

	 
    Double lifeTime = Double.valueOf(lifeTimeInSeconds.longValue()) * 1000;
    
    if (t < 0 || t > lifeTime ) {
    	Double linearMultiplier =  -0.1;   // explain
    	return linearMultiplier;
    }
    else {
    	Double linearMultiplier =(1 - t / lifeTime);
    	return linearMultiplier;
    }

}

public static double LogDecayFormula (long lifeTimeInSeconds , float t)  {

    
        Double lifeTime = Double.valueOf(lifeTimeInSeconds) * 1000;

        if (t < 0 || t >= lifeTime ) {
            return 0.0;
        } else {
            // return value + 1 - Math.pow(Math.E, Math.log(value + 1)/lifeTime*t);
        	return lifeTime;
        }
    }

}