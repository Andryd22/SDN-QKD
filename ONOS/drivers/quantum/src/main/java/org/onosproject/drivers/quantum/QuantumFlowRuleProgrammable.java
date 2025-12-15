package org.onosproject.drivers.quantum;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.DefaultFlowEntry;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleProgrammable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class QuantumFlowRuleProgrammable
        extends AbstractHandlerBehaviour implements FlowRuleProgrammable {

    private static final Logger log =
            LoggerFactory.getLogger(QuantumFlowRuleProgrammable.class);

    private static final Map<String, FlowRule> flowCache = new HashMap<>();

    /**
     * Apply the flow entries specified in the collection rules.
     *
     * @param rules A collection of Flow Rules to be applied
     * @return The collection of added Flow Entries
     */
    @Override
    public Collection<FlowRule> applyFlowRules(Collection<FlowRule> rules) {

        log.info("QKD applyFlowRules cache size {}", flowCache.size());

        for (FlowRule flowRule : rules) {
            log.info("OpenConfig added flowrule {}", flowRule);
            flowCache.put(flowKey(flowRule), flowRule);
        }
        //Print out number of rules sent to the device (without receiving errors)
        log.info("QKD applyFlowRules added {}", rules.size());
        log.info("QKD applyFlowRules cache size {}", flowCache.size());

        return rules;
    }

    /**
     * Get the flow entries that are present on the device.
     *
     * @return A collection of Flow Entries
     */
    @Override
    public Collection<FlowEntry> getFlowEntries() {

        log.info("QKD getFlowEntries cache size {}", flowCache.size());

        Collection<FlowEntry> fetched = flowCache.values().stream()
                .filter(fr -> fr.deviceId().equals(did()))
                .map(fr -> new DefaultFlowEntry(fr, FlowEntry.FlowEntryState.ADDED, 0, 0, 0))
                .collect(Collectors.toList());

        //Print out number of rules actually found on the device that are also included in the cache
        log.info("QKD getFlowEntries fetched connections {}", fetched.size());

        return fetched;
    }

    /**
     * Remove the specified flow rules.
     *
     * @param rules A collection of Flow Rules to be removed
     * @return The collection of removed Flow Entries
     */
    @Override
    public Collection<FlowRule> removeFlowRules(Collection<FlowRule> rules) {

        log.info("QKD removeFlowRules cache size {}", flowCache.size());

        List<FlowRule> removed = new ArrayList<>();
        for (FlowRule flowRule : rules) {
            try {
                flowCache.remove(flowKey(flowRule));
                removed.add(flowRule);
            } catch (Exception e) {
                log.error("Error {}", e);
            }
        }

        //Print out number of removed rules from the device (without receiving errors)
        log.info("QKD removeFlowRules removed {}", removed.size());
        log.info("QKD removeFlowRules cache size {}", flowCache.size());

        return removed;
    }

    private List<PortNumber> getLinePorts() {
        List<PortNumber> linePorts;

        DeviceService deviceService = this.handler().get(DeviceService.class);

        linePorts = deviceService.getPorts(data().deviceId()).stream()
                .map(p -> p.number())
                .collect(Collectors.toList());

        return linePorts;

    }

    private DeviceId did() {
        return data().deviceId();
    }

    private String flowKey(FlowRule rule) {
        String key = String.valueOf(did().hashCode() + rule.selector().hashCode() + rule.treatment().hashCode());
        log.info("QKD flowKey generated {}", key);

        return key;
    }
}
