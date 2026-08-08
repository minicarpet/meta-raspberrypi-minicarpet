SUMMARY = "Minicarpet SWUpdate wrapper"
LICENSE = "CLOSED"

SRC_URI = "file://minicarpet-update"

S = "${UNPACKDIR}"

RDEPENDS:${PN} += "swupdate libubootenv-bin"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/minicarpet-update \
        ${D}${bindir}/minicarpet-update
}