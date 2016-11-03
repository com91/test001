package collectRMQ100To2;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class sh1md {

	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub

		String appid="BSMuser001";
		String appkey="smartcoder12345";
		String S1=appid+"|"+appkey;
		String S2=S1.toLowerCase();
		byte[] S3=S2.getBytes("utf-8");
		MessageDigest md = MessageDigest.getInstance("SHA-1"); 
		byte[] S4=md.digest(S3);
		 Formatter formatter = new Formatter();
		    for (int i=0;i<S4.length;i++) {
		        formatter.format("%02x", S4[i]);
		    }
		String S5= formatter.toString();
		System.out.println("[S1]"+S1);
		System.out.println("[S2]"+S2);
		System.out.println("[S3]"+S3.toString());
		System.out.println("[S4]"+S4.toString());
		System.out.println("[S5]"+S5);

	}

}
