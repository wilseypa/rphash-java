package edu.uc.rphash.kneefinder;
import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.python.core.*;

class JythonTest2 
{
	
//// does not work if there are external imports:
	
//	public static void main(String[] args) {
//	      PythonInterpreter interpreter = new PythonInterpreter(); 
//
//	      interpreter.execfile("C:\\Users\\sayan\\eclipse-workspace\\pythonfunc\\pythonfunc1.py");  
//	      PyFunction function = (PyFunction)interpreter.get("my_test",PyFunction.class);  
//	      PyObject pyobject = function.__call__(new PyString("huzhiweiww"),new PyString("2225")); 
//	      System.out.println("anwser = " + pyobject.toString());  
//	    }
//	
	
	
	
	public static void main(String[] args) {
		
		
		
		// xarray_1 =
		// yarray_2= 
/*	      String[] arguments = new String[] {"python", "C:\\Users\\sayan\\eclipse-workspace\\pythonfunc\\pythonfunc2.py" , "huzhiwei", "25", "C:/Users/sayan/Documents/testdata/data.xlsx"};
	        try {
	            Process process = Runtime.getRuntime().exec(arguments);
	            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            String line = null;  
	          while ((line = in.readLine()) != null) {  
	              System.out.println(line);  
	          }  
	          in.close();  
	          int re = process.waitFor();  
	          System.out.println(re);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }  
*/
	        String[] arguments2 = new String[] {"python", "C:\\Users\\sayan\\git\\rphash-java\\src\\main\\java\\edu\\uc\\rphash\\kneefinder\\KneeLocator.py" , "huzhiwei", "25", "C:/Users/sayan/Documents/testdata/data.xlsx"};
	        try {
	            Process process = Runtime.getRuntime().exec(arguments2);
	            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            String line = null;  
	          while ((line = in.readLine()) != null) {  
	              System.out.println(line);  
	          }  
	          in.close();  
	          int re = process.waitFor();  
	          System.out.println(re);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 

	        int[] int_array_x = new int[] {1,2,3,4,5, 6,7,8,9,10,11,12,13,14,15,16,17,18 ,19,20,21};
	        float[] float_array_y =  new float[] {5000,4000,3000,2000,1000,900,800,700,600,500,450,400,350,300,250,225,200,175,150,125,100};
	        
	        String[] arguments3 = new String[] {"python", "C:\\Users\\sayan\\git\\rphash-java\\src\\main\\java\\edu\\uc\\rphash\\kneefinder\\KneeLocator.py" , "huzhiwei", "25", "C:/Users/sayan/Documents/testdata/data.xlsx"};
	        try {
	            Process process = Runtime.getRuntime().exec(arguments2);
	            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            String line = null;  
	          while ((line = in.readLine()) != null) {  
	              System.out.println(line);  
	          }  
	          in.close();  
	          int re = process.waitFor();  
	          System.out.println(re);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
           
           
	        
	    }
}
