#!/bin/bash
touch 23gbarea
truncate -s 23G 23gbarea
mke2fs -t ext4 -F 23gbarea
sudo mount 23gbarea /mnt/store
