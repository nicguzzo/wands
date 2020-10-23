pushd Forge/src/main/resources
ln -s ../../../../common/resources/assets
ln -s ../../../../common/resources/data
popd
pushd Forge/src/main/java/net/nicguzzo
ln -s ../../../../../../common/java/src
popd

pushd Fabric/src/main/resources
ln -s ../../../../common/resources/assets
ln -s ../../../../common/resources/data
popd
pushd Fabric/src/main/java/net/nicguzzo
ln -s ../../../../../../common/java/src
popd