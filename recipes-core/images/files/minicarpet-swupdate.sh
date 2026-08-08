#!/bin/sh
set -eu

PHASE="${1:-}"
shift || true

# SWUpdate appends the data= value to the script command. Keep it as one
# comma-separated argument so this remains robust even if handler arguments
# evolve. Format: <target-rootpart>,<target-kernel-file>
DATA=""
for arg in "$@"; do
    DATA="$arg"
done

TARGET_ROOTPART="${DATA%%,*}"
TARGET_KERNEL="${DATA#*,}"

case "$TARGET_ROOTPART" in
    2|3) ;;
    *)
        echo "Invalid target root partition: '$TARGET_ROOTPART'" >&2
        exit 1
        ;;
esac

case "$TARGET_KERNEL" in
    *.A|*.B) ;;
    *)
        echo "Invalid target kernel filename: '$TARGET_KERNEL'" >&2
        exit 1
        ;;
esac

active_rootpart()
{
    sed -n 's/.*root=\/dev\/mmcblk0p\([0-9][0-9]*\).*/\1/p' /proc/cmdline
}

ACTIVE_ROOTPART="$(active_rootpart)"
case "$ACTIVE_ROOTPART" in
    2|3) ;;
    *)
        echo "Unable to determine active A/B rootfs from /proc/cmdline" >&2
        exit 1
        ;;
esac

case "$PHASE" in
    preinst)
        # This check MUST happen before the raw ext4 handler runs. A post-only
        # check would be too late and could overwrite the mounted rootfs.
        if [ "$ACTIVE_ROOTPART" = "$TARGET_ROOTPART" ]; then
            echo "Refusing to update active rootfs /dev/mmcblk0p${TARGET_ROOTPART}" >&2
            exit 1
        fi

        echo "Updating inactive rootfs p${TARGET_ROOTPART}; active rootfs is p${ACTIVE_ROOTPART}"
        ;;

    postinst)
        # Rootfs and kernel file handlers have completed successfully before
        # this phase. Verify the new slot-specific kernel is visible before
        # changing persistent U-Boot state.
        if ! grep -qs ' /boot ' /proc/mounts; then
            mount /boot
        fi

        if [ ! -s "/boot/${TARGET_KERNEL}" ]; then
            echo "Installed kernel /boot/${TARGET_KERNEL} is missing or empty" >&2
            exit 1
        fi

        # The SWU contains a compact ext4 filesystem image. A raw write restores
        # that filesystem size, not the size of the 4 GiB partition. Expand the
        # inactive filesystem to consume the complete A/B slot before activation.
        TARGET_ROOTDEV="/dev/mmcblk0p${TARGET_ROOTPART}"
        if ! command -v resize2fs >/dev/null 2>&1; then
            echo "resize2fs is not installed (e2fsprogs-resize2fs is required)" >&2
            exit 1
        fi
        resize2fs "${TARGET_ROOTDEV}" 2>&1

        if ! command -v fw_setenv >/dev/null 2>&1; then
            echo "fw_setenv is not installed (libubootenv-bin is required)" >&2
            exit 1
        fi

        # Write rootpart LAST. If power is lost during the preceding environment
        # updates, the currently running known-good slot remains selected.
        fw_setenv rollback_rootpart "$ACTIVE_ROOTPART"
        fw_setenv bootlimit 3
        fw_setenv bootcount 0
        fw_setenv upgrade_available 1
        fw_setenv rootpart "$TARGET_ROOTPART"

        sync
        echo "Activated rootfs p${TARGET_ROOTPART} with ${TARGET_KERNEL}; rollback target is p${ACTIVE_ROOTPART}"
        ;;

    postfailure)
        # The target slot may be incomplete, but rootpart was not changed because
        # activation is performed only in postinst.
        echo "SWUpdate failed; keeping active rootfs p${ACTIVE_ROOTPART}"
        ;;

    *)
        echo "Unsupported SWUpdate script phase: '$PHASE'" >&2
        exit 1
        ;;
esac

exit 0
