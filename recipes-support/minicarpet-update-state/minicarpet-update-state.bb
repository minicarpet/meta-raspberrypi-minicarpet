SUMMARY = "Minicarpet A/B update state management"
LICENSE = "CLOSED"

SRC_URI = " \
    file://minicarpet-mark-good \
    file://minicarpet-mark-good.service \
"

S = "${UNPACKDIR}"

inherit systemd

RDEPENDS:${PN} += "libubootenv-bin"

SYSTEMD_SERVICE:${PN} = "minicarpet-mark-good.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 ${UNPACKDIR}/minicarpet-mark-good \
        ${D}${sbindir}/minicarpet-mark-good

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/minicarpet-mark-good.service \
        ${D}${systemd_system_unitdir}/minicarpet-mark-good.service
}
