
pushd Forge\src\main\resources
mklink /J assets ..\..\..\..\common\resources\assets
mklink /J data ..\..\..\..\common\resources\data
popd
pushd Forge\src\main\java\net\nicguzzo
mklink /J common ..\..\..\..\..\..\common\java\src
popd

pushd Fabric\src\main\resources
mklink /J assets ..\..\..\..\common\resources\assets
mklink /J data ..\..\..\..\common\resources\data
popd
pushd Fabric\src\main\java\net\nicguzzo
mklink /J common ..\..\..\..\..\..\common\java\src
popd