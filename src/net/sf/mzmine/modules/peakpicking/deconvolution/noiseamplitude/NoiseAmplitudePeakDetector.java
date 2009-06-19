/*
 * Copyright 2006-2009 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peakpicking.deconvolution.noiseamplitude;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.modules.peakpicking.deconvolution.PeakResolver;
import net.sf.mzmine.modules.peakpicking.deconvolution.ResolvedPeak;

/**
 * 
 */
public class NoiseAmplitudePeakDetector implements PeakResolver {

	private double amplitudeOfNoise;
	private double minimumPeakHeight, minimumPeakDuration;

	public NoiseAmplitudePeakDetector(NoiseAmplitudePeakDetectorParameters parameters) {

		minimumPeakDuration = (Double) parameters
				.getParameterValue(NoiseAmplitudePeakDetectorParameters.minimumPeakDuration);
		minimumPeakHeight = (Double) parameters
				.getParameterValue(NoiseAmplitudePeakDetectorParameters.minimumPeakHeight);
		amplitudeOfNoise = (Double) parameters
				.getParameterValue(NoiseAmplitudePeakDetectorParameters.amplitudeOfNoise);

	}

	/**
	 */
    public ChromatographicPeak[] resolvePeaks(ChromatographicPeak chromatogram,
            int scanNumbers[], double retentionTimes[], double intensities[]) {

        Vector<ResolvedPeak> resolvedPeaks = new Vector<ResolvedPeak>();

        // This treeMap stores the score of frequency of intensity ranges
		TreeMap<Integer, Integer> binsFrequency = new TreeMap<Integer, Integer>();
		double maxIntensity = 0;
		double avgChromatoIntensities = 0;
		
		for (int i = 0; i < scanNumbers.length; i++) {

			addNewIntensity(intensities[i], binsFrequency);
			if (intensities[i] > maxIntensity)
				maxIntensity = intensities[i];
			avgChromatoIntensities += intensities[i];

		}

		avgChromatoIntensities /= scanNumbers.length;

		// If the current chromatogram has characteristics of background or just
		// noise
		// return an empty array.
		if ((avgChromatoIntensities) > (maxIntensity * 0.5f))
			return resolvedPeaks.toArray(new ResolvedPeak[0]);

		double noiseThreshold = getNoiseThreshold(binsFrequency, maxIntensity);

		boolean activePeak = false;
		// Index of starting region of the current peak
		int totalNumberPoints = scanNumbers.length;
		int currentPeakStart = totalNumberPoints;


		for (int i = 0; i < totalNumberPoints; i++) {
			if ((intensities[i] > noiseThreshold) && (!activePeak)) {
				currentPeakStart = i;
				activePeak = true;
			}

			if ((intensities[i] < noiseThreshold) && (activePeak)) {
				if (i - currentPeakStart > 0) {
					ResolvedPeak peak = new ResolvedPeak(chromatogram,
							currentPeakStart, i);
					double pLength = peak.getRawDataPointsRTRange().getSize();
					double pHeight = peak.getHeight();
					if ((pLength >= minimumPeakDuration)
							&& (pHeight >= minimumPeakHeight)) {
						resolvedPeaks.add(peak);
					}
				}
				currentPeakStart = totalNumberPoints;
				activePeak = false;
			}
		}

		return resolvedPeaks.toArray(new ResolvedPeak[0]);
	}

	/**
	 * This method put a new intensity into a treeMap and score the frequency
	 * (the number of times that is present this level of intensity)
	 * 
	 * @param intensity
	 * @param binsFrequency
	 */
	public void addNewIntensity(double intensity,
			TreeMap<Integer, Integer> binsFrequency) {
		int frequencyValue = 1;
		int numberOfBin;
		if (intensity < amplitudeOfNoise)
			numberOfBin = 1;
		else
			numberOfBin = (int) Math.floor(intensity / amplitudeOfNoise);

		if (binsFrequency.containsKey(numberOfBin)) {
			frequencyValue = binsFrequency.get(numberOfBin);
			frequencyValue++;
		}
		binsFrequency.put(numberOfBin, frequencyValue);

	}

	/**
	 * This method returns the noise threshold level. This level is calculated
	 * using the intensity with more datapoints.
	 * 
	 * 
	 * @param binsFrequency
	 * @param maxIntensity
	 * @return
	 */
	public double getNoiseThreshold(TreeMap<Integer, Integer> binsFrequency,
			double maxIntensity) {

		int numberOfBin = 0;
		int maxFrequency = 0;

		Set<Integer> c = binsFrequency.keySet();
		Iterator<Integer> iteratorBin = c.iterator();

		while (iteratorBin.hasNext()) {
			int bin = iteratorBin.next();
			int freq = binsFrequency.get(bin);

			if (freq > maxFrequency) {
				maxFrequency = freq;
				numberOfBin = bin;
			}
		}

		double noiseThreshold = (numberOfBin + 2) * amplitudeOfNoise;
		double percentage = noiseThreshold / maxIntensity;
		if (percentage > 0.3)
			noiseThreshold = amplitudeOfNoise;

		return noiseThreshold;
	}

}