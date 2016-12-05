package org.cloudbus.cloudsim.allocationpolicies.power;

import org.cloudbus.cloudsim.hosts.power.PowerHost;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;

/**
 * An interface to be implemented by VM allocation policy for Power-aware VMs.
 *
 * @author Manoel Campos da Silva Filho
 */
public interface PowerVmAllocationPolicy extends VmAllocationPolicy{
    /**
     * Finds the first host that has enough resources to host a given VM.
     *
     * @param vm the vm to find a host for it
     * @return the first host found that can host the VM or {@link PowerHost#NULL} if no suitable
     * Host was found for Vm
     */
    PowerHost findHostForVm(Vm vm);
}
