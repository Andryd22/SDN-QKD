package org.onosproject.drivers.quantum;

import org.onosproject.net.driver.AbstractDriverLoader;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class QuantumDriversLoader extends AbstractDriverLoader {
    public QuantumDriversLoader() {
        super("/quantum-drivers.xml");
    }
}