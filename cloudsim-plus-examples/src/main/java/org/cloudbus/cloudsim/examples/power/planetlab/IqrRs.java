package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.examples.power.util.PlanetLabRunner;
import org.cloudbus.cloudsim.util.ResourceLoader;

import java.io.IOException;

/**
 * A simulation of a heterogeneous power aware data center that applies the Inter Quartile Range
 * (IQR) VM allocation policy and Random Selection (RS) VM selection policy.
 *
 * This example uses a real PlanetLab workload: 20110303.
 *
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
 *
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 *
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 *
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class IqrRs {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		String inputFolder =  "workload/planetlab";
		String outputFolder = "output";
		String workload = "20110303"; // PlanetLab workload
		String vmAllocationPolicy = "iqr"; // Inter Quartile Range (IQR) VM allocation policy
		String vmSelectionPolicy = "rs"; // Random Selection (RS) VM selection policy
		double safetyParameter = 1.5;

		new PlanetLabRunner(
            true,
            false,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				safetyParameter);
	}

}
