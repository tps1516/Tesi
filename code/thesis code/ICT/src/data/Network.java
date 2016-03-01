package data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import windowStructure.FeatureWindow;
import windowStructure.TemporalWindow;

/*
 * la classe ha la responsabilità di modellare il network
 * (l'insieme di TUTTI i sensori attivi e non istante per istante)
 */
public class Network implements Iterable<Integer>{
	
	/*
	 * le chiavi del map rappresentano gli id dei sensori nella rete
	 * mentre gli oggetti associati alle chiavi sono le timeseries
	 * associate al sensore
	 */
	private Map<Integer, FeatureWindow> network;

	/*
	 * costruttore della calsse
	 * istanzia il map
	 */
	public Network() {
		network = new HashMap<Integer, FeatureWindow>();
	}

	/*
	 * il metodo ha la responsabilità di creare la rete
	 * tale metodo viene richiamato solo dopo aver letto il PRIMO 
	 * snapshot che contiene TUTTI i sensori della rete
	 */
	public void createWork(SnapshotData data, SnapshotSchema schema, int dim) {

		/*
		 * per ciascun sensore memorizzato nello snapshot 
		 * memorizzo nell'hashMap l'id del sensore
		 * associandoli una timeseries vuota (solo istanziata)
		 */
		for (SensorPoint sp : data) {
			network.put(sp.getId(), new FeatureWindow(schema, dim));
		}

	}

	/*
	 * tale metodo ha la responsabilità di aggiornare la timeseries
	 * di ciascun sensore nella rete con i valori attuali letti dai sensori
	 * nello snapshot attuale passato in input 
	 */
	public void updateNetword(SnapshotData data, SnapshotSchema schema,
			HashMap<Integer, SensorPoint> networkPredicted) {
		
		
		/*
		 * ciclo su ciascun sensore nella rete
		 */
		for (Integer idSensorPoint : network.keySet()) {

			//setto una variabile boolean false come flag
			boolean sensorPointFind = false;

			/*
			 * ciclo su tutti i sensori attivi nello snapshot in esame
			 * per verificare se il sensore è attivo nello snapshto
			 */
			for (SensorPoint sp : data) {
				
				/*
				 * se il sensore è attivo 
				 * recupero la sua timeseries 
				 * e l'aggiorno con i valori rilevati 
				 * in questo istante
				 * setto la varibile flag a true
				 * ed esco dal ciclo 
				 */
				if (idSensorPoint == sp.getId()) {
					FeatureWindow tw = network.get(idSensorPoint);
					tw.updateSensorFeature(sp, schema);
					network.put(idSensorPoint, tw);
					sensorPointFind = true;
					break;
				}

			}

			/*
			 * al termine del ciclo su tutti i sensori attivi 
			 * verifico se il sensore era attivo controllando lo stato 
			 * della variabile flag e se non era attivo 
			 * recupero la sua timesiries e l'aggiorno questa volta
			 * con i valori predetti per tale sensore nell'istante precedente
			 * con n.ahead 1
			 */
			if (!sensorPointFind) {
				FeatureWindow tw = network.get(idSensorPoint);
				tw.updateSensorFeature(networkPredicted.get(idSensorPoint), schema);
				network.put(idSensorPoint, tw);
			}

		}
	}

	public Iterator<Integer> iterator() {
		return network.keySet().iterator();
	}
	
	public FeatureWindow getTemporalWindow(int i) {
		return network.get(i);
	}
}
