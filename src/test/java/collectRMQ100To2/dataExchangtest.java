package collectRMQ100To2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import com.sun.xml.bind.v2.schemagen.xmlschema.List;




public class dataExchangtest {

	public static void main(String args[])
	{

		  try  
		  {
			  String line=null;
			  String str="";
			  FileReader fileReader = 
		                new FileReader("C:\\Users\\yjiang\\Desktop\\new2.txt");

		            // Always wrap FileReader in BufferedReader.
		            BufferedReader bufferedReader = 
		                new BufferedReader(fileReader);

		            while((line = bufferedReader.readLine()) != null) {
		              //  System.out.println(line);
		                str=str+line;
		            }   

		            // Always close files.
		            System.out.println(str);
		            bufferedReader.close();    
			
		            byte[] base64decodedBytes = Base64.getDecoder().decode(str);
		            
		            String oriStr=new String(base64decodedBytes, "utf-8");
		    		
		            System.out.println("Original String: " + oriStr);
		           
		            ByteArrayInputStream bis = new ByteArrayInputStream(base64decodedBytes);
		           // java.io.StringBufferInputStream sbi=new StringBufferInputStream(oriStr);
		            //DataInputStream  ois = new DataInputStream(bis);
		            ObjectInputStream ois1=new ObjectInputStream(bis);


//		            java.util.List<HashMap<String, String>> juL=(java.util.List<HashMap<String, String>>) ois.readObject();
		            
//		   FileInputStream fis = new FileInputStream("C:\\Users\\yjiang\\Desktop\\new2.txt");
//		   ObjectInputStream ois = new ObjectInputStream(fis);
//		   System.out.println(ois==null?"ois null":"ois not null");
//		   String sk;
//		   sk = (String) ois.readObject();
//		   System.out.println(sk);
		  } 
		  catch (Exception e)
		   { e.printStackTrace(); }
		  //System.out.println(si==null?"null":"not null");

		 }
	}

