package regminer.util;

import java.io.*;


/**
 * Debug.java, 2011. 12. 5.
 */

/**
 * @author Dong-Wan Choi
 * @date 2011. 12. 5.
 * @date 2013. 12. 24 updated to write logs in the DEBUG mode
 */
public class Debug {
	
	public static boolean flag = false;
	public static String logFileName = Env.HomeDir + "/logs/minsk.log";
	
	public static void _PrintL(String str) {
		if (flag) System.out.println(str);
		else {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
				out.println(str);
				out.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void _Print(Object str) {
		if (flag) System.out.print(str);
		else {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
				out.print(str);
				out.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void _Error(Object object, Object str) {
		if (object != null)
			if (flag) System.err.println("Error:" + str + " in "+object.getClass().getSimpleName());
			else {
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
					out.print(str);
					out.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		else
			if (flag) System.err.println("Error:" + str);
			else {
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
					out.print(str);
					out.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
}
