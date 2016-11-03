package collectRMQ100To2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class unzipstringtest {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub

		String str="";
		String fileName="C://Users//cz//Desktop//sample.txt";
		String content = new String(Files.readAllBytes(Paths.get(fileName)));
		unzipstringtest unzip=new unzipstringtest();
		System.out.println(content);
		System.out.println(unzip.toUnzipString(content));
		
	}
	public String toUnzipString(String str) throws IOException
	{
		InputStream is = new ByteArrayInputStream( str.getBytes( "utf-8" ) );
		InputStream in = new GZIPInputStream(is);
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    int len1;
	    byte[] buffer1 = new byte[1024];
	    while ((len1 = in.read(buffer1)) != -1) {
	        byteArrayOutputStream.write(buffer1, 0, len1);
	    }
	    in.close();
	    byteArrayOutputStream.close();
	    String unzipstr = new String(byteArrayOutputStream.toByteArray(), "utf-8");
		return unzipstr;
	}
}
