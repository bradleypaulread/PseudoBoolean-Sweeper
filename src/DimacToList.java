import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DimacToList {

	List<Integer> lits = new ArrayList<Integer>();
	int MAX_VARS;
	int MAX_CLAUSES;

	public DimacToList(String file) throws IOException {

		File f = new File(file);
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line;

		while ((line = br.readLine()) != null) {
			if (line.charAt(0) == 'c')
				continue;
			if (line.charAt(0) == 'p') {
				// Plus 4 to create a substring from after cnf string
				String[] str = line.substring(line.indexOf("cnf")+4).trim().split(" ");
				MAX_VARS = Integer.parseInt(str[0]);
				MAX_CLAUSES = Integer.parseInt(str[1]);
				break;
			}
		}
		while ((line = br.readLine()) != null) {
			while ((line.charAt(line.length()-1) != '0')) {
				line = line + " " + br.readLine();
			}
			String[] litrals = line.split(" ");
			for (String s : litrals) {
				if (!s.equals("")) {
					lits.add(Integer.parseInt(s));
				}
			}
		}
		br.close();
	}
	
	public List<Integer> getLits() {
		return lits;
	}
	
	public int getMAX_VARS() {
		return MAX_VARS;
	}

	public int getMAX_CLAUSES() {
		return MAX_CLAUSES;
	}
}
