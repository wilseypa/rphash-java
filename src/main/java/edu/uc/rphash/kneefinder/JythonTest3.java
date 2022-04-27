package edu.uc.rphash.kneefinder; 
/*
 * 
 * import org.python.util.PythonInterpreter;
 * import org.python.core.PyInstance;
 * 
 * import java.io.BufferedReader; import java.io.InputStreamReader;
 * 
 * import org.python.core.*;
 * 
 * class JythonTest3 {
 * 
 * //// does not work if there are external imports:
 * 
 * // public static void main(String[] args) { // PythonInterpreter interpreter
 * = new PythonInterpreter(); // // interpreter.execfile(
 * "C:\\Users\\sayan\\eclipse-workspace\\pythonfunc\\pythonfunc1.py"); //
 * PyFunction function =
 * (PyFunction)interpreter.get("my_test",PyFunction.class); // PyObject pyobject
 * = function.__call__(new PyString("huzhiweiww"),new PyString("2225")); //
 * System.out.println("anwser = " + pyobject.toString()); // } //
 * 
 * static PythonInterpreter interpreter;
 * 
 * @SuppressWarnings("resource") public static void main( String gargs[] ) {
 * //String[] s = {"New York", "Chicago" , "errr"}; int[] s = new int[]
 * {1,2,3,4,5, 6,7,8,9,10,11,12,13,14,15,16,17,18 ,19,20,21};
 * PythonInterpreter.initialize(System.getProperties(),System.getProperties(),
 * s); interpreter = new PythonInterpreter(); interpreter.execfile(
 * "C:\\Users\\sayan\\git\\rphash-java\\src\\main\\java\\edu\\uc\\rphash\\kneefinder\\PyScript.py"
 * ); PyInstance hello = (PyInstance) interpreter.eval("PyScript" + "(" + "None"
 * + ")"); }
 * 
 * public void getData(Object[] data) { for (int i = 0; i < data.length; i++) {
 * System.out.print(data[i].toString()); }
 * 
 * } }
 * 
 */

     import org.python.util.PythonInterpreter;                       
     import org.python.core.*;                                       
                                                                     
     public class JythonTest3 {                               
     	public static void main(String a[]){                           
     	                                                               
     		PythonInterpreter python = new PythonInterpreter();           
     		                                                              
     		int number1 = 5;                                              
     		int number2 = 6;                                              
     		                                                              
     		python.set("number1", new PyInteger(number1));                
     		python.set("number2", new PyInteger(number2));                
     		python.exec("number3 = number1+number2");                     
     		PyObject number3 = python.get("number3");                     
     		System.out.println("Returned Value is : "+number3.toString());
     	}                                                              
     }                                                               