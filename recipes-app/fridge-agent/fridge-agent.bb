SUMMARY = "Fridge AI agent"
DESCRIPTION = "AI-assisted fridge inventory, meal planning and shopping list service"
HOMEPAGE = "https://github.com/minicarpet/fridge-agent"

LICENSE = "CLOSED"

SRC_URI = " \
    git://github.com/minicarpet/fridge-agent.git;protocol=https;branch=main \
    file://fridge-agent.service \
    file://fridge-agent.env \
    file://fridge-agent-tmpfiles.conf \
"

SRCREV = "${AUTOREV}"

PV = "0.2.0+git"

inherit python_setuptools_build_meta
inherit systemd
inherit useradd

SYSTEMD_SERVICE:${PN} = "fridge-agent.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

USERADD_PACKAGES = "${PN}"

FRIDGE_AGENT_UID = "995"
FRIDGE_AGENT_GID = "995"

GROUPADD_PARAM:${PN} = " \
    --system \
    --gid ${FRIDGE_AGENT_GID} \
    fridge-agent \
"

USERADD_PARAM:${PN} = " \
    --system \
    --uid ${FRIDGE_AGENT_UID} \
    --gid ${FRIDGE_AGENT_GID} \
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

    install -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 \
        ${UNPACKDIR}/fridge-agent-tmpfiles.conf \
        ${D}${sysconfdir}/tmpfiles.d/fridge-agent.conf
}

pkg_postinst:${PN}() {
    if [ -z "$D" ]; then
        systemctl try-restart fridge-agent.service || true
    fi
}

CONFFILES:${PN} += " \
    ${sysconfdir}/fridge-agent/fridge-agent.env \
"