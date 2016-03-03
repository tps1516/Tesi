package forecast;

import java.util.ArrayList;

import snapshot.SnapshotSchema;

public class Utility {

	boolean checkisPredictable(ForecastingModel VARModel, double[][] FW) {
		boolean isPredictable = true;
		FeatureVARForecastingModel FVFModel = null;
		int p;
		double err = Double.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		if (VARModel != null) {
			if (FW != null) {
				for (FeatureForecastingModel ffm : VARModel) {
					FVFModel = (FeatureVARForecastingModel) ffm;
					p = FVFModel.getP();
					if (p > max)
						max = p;
				}
				for (int i = FW[0].length; i > max; i--)
					for (int j = FW.length; j > max; j--) {
						if (FW[i][j] == err) {
							isPredictable = false;
							break;
						}
					}

			}
		}
		return isPredictable;
	}

	double computeEquation(double[][] FW, FeatureVARForecastingModel VARModel,
			SnapshotSchema schema) {
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
				valReali.add(FW[i][record.getFeature().getFeatureIndex()]);
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
