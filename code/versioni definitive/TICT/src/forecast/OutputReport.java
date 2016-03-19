package forecast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import data.SensorPoint;
import rForecast.ParametersRForecastIndex;
import snapshot.SnapshotData;

public class OutputReport {

	private ArrayList<PrintStream> output;

	public OutputReport(int nahead, ArrayList<Object> rParameters,
			String dataset,String outputPath) {
		output = new ArrayList<PrintStream>();
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		
		for (int i = 1; i <= nahead; i++) {
			try {
				FileOutputStream outputFile = new FileOutputStream(outputPath+
						"output/stream/forecast/" + dataset + "_nahead_" + i
								+ "_" + rParameters.get(index.TWSize) + "_"
								+ rParameters.get(index.ic) + "_"
								+ rParameters.get(index.type) + ".arff");
				output.add(new PrintStream(outputFile));
				output.get(i-1).println("@");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

	}

	public void addForecast(LinkedList<SnapshotData> snaps) {
		int i = 0;
		for (SnapshotData d : snaps) {
			
			for (SensorPoint sp: d){
				output.get(i).println(sp);
			}
			output.get(i).println("@");
			i = i + 1;
		}
	}
	
	
	public void closeFiles(){
		for (PrintStream p: output){
			p.close();
		}
	}

}
