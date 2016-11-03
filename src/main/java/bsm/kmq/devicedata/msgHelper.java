package bsm.kmq.devicedata;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class msgHelper {

	private static final Logger LOG = LoggerFactory
			.getLogger(msgHelper.class);
	private static int num=0;
	
	private String provider="BSW".toUpperCase();
	
	/**
	 * Kafka Message Queue Data Save To Header "KMQ_QUEUE_DATA"*/
	public void Save_QueueDataToHeader(Exchange exchange) throws Exception
	{
		try
		{
			/*for Test Record count*/
			num++;
			System.out.println("[Count:"+num+"]");
			/* end test*/
			
			String body=exchange.getIn().getBody(String.class);
			exchange.getIn().setHeader("KMQ_QUEUE_DATA", body);
			
			//body =DecodeBase64String(body);
		    //exchange.getIn().setBody(body);
		} 
		catch (Exception e) {
			LOG.error("Exception in KMQtoWebservice Save_QueueDataToHeader call");
			LOG.error(e.toString());
			throw (e);
		}
	}
	/**
	 * Prepare Web service Parameters for PROBE Project
	 * @throws Exception 
	 * Part 1: prepare Web service Parameters (http header part)
	 * Part 2: prepare JSON data ( http body part)*/
	public void Assemble_HttpPostByPROBE(Exchange exchange) throws Exception
	{
		try
		{
			Message msg=exchange.getIn();
			String rowStr=msg.getBody(String.class);
			Map<String,String> params= Convert_SqlDBToMap(rowStr);
			String protocol=params.get("ServiceProtocol");
			String domain=params.get("Description");
			String ServiceUrl=params.get("ServiceUrl");
			String username=params.get("UserName");
			String password=params.get("UserPassword");
			String AuthorizationStr=domain+":"+username+":"+password;
			String AuthorizationEncode=toEncodeBase64String(AuthorizationStr);
			
			if (protocol == null)
				protocol = "http4://";
			if (protocol.equalsIgnoreCase("https"))
				protocol = "https4://";
			else
				protocol = "http4://";
			String url=protocol+ServiceUrl;
			
			/*part 1: header*/
			msg.setHeader("HTTP_RECEPIENT_LIST", url);
			msg.setHeader("CamelHttpMethod", "POST");
			msg.setHeader("Authorization ", AuthorizationEncode);
			msg.setHeader("Accept", "application/json");
			msg.setHeader("Accept-Encoding","gzip");
			msg.setHeader("Content-type","application/json");
			
			/*part 2: body*/
			String KMQ_QUEUE_DATA=(String) msg.getHeader("KMQ_QUEUE_DATA");
			//String RawRecord=toDecodeBase64String(KMQ_QUEUE_DATA);//use it where KMQ_QUEUE_DATA is base64 format
			String RawRecord=KMQ_QUEUE_DATA; //KMQ_QUEUE_DATA is string,no encoded
			Map<String,String> mapData= Convert_QueuedataToMap(RawRecord);
			String HttpBody=Get_Probe_HttpBody_JsonsimpleFormat(mapData); 
			msg.setBody(HttpBody);
		} 
		catch (Exception e) {
			LOG.error("Exception in KMQtoWebservice Assemble_HttpPostByPROBE call");
			LOG.error(e.toString());
			throw (e);
		}
		
	}
	public void Assemble_HttpLoginByXFCD(Exchange exchange) throws Exception
	{
		try {
			
			Message msg=exchange.getIn();
			String rowStr=msg.getBody(String.class);
			Map<String,String> params= Convert_SqlDBToMap(rowStr);
			String protocol=params.get("ServiceProtocol");
			String URLs=params.get("ServiceUrl");
			String AppID=params.get("UserName");
			String AppKey=params.get("UserPassword");
			String LoginUrl=Get_Xfcd_HttpUrl(URLs,"login");
			String PostUrl=Get_Xfcd_HttpUrl(URLs,"post"); //need to save in header
			String LoginUrlFull=LoginUrl+"?appId={guid}&hashToken={encodedValue}";
			String encodedValue=Get_Xfcd_encodeValue(AppID,AppKey);
			LoginUrlFull.replace("{guid}", AppID);
			LoginUrlFull.replace("{encodedValue}", encodedValue);
			
			if (protocol == null)
				protocol = "http4://";
			if (protocol.equalsIgnoreCase("https"))
				protocol = "https4://";
			else
				protocol = "http4://";
			
			/*part 1: header*/
			msg.setHeader("HTTP_RECEPIENT_LIST", protocol+LoginUrlFull);
			msg.setHeader("CamelHttpMethod", "GET");
			
			/*for post url using,this time is unused,only keep.*/
			msg.setHeader("KMQ_XFCD_POSTURL",PostUrl);
			msg.setHeader("KMQ_XFCD_PROTOCOL",protocol);
			/*end*/
			
		} 
		catch (Exception e) {
			LOG.error("Exception in KMQtoWebservice Assemble_HttpLoginByXFCD call");
			LOG.error(e.toString());
			throw (e);
		}
	}
	/**
	 * Prepare Web service Parameters for XFCD Project
	 * @throws Exception 
	 * Part 1: Result for Login,get token from Json result.
	 * Part 2: prepare Web service Parameters (http header part)
	 * Part 3: prepare JSON data ( http body part)*/
	public void Assemble_HttpPostByXFCD(Exchange exchange) throws Exception
	{
		try {
			Message msg=exchange.getIn();
			String ResultJsonString=msg.getBody(String.class);
			String PostURL=(String) msg.getHeader("KMQ_XFCD_POSTURL");
			String protocol=(String) msg.getHeader("KMQ_XFCD_PROTOCOL");
			String PostURLFull=PostURL+"?token=<your-token>";
			String your_token=Get_Xfcd_tokenFromJson(ResultJsonString);
			
			PostURLFull.replace("<your-token>", your_token);
			
			/*part 1: header*/
			msg.setHeader("HTTP_RECEPIENT_LIST", protocol+PostURLFull);
			msg.setHeader("CamelHttpMethod", "POST");
			
			/*part 2: body*/
			String KMQ_QUEUE_DATA=(String) msg.getHeader("KMQ_QUEUE_DATA");
			String RawRecord=toDecodeBase64String(KMQ_QUEUE_DATA);
			Map<String,String> mapData= Convert_QueuedataToMap(RawRecord);
			String HttpBody=Get_Xfcd_HttpBody_JsonsimpleFormat(mapData); //????
			msg.setBody(HttpBody);
			
			
		} 
		catch (Exception e) {
			LOG.error("Exception in KMQtoWebservice Assemble_HttpPostByXFCD call");
			LOG.error(e.toString());
			throw (e);
		}

	}
	/**
	 * Process Result Value by PROBE
	 * Get body and Unzip 
	 * Get http code from header
	 * Assemble string for logs 
	 */
	public void Result_HttpResponseByPROBE(Exchange exchange) throws Exception
	{
		try
		{
			/* test 
			 * need test
			 * get body and headers for format print string*/
			InputStream body=exchange.getIn().getBody(InputStream.class);
			String decodedbody=toUnzipString(body);
			Map<String,Object> headers=exchange.getIn().getHeaders();
			Iterator entries = headers.entrySet().iterator();    
			while (entries.hasNext()) {  
			    Map.Entry entry = (Map.Entry) entries.next();  
			    String key = (String)entry.getKey();  
			    String value = (String)entry.getValue();  
			    System.out.println("Key = " + key + ", Value = " + value);  
			}  
			/* end */
			//String httpcode="";
			String httpcode="httpCode:"+exchange.getIn().getHeader("HTTP_RESPONSE_CODE");
			String newbody=",Response:"+decodedbody;
			exchange.getIn().setBody(httpcode+newbody);
		}
		catch (Exception e) {
			LOG.error("Exception in KMQtoWebservice Result_HttpResponseByPROBE call");
			LOG.error(e.toString());
			throw (e);
		}
	}
	/**/
	private String Get_Xfcd_HttpBody_JsonsimpleFormat(Map<String,String> mapData)
	{
		return null;
	}
	private String Get_Xfcd_tokenFromJson(String JsonString)
	{

			String token = "";
			JSONObject objroot = new JSONObject(JsonString);
			JSONObject objresult = objroot.getJSONObject("result");
			token = objresult.getString("token");
			return token;

	}
	/**
	 * Encode hashtoken prepare for web service login in XFCD project 
	 * 1.	Create a string with a value of  AppId|AppKey   - The appKey is a value provided by INRIX.  
	 * 2.	Lowercase the string value 
	 * 3.	Encode the string into a byte array using UTF-8 encoding 
	 * 4.	Use SHA1 to hash the bytes 
	 * 5.	Write the resulting bytes in hexadecimal for the hashToken value.  
	 * @throws Exception 
	 */
	private String Get_Xfcd_encodeValue(String appid,String appkey) throws Exception
	{
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
		return S5;
	}
	/**
	 * split XFCD web service URLs to login URL and post URL*/
	private String Get_Xfcd_HttpUrl(String urls,String type)
	{
		String [] array=Convert_SplitStringtoArray(urls, ";");
		String loginurl;
		String posturl;
		if(array.length<2)
		{
			loginurl=array[0];
			posturl=array[0];
		}
		else
		{
			loginurl=array[0];
			posturl=array[1];
		}
		
		if(type.equals("login"))
		{
			return loginurl;
		}
		else
		{
			return posturl;
		}
	}
	/**
	 * Probe http body json simple format*/
	private String Get_Probe_HttpBody_JsonsimpleFormat(Map<String,String> mapData) throws ParseException
	{
		/* Probe web service json parameters
		 * 
		 * {"provider":[provider],
		 * "pp":[{
		 * "id":"abc-124", 	[y-id]
		 * "h":289,			[y-h]
		 * "s":60,			[y-s]
		 * "x":105.987,		[y-x]
		 * "y":45.456,		[y-y]
		 * "t":"2011-05-26T13:47:34" [y-t]
		 * "a":100,			[n-a]
		 * "hp":10,			[n-hp]
		 * "sa":3,			[n-sa]
		 * "er":10,			[n-er]
		 * "mx":15.123,		[n-mx]
		 * "my":45.123,		[n-my]
		 * "am":1,			[n-am]
		 * "dt":1,			[n-dt]
		 * "ad":"key1=value1,key2=value2,keyN=valueN"	[n-ad]
		 * }]}*/
		
		/* Queue Message record data
		 * 
		 * BoxId==37432;;
		 * OriginDateTime==7/29/2015 3:33:25 PM;;
		 * DateTimeReceived==7/29/2015 3:34:25 PM;;
		 * DclId==15;;
		 * BoxMsgInTypeId==2;;
		 * BoxProtocolTypeId==23;;
		 * CommInfo1==10.217.86.39;;
		 * CommInfo2==4038;;
		 * ValidGps==0;;
		 * Latitude==45.553137;;
		 * Longitude==-73.734702;;
		 * Speed==0;;
		 * Heading==341;;
		 * SensorMask==20492;;
		 * CustomProp==SENSOR_NUM=3;SENSOR_STATUS=ON;Odometer=125633;Analog1=0;Analog2=0;FUEL=0;FFV=1;;;
		 * BlobDataSize==0;;
		 * StreetAddress==480 Boulevard Armand-Frappier, Laval, QC, H7V 4B4;;
		 * SequenceNum==0;;
		 * IsArmed==1;;
		 * NearestLandmark==BSM Laval Office;;
		 * LSD==DS=109.4;SC=SC6;FC=5;;
		 * Processed==;;
		 * SlsDateTime==7/29/2015 3:34:28 PM;;
		 * ID==2756284065;;
		 * RoadSpeed==50;;
		 * IsPostedSpeed==False;;
		 * SpeedZoneType==0;;
		 * Organizationid==480;;
		 * StandardValidGps==0;;*/
		String JsonsimpleFormat="{\"provider\":[provider],\"pp\":[{[y-id][y-h][y-s][y-x][y-y][y-t]}]}";
				
		JsonsimpleFormat.replace("[provider]",toString(provider));
		
		String y_id=mapData.get("BoxId");
		y_id=toString("id")+":"+toString(y_id)+",";
		JsonsimpleFormat.replace("[y-id]",y_id);
		
		String y_h=mapData.get("Heading");
		y_id=toString("h")+":"+y_h+",";
		JsonsimpleFormat.replace("[y-h]",y_h);
		
		String y_s=mapData.get("Speed");
		y_id=toString("s")+":"+y_s+",";
		JsonsimpleFormat.replace("[y-s]",y_s);
		
		String y_x=mapData.get("Longitude");
		y_id=toString("x")+":"+y_x+",";
		JsonsimpleFormat.replace("[y-s]",y_x);
		
		String y_y=mapData.get("Latitude");
		y_id=toString("y")+":"+y_y+",";
		JsonsimpleFormat.replace("[y-s]",y_y);
		
		String y_t=mapData.get("OriginDateTime");
		y_t=toCovertDate(y_t);
		y_id=toString("t")+":"+toString(y_t)+",";
		JsonsimpleFormat.replace("[y-id]",y_t);
		
		JsonsimpleFormat.replace(",}", "}"); //delete last ","
		
		return JsonsimpleFormat;
	}
	/**
	 * UNUSED*/
	private String Get_Probe_HttpBody_JsonfullFormat(Map<String,String> mapData) throws ParseException
	{
		/* Probe web service json parameters
		 * 
		 * {"provider":[provider],
		 * "pp":[{
		 * "id":"abc-124", 	[y-id]
		 * "h":289,			[y-h]
		 * "s":60,			[y-s]
		 * "x":105.987,		[y-x]
		 * "y":45.456,		[y-y]
		 * "t":"2011-05-26T13:47:34" [y-t]
		 * "a":100,			[n-a]
		 * "hp":10,			[n-hp]
		 * "sa":3,			[n-sa]
		 * "er":10,			[n-er]
		 * "mx":15.123,		[n-mx]
		 * "my":45.123,		[n-my]
		 * "am":1,			[n-am]
		 * "dt":1,			[n-dt]
		 * "ad":"key1=value1,key2=value2,keyN=valueN"	[n-ad]
		 * }]}*/
		
		/* Queue Message record data
		 * 
		 * BoxId==37432;;
		 * OriginDateTime==7/29/2015 3:33:25 PM;;
		 * DateTimeReceived==7/29/2015 3:34:25 PM;;
		 * DclId==15;;
		 * BoxMsgInTypeId==2;;
		 * BoxProtocolTypeId==23;;
		 * CommInfo1==10.217.86.39;;
		 * CommInfo2==4038;;
		 * ValidGps==0;;
		 * Latitude==45.553137;;
		 * Longitude==-73.734702;;
		 * Speed==0;;
		 * Heading==341;;
		 * SensorMask==20492;;
		 * CustomProp==SENSOR_NUM=3;SENSOR_STATUS=ON;Odometer=125633;Analog1=0;Analog2=0;FUEL=0;FFV=1;;;
		 * BlobDataSize==0;;
		 * StreetAddress==480 Boulevard Armand-Frappier, Laval, QC, H7V 4B4;;
		 * SequenceNum==0;;
		 * IsArmed==1;;
		 * NearestLandmark==BSM Laval Office;;
		 * LSD==DS=109.4;SC=SC6;FC=5;;
		 * Processed==;;
		 * SlsDateTime==7/29/2015 3:34:28 PM;;
		 * ID==2756284065;;
		 * RoadSpeed==50;;
		 * IsPostedSpeed==False;;
		 * SpeedZoneType==0;;
		 * Organizationid==480;;
		 * StandardValidGps==0;;*/
		//String JsonsimpleFormat="{\"provider\":[provider],\"pp\":[{[y-id][y-h][y-s][y-x][y-y][y-t]}]}";
		String JsonfullFormat="{\"provider\":[provider],\"pp\":[{[y-id][y-h][y-s][y-x][y-y][y-t][n-a][n-hp][n-sa][n-er][n-mx][n-my][n-am][n-dt][n-ad]}]}";
		JsonfullFormat.replace("[provider]",toString(provider));
		
		String y_id=mapData.get("BoxId");
		y_id=toString("id")+":"+toString(y_id)+",";
		JsonfullFormat.replace("[y-id]",y_id);
		
		String y_h=mapData.get("Heading");
		y_id=toString("h")+":"+y_h+",";
		JsonfullFormat.replace("[y-h]",y_h);
		
		String y_s=mapData.get("Speed");
		y_id=toString("s")+":"+y_s+",";
		JsonfullFormat.replace("[y-s]",y_s);
		
		String y_x=mapData.get("Longitude");
		y_id=toString("x")+":"+y_x+",";
		JsonfullFormat.replace("[y-s]",y_x);
		
		String y_y=mapData.get("Latitude");
		y_id=toString("y")+":"+y_y+",";
		JsonfullFormat.replace("[y-s]",y_y);
		
		String y_t=mapData.get("OriginDateTime");
		y_t=toCovertDate(y_t);
		y_id=toString("t")+":"+toString(y_t)+",";
		JsonfullFormat.replace("[y-id]",y_t);
		
		/* n, this part maybe NULL when ??? then can't working*/
		String n_a=mapData.get("???");
		y_id=toString("a")+":"+n_a+",";
		if(n_a==null){
			JsonfullFormat.replace("[n-a]",n_a);
		}
		else{
			JsonfullFormat.replace("[n-a]","");
		}
		
		String n_hp=mapData.get("???");
		y_id=toString("hp")+":"+n_hp+",";
		if(n_hp==null){
			JsonfullFormat.replace("[n-hp]",n_hp);
		}
		else{
			JsonfullFormat.replace("[n-hp]","");
		}
		
		String n_sa=mapData.get("???");
		y_id=toString("sa")+":"+n_sa+",";
		if(n_sa==null){
			JsonfullFormat.replace("[n-sa]",n_sa);
		}
		else{
			JsonfullFormat.replace("[n-sa]","");
		}
		
		String n_er=mapData.get("???");
		y_id=toString("er")+":"+n_er+",";
		if(n_er==null){
			JsonfullFormat.replace("[n-er]",n_er);
		}
		else{
			JsonfullFormat.replace("[n-er]","");
		}
		
		String n_mx=mapData.get("???");
		y_id=toString("mx")+":"+n_mx+",";
		if(n_mx==null){
			JsonfullFormat.replace("[n-mx]",n_mx);
		}
		else{
			JsonfullFormat.replace("[n-mx]","");
		}
		
		String n_my=mapData.get("???");
		y_id=toString("my")+":"+n_my+",";
		if(n_my==null){
			JsonfullFormat.replace("[n-my]",n_my);
		}
		else{
			JsonfullFormat.replace("[n-my]","");
		}
		
		String n_am=mapData.get("???");
		y_id=toString("am")+":"+n_am+",";
		if(n_am==null){
			JsonfullFormat.replace("[n-am]",n_am);
		}
		else{
			JsonfullFormat.replace("[n-am]","");
		}
		
		String n_dt=mapData.get("???");
		y_id=toString("dt")+":"+n_dt+",";
		if(n_dt==null){
			JsonfullFormat.replace("[n-dt]",n_dt);
		}
		else{
			JsonfullFormat.replace("[n-dt]","");
		}
		
		/*need modify,need format to key/value,the value is "key1=value1,key2=value2"*/
		String n_ad=mapData.get("???");
		y_id=toString("ad")+":"+toString(n_ad)+",";
		if(n_ad==null){
			JsonfullFormat.replace("[n-ad]",n_ad);
		}
		else{
			JsonfullFormat.replace("[n-ad]","");
		}
		/* end need modify********************************/
		
		JsonfullFormat.replace(",}", "}"); //delete last ","
		
		return JsonfullFormat;
	}
	private String toCovertDate(String str) throws ParseException
	{
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH);
		Date date = format.parse(str);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String aa = dateFormat.format(date);
		return aa;
	}
	private String toString(String str)
	{
		return "\""+str+"\"";
	}
	private Map<String,String> Convert_SqlDBToMap(String rowStr)
	{
		Map<String,String> map=new HashMap<String,String> ();
		rowStr=rowStr.substring(2, rowStr.length()-2); // delete [{}]
		rowStr=rowStr.replace(" ", "");//delete space
		String rowKV[]=Convert_SplitStringtoArray(rowStr,",");
		for(int i=0;i<rowKV.length;i++)
		{
			String ElementKV[]=Convert_SplitStringtoArray(rowStr,"=");
			map.put(ElementKV[0], ElementKV[1]);
		}
		return map;
	}
	private Map<String,String> Convert_QueuedataToMap(String queueStr)
	{
		Map<String,String> map=new HashMap<String,String> ();
		String rowKV[]=Convert_SplitStringtoArray(queueStr,";;");
		for(int i=0;i<rowKV.length;i++)
		{
			String ElementKV[]=Convert_SplitStringtoArray(queueStr,"==");
			map.put(ElementKV[0], ElementKV[1]);
		}
		return map;
	}
	private String[] Convert_SplitStringtoArray(String Data,String token)
	{
		return Data.split(token);
	}
	/**
	 * Decode BASE64 String */
	private String toDecodeBase64String(String str) throws UnsupportedEncodingException 
	{
		byte [] base64decodedBytes = Base64.getDecoder().decode(str);
	    String decodedstr=new String(base64decodedBytes, "utf-8");
	    return decodedstr;
	}
	/**
	 * Encode BASE64 String */
	private String toEncodeBase64String(String str) throws UnsupportedEncodingException
	{
		byte [] base64encodedBytes=str.getBytes("utf-8");
		String encodedstr = Base64.getEncoder().encodeToString(base64encodedBytes);
		return encodedstr;

	}
	/**
	 * UNZIP string
	 */
	private String toUnzipString(InputStream inputStr) throws IOException
	{
		InputStream in = new GZIPInputStream(inputStr);
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    int len1;
	    byte[] buffer1 = new byte[1024];
	    while ((len1 = in.read(buffer1)) != -1) {
	        byteArrayOutputStream.write(buffer1, 0, len1);
	    }
	    in.close();
	    byteArrayOutputStream.close();
	    String str1 = new String(byteArrayOutputStream.toByteArray(), "utf-8");
	    return str1;
	}
}
