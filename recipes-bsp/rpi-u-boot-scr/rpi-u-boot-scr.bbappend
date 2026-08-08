# meta-raspberrypi/wrynose provides an unversioned rpi-u-boot-scr.bb.
# Override its boot.cmd.in with the Minicarpet A/B-aware boot script.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
