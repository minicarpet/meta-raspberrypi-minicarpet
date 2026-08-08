SWUPDATE_HWBOARD ?= "${MACHINE}"
SWUPDATE_HWREV ?= "1.0"

do_install:append() {
    install -d ${D}${sysconfdir}

    echo "${SWUPDATE_HWBOARD} ${SWUPDATE_HWREV}" \
        > ${D}${SWUPDATE_HW_COMPATIBILITY_FILE}
}

FILES:${PN}:append = " ${SWUPDATE_HW_COMPATIBILITY_FILE}"