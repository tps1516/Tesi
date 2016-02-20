package forecast;

import rForecast.RForecast;
import rForecast.RVar;
import snapshot.SnapshotSchema;
import varUtility.FromRToJava;
import varUtility.OptimalVARModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import data.feature.Feature;

/*
 * tale classe ha la responsabilità di modellare il modello VAR
 * per ciascuna FEATURE dello schema
 */
public class ForecastingModel implements Iterable<FeatureForecastingModel>,
		Serializable {

	/*
	 * ha la responsabilità di memorizzare i modelli VAR di ciascuna FEATURE
	 * la posizione in cui il modello VAR viene memorizzato nell'arrayList
	 * corrisponde all'indice della feature stessa nello schema
	 */
	private ArrayList<FeatureForecastingModel> models;

	/*
	 * costruttore della classe
	 * ha la responsibilità di istanziare uno oggetto 
	 * tale che il modello VAR corrispondete alla FEATURE sia il migliore
	 * rispetto al RMSE ottenuto variando le possibili combinazini sui parametri 
	 * di input del modello VAR (type e ic)
	 */
	public ForecastingModel(double[][] dataset, SnapshotSchema schema,
			ArrayList<Object> rParameters) {
		HashMap<String, ArrayList<Object>> resultR;
		HashMap<String, ForecastingModel> resultRToJava;
		RForecast r = new RVar();
		
		/*
		 * richiamo il metodo RForecasting e memorizzo 
		 * i risultati ottenuti dalla chiamata nell'oggetto resultR
		 * tale oggetto è un hashmap tale che l'insieme delle chiavi
		 * rappresenta l'insieme di tutte le combinazioni possibili dei parametri di input
		 * al modello VAR mentre l elemento (arrayList<Object>) associato alla specifica
		 * chaive è il risultato ottenuto per tale configurazione
		 */
		resultR = r.RForecasting(dataset, schema, rParameters);
		FromRToJava fRToJava = new FromRToJava();
		
		/*
		 * converto la struttura dati ottenuta dalla chiamata precedente
		 * in una struttura analoga che però memorizza per ciascuna chiave
		 * un oggetto ForecastingModel 
		 * ogni oggetto corrisponde ad una specifica combinazione di parametri
		 */
		resultRToJava = fRToJava.converter(resultR, schema,
				doubleToDouble(dataset));
		OptimalVARModel opt = new OptimalVARModel();
		
		/*
		 * costruisco l'oggetto forecastingmodel finale 
		 * dove il modello var per ciascuna feature corrisponde
		 * al modello costruito con la combinazione di parametri
		 * che minimizza il valore del RMSE
		 */
		setModels(opt.computeOptimalVARModel(resultRToJava, schema));
	}

	/*
	 * costruttore della classe
	 * setta l'attributo models 
	 * con l'arrayList che arriva in input
	 * tale costruttore viene utilizzato per costruire un oggetto
	 * forecastingmodel dove i modelli VAR per ciascuna feature 
	 * sono costruiti tutti conl la stessa combinazione di parametri
	 */
	public ForecastingModel(ArrayList<FeatureForecastingModel> mod) {
		setModels(mod);
	}

	
	public String toString() {
		String str = "MODELLO VAR:" + "\n";
		;
		for (FeatureForecastingModel VARModel : models) {
			str += VARModel.toString() + "\n";
		}

		return str;
	}



	/*
	 * restituisca un iteratore 
	 */
	public Iterator<FeatureForecastingModel> iterator() {

		return this.models.iterator();
	}

	
	/*
	 * restituisca il modello VAR per la specifica feature
	 * che viene passata in input
	 */
	public FeatureForecastingModel getFeatureForecastingModel(Feature f) {
		return models.get(f.getFeatureIndex());
	}
	
	
	/*
	 * metodo privato
	 * setta l'attributo models
	 */
	private void setModels(ArrayList<FeatureForecastingModel> mod) {
		this.models = mod;
	}
	

	private Double[][] doubleToDouble(double[][] conv) {
		int rows = conv.length;
		int col = conv[0].length;
		Double[][] res = new Double[rows][col];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < col; j++)
				res[i][j] = conv[i][j];
		return res;

	}
	
}
