FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# base-files already fetches file://fstab; FILESEXTRAPATHS makes this layer's
# version take precedence. fw_env.config is additional Minicarpet configuration.
SRC_URI += "file://fw_env.config"

dirs755:append = " /data"

do_install:append() {
    install -m 0644 ${UNPACKDIR}/fw_env.config ${D}${sysconfdir}/fw_env.config
}
