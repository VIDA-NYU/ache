package focusedCrawler.util.string;

public class StringDistance {

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}
 
	public static int computeLevenshteinDistance(CharSequence str1,CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];
		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;
		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));
		return distance[str1.length()][str2.length()];
	}

	
	public static void main(String[] args) {
		try {
			java.io.BufferedReader input = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[0])));
			String a1 = input.readLine();

			java.io.BufferedReader input1 = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[1])));
			String b1 = input1.readLine();
			
//			String a1 = args[0];
//			String b1 = args[1];
			double max = Math.max(a1.length(), b1.length());
			int dist = StringDistance.computeLevenshteinDistance(a1,b1);
			double dist1 = (dist)/max;
			System.out.println(dist1);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
	
}
