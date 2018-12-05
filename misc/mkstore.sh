#!/bin/bash
file2cr=$1
mnt=$1
touch $file2cr
truncate -s 23G $file2cr
mke2fs -t ext4 -F $file2cr -L $file2cr
sudo mkdir /mnt/store/$file2cr
sudo mount $file2cr /mnt/store/$mnt
sudo mkdir /mnt/store/$mnt/wherisdb
sudo mkdir /mnt/store/$mnt/$file2cr
sudo chown -R 1000:1000 /mnt/store/$mnt/wherisdb
sudo chown -R 1000:1000 /mnt/store/$mnt/$file2cr