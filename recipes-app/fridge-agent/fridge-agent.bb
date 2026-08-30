SUMMARY = "Fridge AI agent"
DESCRIPTION = "AI-assisted fridge inventory, meal planning and shopping list service"
HOMEPAGE = "https://github.com/minicarpet/fridge-agent"

LICENSE = "CLOSED"

SRC_URI = " \
    git://github.com/minicarpet/fridge-agent.git;protocol=https;branch=main \
    file://fridge-agent.service \
    file://fridge-agent.env \
"

SRCREV = "${AUTOREV}"

PV = "0.1.0+git"

inherit python_setuptools_build_meta
inherit systemd
inherit useradd

SYSTEMD_SERVICE:${PN} = "fridge-agent.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

USERADD_PACKAGES = "${PN}"

GROUPADD_PARAM:${PN} = " \
    --system fridge-agent \
"

USERADD_PARAM:${PN} = " \
    --system \
    --gid fridge-agent \
    --home-dir ${localstatedir}/lib/fridge-agent \
    --no-create-home \
    --shell /bin/false \
    fridge-agent \
"

RDEPENDS:${PN} += " \
    python3-core \
    python3-aiohttp \
    python3-jinja2 \
    python3-httpx \
    python3-pydantic \
    python3-sqlite3 \
    ca-certificates \
"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 \
        ${UNPACKDIR}/fridge-agent.service \
        ${D}${systemd_system_unitdir}/fridge-agent.service

    install -d ${D}${sysconfdir}/fridge-agent
    install -m 0644 \
        ${UNPACKDIR}/fridge-agent.env \
        ${D}${sysconfdir}/fridge-agent/fridge-agent.env
}

CONFFILES:${PN} += " \
    ${sysconfdir}/fridge-agent/fridge-agent.env \
"