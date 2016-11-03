package collectRMQ100To2;

import org.json.JSONObject;

public class jsontest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String json="{\"copyright\":\"CopyrightINRIXInc.2015\",\"versionNumber\":\"v1\",\"createdDate\":\"2015-06-04T22:58:39.039099Z\",\"responseId\":\"f5ba0b34-06e9-40de-af8a-8bb008fc3454\",\"result\":{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImVjOWM3ZDAwLWUwNjUtNDc1OS05M2UyLWI3MWJhMmQ0MTAyYSIsIkV4cGlyeSI6IjIwMTUtMDYtMDVUMjI6NTg6MzkuMDcwMzQ5N1oiLCJleHAiOjE0MzM1NDUxMTksInJvbGUiOiJzZXJ2aWNlIn0.5fL8TL9z6hq2smrpbFhZE3S2CATTvhYNHgzFa5TQtzE\",\"expiry\":\"2015-06-05T22:58:39.0703497Z\"}}";
		JSONObject objroot = new JSONObject(json);
		JSONObject objresult = objroot.getJSONObject("result");
		String token = objresult.getString("token");
		System.out.println("[token]"+token);
	}

}
