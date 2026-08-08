# meta-raspberrypi-minicarpet

Minicarpet OS layer for Raspberry Pi 4 (64-bit), targeting Yocto Project Wrynose.

## Build

Use the supplied `raspberry-pi` template, then build:

```sh
bitbake minicarpet-image
```

The image recipe generates:

- an uncompressed ext4 root filesystem used by SWUpdate;
- a compressed Wic SD-card image for initial provisioning;
- an SWUpdate `.swu` bundle.

## Storage layout

| Partition | Purpose | Filesystem |
| --- | --- | --- |
| `mmcblk0p1` | shared boot | FAT |
| `mmcblk0p2` | rootfs A | ext4 |
| `mmcblk0p3` | rootfs B | ext4 |
| `mmcblk0p4` | shared data | ext4 |

U-Boot stores the selected root partition in `rootpart` (`2` or `3`). SWUpdate
updates the inactive root filesystem and updates this U-Boot environment value.

> Current limitation: the kernel and boot files are shared on partition 1. A
> rootfs-only update must therefore use a kernel-compatible rootfs. Full
> fail-safe kernel A/B and automatic rollback/boot-count handling are not yet
> implemented.

## A/B kernel and rollback transaction

The shared FAT boot partition contains one kernel per rootfs slot:

- `Image.A` for `/dev/mmcblk0p2`
- `Image.B` for `/dev/mmcblk0p3`

`minicarpet-swupdate.sh` is declared as a SWUpdate `shellscript` handler. It
rejects updates aimed at the active rootfs during `preinst`, then during
`postinst` it activates the newly written slot only after the rootfs and its
slot-specific kernel have both been installed successfully.

U-Boot tracks `upgrade_available`, `bootcount`, `bootlimit` and
`rollback_rootpart`. The new slot is allowed three boot attempts. Once userspace
reaches `multi-user.target`, `minicarpet-mark-good.service` clears the pending
update state. If that never happens, U-Boot returns to `rollback_rootpart`.

For product-level health validation, change the mark-good service ordering so it
runs only after the critical Minicarpet application/service has proven healthy.

Device-tree blobs and Raspberry Pi firmware remain shared in this design; only
the Linux kernel and rootfs are slot-specific.
