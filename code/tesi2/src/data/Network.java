package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import data.feature.Feature;
import forecast.FeatureVARForecastingModel;
import forecast.ForecastingModel;
import forecast.NotForecastingModelException;
import rForecast.RVar;
import rForecast.VARParameter;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import windowStructure.FeatureWindow;

/*
 * la classe ha la responsabilità di modellare il network
 * (l'insieme di TUTTI i sensori attivi e non istante per istante)
 */
public class Network implements Iterable<Integer> {

	/*
	 * le chiavi del map rappresentano gli id dei sensori nella rete mentre gli
	 * oggetti associati alle chiavi sono le timeseries associate al sensore
	 */
	private Map<Integer, RecordNetwork> network;

	/*
	 * costruttore della calsse istanzia il map
	 */
	public Network() {
		network = new HashMap<Integer, RecordNetwork>();
	}

	/*
	 * il metodo ha la responsabilità di creare la rete tale metodo viene
	 * richiamato solo dopo aver letto il PRIMO snapshot che contiene TUTTI i
	 * sensori della rete
	 */
	public void createWork(SnapshotData data, SnapshotSchema schema, int dim) {

		for (SensorPoint sp : data) {
			network.put(sp.getId(), new RecordNetwork(new FeatureWindow(schema,
					dim)));
		}

	}

	/*
	 * tale metodo ha la responsabilità di aggiornare la timeseries di ciascun
	 * sensore nella rete con i valori attuali letti dai sensori nello snapshot
	 * attuale passato in input
	 */
	public void updateNetwork(SnapshotData data, SnapshotSchema schema) {

		for (SensorPoint sp : data) {

			RecordNetwork rn = network.get(sp.getId());
			FeatureWindow timeseries = rn.getTimeseries();
			timeseries.updateSensorFeature(sp, schema);
			rn.setTimeSeries(timeseries);
			network.put(sp.getId(), rn);

		}
	}

	public Iterator<Integer> iterator() {
		return network.keySet().iterator();
	}

	public FeatureWindow getTemporalWindow(int i) {
		return network.get(i).getTimeseries();
	}

	public ForecastingModel getVARModel(int i) {
		return network.get(i).getVARModel();
	}

	public void learnVARModel(SnapshotSchema schema,
			ArrayList<Object> rParameters) {

		for (int i : this.network.keySet()) {

			RecordNetwork rn = network.get(i);
			double[][] dataset = rn.getTimeseries().exportInMatrixForm();
			ForecastingModel vm;
			try {
				vm = new ForecastingModel(dataset, schema, rParameters);
			} catch (NotForecastingModelException e) {
				vm = null;
			}
			rn.setVARModel(vm);
			network.put(i, rn);
		}

	}

	public ArrayList<Object> countPar(SnapshotSchema schema) {
		HashMap<String, HashMap<String, Integer>> hmCombResult = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> counterByPar;
		HashMap<String, Double> hmOptimalRMSEByFeature = new HashMap<String, Double>();
		for (Feature f : schema.getTargetList()) {
			counterByPar = new HashMap<String, Integer>();
			for (VARParameter s : RVar.getVARParameters()) {
				String par = s.toString();
				counterByPar.put(par, 0);
			}
			hmCombResult.put(f.getName(), counterByPar);
			hmOptimalRMSEByFeature.put(f.getName(), 0.0);
		}

		String pars;
		int counterSensorWithVARModel = 0;
		for (Integer i : this.network.keySet()) {

			ForecastingModel model = network.get(i).getVARModel();

			if (model == null)
				continue;

			for (Feature f : schema.getTargetList()) {
				FeatureVARForecastingModel fmodel = (FeatureVARForecastingModel) model
						.getFeatureForecastingModel(f);
				pars = fmodel.getVARParameters();
				hmCombResult.get(f.getName()).put(pars,
						hmCombResult.get(f.getName()).get(pars) + 1);

				hmOptimalRMSEByFeature.put(
						f.getName(),
						hmOptimalRMSEByFeature.get(f.getName())
								+ fmodel.getRMSE());
				counterSensorWithVARModel = counterSensorWithVARModel + 1;
			}
		}
		ArrayList<Object> finalResult = new ArrayList<Object>();
		finalResult.add(0, hmCombResult);
		finalResult.add(1, hmOptimalRMSEByFeature);
		finalResult.add(2, counterSensorWithVARModel);
		return finalResult;
	}

}