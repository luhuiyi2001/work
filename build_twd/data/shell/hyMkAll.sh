#!/bin/bash
J_SRC_PROJ=$1

if [ -z "$J_SRC_PROJ" ] ; then
	echo "---------->>>>>> J_SRC_PROJ is null!"
	exit 0
fi

echo "------ start HY make : $J_SRC_PROJ------"

source build/envsetup.sh
lunch full_${J_SRC_PROJ}-user
./tyMk.py n

echo "------ end HY make : $J_SRC_PROJ ------"