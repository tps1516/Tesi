package varUtility;

import java.util.ArrayList;

import forecast.FeatureVARForecastingModel;
import forecast.NotForecastingException;
import forecast.RecordVAR;

/*
 * la classe ha la responsabilità di modellare il calcolo
 * del RMSE
 */
public class RMSEUtility {

	/*
	 * calcola RMSE per una specifica feature per uno specifico VAR Model
	 * ottenuto con una specifica combinazione di parametri di input
	 */
	public Double computeRMSE(Double[][] timeseries,
			FeatureVARForecastingModel VARModel) {

		// setto a 0 il valore del RMSE
		double RMSE = 0.0;
		int p = VARModel.getP();

		/*
		 * setto a 0 il valore dell'indice di inzio per estrarre la matrice
		 * ridotta
		 */
		int beginIndexReducedMatrix = 0;

		/*
		 * calcolo il numero di righe nella timesiries (quindi quanti istanti di
		 * rilevazioni contiene)
		 */
		int timeseriesRows = timeseries.length;

		/*
		 * calcolo l'indice sul quale fermare la previsione per il calcolo del
		 * RMSE numero di istanti -1 (ultima riga della matrice) (perché in java
		 * il primo indice in una matrice è 0)
		 */
		int endIndexComputeRMSE = timeseriesRows - 1;

		int i = 0;
		double[][] reducedMatrix;
		double predicted = 0.0;
		double real = 0.0;

		/*
		 * inizio il calcolo del RMSE ciclando dal primo valore da predire fino
		 * all'ultimo e confrontandolo con quello reale per effettuare il
		 * calcolo
		 */
		for (i = p; i < endIndexComputeRMSE; i++) {

			/*
			 * mi calcolo la matrice ridotta ai p istanti che mi serve per
			 * andare ad effettuare la predizione
			 */
			reducedMatrix = reduceMatrix(timeseries, p, VARModel,
					beginIndexReducedMatrix, i - 1);

			/*
			 * effettuo la predizione del valore utilizzando la matrice ridotta
			 * appena estrapolata
			 */
			try {
				predicted = VARModel.forecasting(reducedMatrix);
			} catch (NotForecastingException e) {
				e.printStackTrace();
			}
			// predicted = predict(reducedMatrix, VARModel);

			/*
			 * estraggo il valore reale dalla timesiries corrispondente al
			 * valore appena predetto la riga è determinata dall'indice del
			 * ciclo mentre la colonna è determinata dal'indice della feature
			 * del VAR model
			 */
			real = timeseries[i][VARModel.getFeature().getFeatureIndex()];

			/*
			 * calcolo RMSE del valore attuale predetto e lo aggiungo al totale
			 */
			RMSE += Math.pow((real - predicted), 2);

			/*
			 * incremento l'indice di inzio estrazione della matrice ridotta
			 */
			beginIndexReducedMatrix++;
		}

		/*
		 * calcolo l RMSE finale dividendo il suo valore (al fine di
		 * standardizzarlo) per il numero di predizioni effettuate durante il
		 * suo calcolo
		 */
		RMSE /= (timeseriesRows - p);

		return RMSE;
	}

	/*
	 * metodo privato ha la responsabilità di calcolare la matrice ridotta
	 */
	private double[][] reduceMatrix(Double[][] timeseries, int p,
			FeatureVARForecastingModel VARModel, int beginIndexReducedMatrix,
			int endIndexReducedMatrix) {

		/*
		 * inizializzo la matrice da restituire il numero di righe sono i p
		 * istanti ridotti il numero di colonne resta invariato (numero di
		 * feature dello schema quindi numero di colonne già presenti nella
		 * timeseries)
		 */
		double[][] matrix = new double[p][timeseries[0].length];

		int indexMatrix = 0;

		/*
		 * avvaloro la matrice copiando i valori dalla timesiries nella matrice
		 * ridotta il ciclo più esterno cicla sulle righe quindi effettuando la
		 * riduzione il ciclo più interno riguarda le colonne che restano
		 * invariate indexMatrix rappresenta l'indice di riga nel quale andare a
		 * memorizzare nella matrice ridotta
		 */
		for (int i = beginIndexReducedMatrix; i <= endIndexReducedMatrix; i++) {
			for (int j = 0; j < timeseries[0].length; j++) {
				matrix[indexMatrix][j] = timeseries[i][j];
			}

			// al termine del ciclo più interno incremento indexMatrix
			indexMatrix++;
		}

		return matrix;
	}

	/*
	 * metodo privato ha la responsabilità di effettuare la predizione
	 */
	private double predict(double[][] reducedMatrix,
			FeatureVARForecastingModel VARModel) {
		double predict = 0.0;
		int p = VARModel.getP();

		/*
		 * effettuo un ciclo su tutte le feature correlate nella predizione
		 * della feature del modello al fine di implementare l'equazione per la
		 * predizione
		 */
		for (RecordVAR record : VARModel) {

			// recupero i coefficienti corrispondenti alla specifica feature
			// correlata
			ArrayList<Double> coeff = record.getCoefficients();

			/*
			 * i coefficienti vanno moltiplicati con i p valori reali della
			 * matrice ridotta i coefficenti vanno dal valore più recente al
			 * valore più lontano mentre nella matrice ridotta i valori reali
			 * sono memorizzati dal più lontano al più recente effetto questo
			 * ciclo in modo tale da avere i valori reali memorizzati dal più
			 * recente al più lontano
			 */
			ArrayList<Double> valReali = new ArrayList<Double>();
			for (int i = p - 1; i >= 0; i--) {
				valReali.add(reducedMatrix[i][record.getFeature()
						.getFeatureIndex()]);
			}

			/*
			 * effettuo la predizione moltiplicando ciascun valore
			 * precedentemente realmente misurato con il suo corrispondente
			 * coefficiente
			 */
			for (int i = 0; i < p; i++)
				predict += coeff.get(i) * valReali.get(i);
		}

		// sommo alla predizione gli eventuali coefficienti di trend e di const
		predict += VARModel.getCoefficientsConst()
				+ VARModel.getCoefficientsTrend();

		return predict;
	}
}
