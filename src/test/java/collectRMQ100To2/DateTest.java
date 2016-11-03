package collectRMQ100To2;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		//String string = "January 2, 2010";
		String str="7/29/2015 3:33:25 PM";
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH);
		Date date = format.parse(str);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		//Date date1 = new Date();
		String aa = dateFormat.format(date);
		System.out.println(aa);
		
	}

}
