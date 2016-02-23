package varUtility;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import rForecast.VARParameter;
import snapshot.SnapshotSchema;
import tree.Tree;

public class VAROutput {

	public void updateOutputFile(Tree tree, LinkedList<VARParameter> VARParam,
			HashMap<String, Integer> counter, PrintStream outputVARParameter,
			int idSnapshot,SnapshotSchema s,HashMap<String,PrintStream> streams, HashMap<String,ArrayList<Double>> countRMSE) {
		if (tree.countPar(counter)) {
			outputVARParameter.print("ID: " + idSnapshot + ";");
			int nodesInTree=tree.countNodes();
			for (VARParameter vp : VARParam) {
				outputVARParameter.print(counter.get(vp.toString()) + ";");
				PrintStream stream= streams.get(vp.toString());
				ArrayList<Double> rmse=countRMSE.get(vp.toString());
				stream.print("\n");
				stream.print("ID: " + idSnapshot + ";");
				for (Double d: rmse){
					stream.print(d/nodesInTree+";");
				}
				
			}
			outputVARParameter.print("\n");
		}

		for (VARParameter vp : VARParam) {
			counter.put(vp.toString(), 0);
			
			ArrayList<Double> aux= countRMSE.get(vp.toString());
			for(int i=0; i<aux.size(); i++){
				aux.set(i, 0.0);
			}
			countRMSE.put(vp.toString(), aux);
		}
	}

}
