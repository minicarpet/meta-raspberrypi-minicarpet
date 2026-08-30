SUMMARY = "Minicarpet APT feed configuration"
DESCRIPTION = "Installs the Minicarpet development APT package feed configuration."
LICENSE = "CLOSED"

SRC_URI = "file://minicarpet.list"

do_install() {
    install -d ${D}${sysconfdir}/apt/sources.list.d

    install -m 0644 ${UNPACKDIR}/minicarpet.list \
        ${D}${sysconfdir}/apt/sources.list.d/minicarpet.list
}

FILES:${PN} = "${sysconfdir}/apt/sources.list.d/minicarpet.list"