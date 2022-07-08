SET CURDIR=%cd%
SET VERS="1.16.5" "1.17.1" "1.18.1" "1.18.2" "1.19"

(for %%v in (%VERS%) do ( 
  echo %%v
  pushd wands%%v
    pushd "common\src\main"
      pushd "java\net\nicguzzo"
        mklink /J wands %CURDIR%\src    
      popd
      pushd "resources"
        mklink /J assets %CURDIR%\assets
        mklink /J data   %CURDIR%\data
      popd
    popd
    pushd "fabric\src\main\java\net\nicguzzo\wands"
      mklink /J fabric %CURDIR%\fabric
    popd
    pushd "forge\src\main\java\net\nicguzzo\wands"
      mklink /J forge %CURDIR%\forge
    popd
  popd

))