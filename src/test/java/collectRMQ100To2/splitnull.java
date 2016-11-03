package collectRMQ100To2;

public class splitnull {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		splitnull kk=new splitnull();
		String loginurl;
		String posturl;
		String str="www.xfcd.com/var";
		String [] array=kk.Convert_SplitStringtoArray(str, ";");
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
		System.out.println("login:"+loginurl+"\nposturl:"+posturl);
	}
	public String[] Convert_SplitStringtoArray(String Data,String token)
	{
		return Data.split(token);
	}
}
