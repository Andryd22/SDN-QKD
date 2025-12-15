package org.onosproject.drivers.quantum;

import com.google.common.collect.ImmutableSet;
import org.onosproject.net.*;
import org.onosproject.net.behaviour.LambdaQuery;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;

import java.util.Set;
import java.util.stream.IntStream;

public class QuantumLambdaQuery extends AbstractHandlerBehaviour implements LambdaQuery {

    //Assumes a lambda range in the form N-M where N and M are integer numbers
    //Returns M-N channels starting from index N
    @Override
    public Set<OchSignal> queryLambdas(PortNumber portNumber) {
        DeviceService deviceService = this.handler().get(DeviceService.class);
        Port port = deviceService.getPort(data().deviceId(), portNumber);

        String lambdaRange = port.annotations().value("qkdi_capabilities.wavelength_range");
        String[] minMax = lambdaRange.split("-");

        int minLambda = Integer.valueOf(minMax[0]);
        int maxLambda = Integer.valueOf(minMax[1]);

        ChannelSpacing channelSpacing = ChannelSpacing.CHL_50GHZ;
        int lambdaCount = maxLambda - minLambda;
        int slotGranularity = 4;

        return IntStream.range(0, lambdaCount + 1)
                .mapToObj(x -> new OchSignal(GridType.DWDM, channelSpacing, x + minLambda, slotGranularity))
                .collect(ImmutableSet.toImmutableSet());
    }
}
