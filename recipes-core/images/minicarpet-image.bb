SUMMARY = "Minicarpet OS image"

# Keep an uncompressed ext4 rootfs for fast SWUpdate installation and build a
# compressed Wic image for initial SD-card provisioning.
IMAGE_FSTYPES = "ext4 wic.bz2"

WKS_FILE = "minicarpet-ab.wks.in"

# The standalone ext4 must contain the same fstab as the initial Wic image.
# Wic must therefore not rewrite /etc/fstab while assembling the SD image.
WIC_CREATE_EXTRA_ARGS += " --no-fstab-update"

# A rootfs slot is exactly 4 GiB. Fail the build before generating an update
# that cannot fit into either A or B.
IMAGE_ROOTFS_MAXSIZE = "4194304"

# swupdate-image on Wrynose prepends IMAGE_NAME_SUFFIX (normally .rootfs) to
# this extension and generates the .swu as part of `bitbake minicarpet-image`.
SWUPDATE_IMAGES_FSTYPES[minicarpet-image] = ".ext4"

# The Raspberry Pi Wrynose BSP keeps the kernel outside the rootfs. Include the
# deployed kernel as an additional SWU artifact so it can be updated together
# with the inactive rootfs slot.
SWUPDATE_IMAGES += "${KERNEL_IMAGETYPE}"
do_swuimage[depends] += "virtual/kernel:do_deploy"

# Included in the SWU. type=shellscript makes SWUpdate call it both before and
# after installation, allowing active-slot validation before the raw write and
# slot activation only after rootfs + kernel installation succeeds.
SRC_URI += "file://minicarpet-swupdate.sh"

inherit core-image extrausers swupdate-image

IMAGE_FEATURES += " \
    allow-root-login \
    ssh-server-openssh \
    tools-debug \
    package-management \
"

IMAGE_INSTALL:append = " \
    swupdate \
    minicarpet-update \
    minicarpet-update-state \
    e2fsprogs \
    e2fsprogs-resize2fs \
    util-linux \
    util-linux-lsblk \
    util-linux-findmnt \
    nano \
    ethtool \
    iproute2 \
    \
    fridge-agent \
    \
    sqlite3 \
    curl \
    openssl \
    \
    avahi-daemon \
    libnss-mdns \
    tzdata \
"

IMAGE_LINGUAS = ""

# Keep logs across reboots. Storage=persistent makes journald create/use
# /var/log/journal once the writable root filesystem is available.
minicarpet_configure_journald() {
    install -d ${IMAGE_ROOTFS}${sysconfdir}/systemd/journald.conf.d
    cat > ${IMAGE_ROOTFS}${sysconfdir}/systemd/journald.conf.d/10-minicarpet-persistent.conf <<'JOURNAL_EOF'
[Journal]
Storage=persistent
SystemMaxUse=100M
RuntimeMaxUse=50M
JOURNAL_EOF
}
ROOTFS_POSTPROCESS_COMMAND += "minicarpet_configure_journald; "

# Raspberry Pi's 99-com.rules assigns I2C/SPI/GPIO devices to these groups.
# Create them in the image so udev does not discard those rules.
PASSWD = "\$5\$ca6NiYxhYPKvzERO\$GYbst0njWo14eUL1vwS7qUZb2St5.G.ASbVJZhTG1M2"
EXTRA_USERS_PARAMS = " \
    groupadd -r -f i2c; \
    groupadd -r -f spi; \
    groupadd -r -f gpio; \
    usermod -p '${PASSWD}' root; \
"
