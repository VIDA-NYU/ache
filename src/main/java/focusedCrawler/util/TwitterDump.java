package focusedCrawler.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;

public class TwitterDump {

	public void execute(){
		int i = 253 & 0xff;
		byte b = (byte)i;
		System.out.println(b);
		System.out.println(Integer.toBinaryString(b));
		System.out.println();
		
	}
	
	
	private void writeFile(String file) throws IOException{
//		StringBuffer content = new StringBuffer();
//		content.append("name: twitter\n");
//		content.append("field: latitude,  float\n");
//		content.append("field: longitude, float\n");
//		content.append("field: time,      uint64\n");
//		content.append("field: language,      uint8\n");
//		content.append("valname: language, 0, en\n");
//		content.append("valname: language, 1, es\n");
//		content.append("field: sentiment,      uint8\n");
//		content.append("valname: sentiment, 0, positive\n");
//		content.append("valname: sentiment, 1, negative\n\n");
		DataOutputStream fout = new DataOutputStream(new FileOutputStream("C:\\user\\lbarbosa\\sample"));
		String header = "name: twitter\n";
		header = header + "field: latitude, float\n";
		header = header + "field: longitude, float\n";
		header = header + "field: time,      uint64\n";
		header = header + "field: language,      uint8\n";
		header = header + "valname: language, 0, en\n";
		header = header + "valname: language, 1, es\n";
		header = header + "field: sentiment,      uint8\n";
		header = header + "valname: sentiment, 0, positive\n";
		header = header + "valname: sentiment, 1, negative\n\n";
		byte[] headBytes = header.getBytes();
		fout.write(headBytes);
		float f = (float)-17.699852;
		fout.writeFloat(f);
		float f1 = (float)-45.527344;
		fout.writeFloat(f1);
		fout.writeLong(System.currentTimeMillis());
		int i = 0;
		fout.writeByte(i);
		fout.writeByte(i);
        fout.close();
	}
	
	public static void main(String[] args) {
		TwitterDump td = new TwitterDump();
		try {
			td.writeFile("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
