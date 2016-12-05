/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.allocationpolicies.power;

import org.cloudbus.cloudsim.hosts.power.PowerHost;
import org.cloudbus.cloudsim.hosts.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * A VM allocation policy that uses Local Regression (LR) to predict host utilization (load)
 * and define if a host is overloaded or not.
 *
 * <p>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:</p>
 *
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmAllocationPolicyMigrationLocalRegression extends PowerVmAllocationPolicyMigrationAbstract {

	/** The scheduling interval that defines the periodicity of VM migrations. */
	private double schedulingInterval;

	/** The safety parameter in percentage (at scale from 0 to 1).
         * It is a tuning parameter used by the allocation policy to
         * estimate host utilization (load). The host overload detection is based
         * on this estimation.
         * This parameter is used to tune the estimation
         * to up or down. If the parameter is set as 1.2, for instance,
         * the estimated host utilization is increased in 20%, giving
         * the host a safety margin of 20% to grow its usage in order to try
         * avoiding SLA violations. As this parameter decreases, more
         * aggressive will be the consolidation (packing) of VMs inside a host,
         * what may lead to optimization of resource usage, but rising of SLA
         * violations. Thus, the parameter has to be set in order to balance
         * such factors.
         */
	private double safetyParameter;

	/** The fallback VM allocation policy to be used when
         * the Local REgression over utilization host detection doesn't have
         * data to be computed. */
	private PowerVmAllocationPolicyMigration fallbackVmAllocationPolicy;

	/**
	 * Creates a PowerVmAllocationPolicyMigrationLocalRegression.
	 *
	 * @param vmSelectionPolicy the vm selection policy
         * @param safetyParameter
	 * @param schedulingInterval the scheduling interval
	 * @param fallbackVmAllocationPolicy the fallback vm allocation policy
	 * @param utilizationThreshold the utilization threshold
	 */
	public PowerVmAllocationPolicyMigrationLocalRegression(
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter,
			double schedulingInterval,
			PowerVmAllocationPolicyMigration fallbackVmAllocationPolicy,
			double utilizationThreshold) {
		super(vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setSchedulingInterval(schedulingInterval);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
	}

	/**
	 * Creates a PowerVmAllocationPolicyMigrationLocalRegression.
	 *
	 * @param vmSelectionPolicy the vm selection policy
     * @param safetyParameter
	 * @param schedulingInterval the scheduling interval
	 * @param fallbackVmAllocationPolicy the fallback vm allocation policy
	 */
	public PowerVmAllocationPolicyMigrationLocalRegression(
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter,
			double schedulingInterval,
			PowerVmAllocationPolicyMigration fallbackVmAllocationPolicy) {
		super(vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setSchedulingInterval(schedulingInterval);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
	}

	/**
	 * Checks if a host is over utilized.
	 *
	 * @param host the host
	 * @return true, if is host over utilized; false otherwise
	 */
	@Override
	public boolean isHostOverUtilized(PowerHost host) {
		PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
		double[] utilizationHistory = _host.getUtilizationHistory();
		int length = 10; // we use 10 to make the regression responsive enough to latest values
		if (utilizationHistory.length < length) {
			return getFallbackVmAllocationPolicy().isHostOverUtilized(host);
		}
		double[] utilizationHistoryReversed = new double[length];
		for (int i = 0; i < length; i++) {
			utilizationHistoryReversed[i] = utilizationHistory[length - i - 1];
		}
		double[] estimates = null;
		try {
			estimates = getParameterEstimates(utilizationHistoryReversed);
		} catch (IllegalArgumentException e) {
			return getFallbackVmAllocationPolicy().isHostOverUtilized(host);
		}
		double migrationIntervals = Math.ceil(getMaximumVmMigrationTime(_host) / getSchedulingInterval());
		double predictedUtilization = estimates[0] + estimates[1] * (length + migrationIntervals);
		predictedUtilization *= getSafetyParameter();

		addHistoryEntryIfAbsent(host, predictedUtilization);

		return predictedUtilization >= 1;
	}

	/**
	 * Gets utilization estimates.
	 *
	 * @param utilizationHistoryReversed the utilization history in reverse order
	 * @return the utilization estimates
	 */
	protected double[] getParameterEstimates(double[] utilizationHistoryReversed) {
		return MathUtil.getLoessParameterEstimates(utilizationHistoryReversed);
	}

	/**
	 * Gets the maximum vm migration time.
	 *
	 * @param host the host
	 * @return the maximum vm migration time
	 */
	protected double getMaximumVmMigrationTime(PowerHost host) {
		double maxRam = host.getVmList().stream().mapToDouble(Vm::getRam).max().orElse(0);
		return maxRam / (host.getBwCapacity() / (2 * 8));
	}

	/**
	 * Sets the scheduling interval.
	 *
	 * @param schedulingInterval the new scheduling interval
	 */
	protected final void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

	/**
	 * Gets the scheduling interval.
	 *
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the fallback vm allocation policy.
	 *
	 * @param fallbackVmAllocationPolicy the new fallback vm allocation policy
	 */
	public final void setFallbackVmAllocationPolicy(
			PowerVmAllocationPolicyMigration fallbackVmAllocationPolicy) {
		this.fallbackVmAllocationPolicy = fallbackVmAllocationPolicy;
	}

	/**
	 * Gets the fallback vm allocation policy.
	 *
	 * @return the fallback vm allocation policy
	 */
	public PowerVmAllocationPolicyMigration getFallbackVmAllocationPolicy() {
		return fallbackVmAllocationPolicy;
	}

	public double getSafetyParameter() {
		return safetyParameter;
	}

	public final void setSafetyParameter(double safetyParameter) {
		this.safetyParameter = safetyParameter;
	}

}
