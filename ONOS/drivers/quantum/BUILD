COMPILE_DEPS = CORE_DEPS + JACKSON + [
    "//drivers/utilities:onos-drivers-utilities",
    "//protocols/netconf/api:onos-protocols-netconf-api",
    "//apps/optical-model:onos-apps-optical-model",
]

APPS = [
    "org.onosproject.netconf",
    "org.onosproject.models.common",
    "org.onosproject.drivers.netconf",
    "org.onosproject.drivers.optical",
    "org.onosproject.optical-model",
]

BUNDLES = [
    ":onos-drivers-quantum",
    "//drivers/utilities:onos-drivers-utilities",
]

osgi_jar(
    resources = glob(["src/main/resources/**"]),
    resources_root = "src/main/resources",
    deps = COMPILE_DEPS,
)

onos_app(
    app_name = "org.onosproject.drivers.quantum",
    category = "Drivers",
    description = "ONOS Quantum Device Drivers application.",
    included_bundles = BUNDLES,
    required_apps = APPS,
    title = "Quantum Device Drivers",
    url = "http://www.ieiit.cnr.it",
)
