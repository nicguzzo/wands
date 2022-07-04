SET CURDIR=%cd%
SET VERS="1.16.5" "1.17.1" "1.18.1" "1.18.2" "1.19"

(for %%v in (%VERS%) do ( 
  echo %%v
  pushd wands%%v    
    rmdir common\src\main\java\net\nicguzzo\wands
    rmdir common\src\main\resources\assets
    rmdir common\src\main\resources\data      
    rmdir fabric\src\main\java\net\nicguzzo\wands\fabric
    rmdir fabric\src\main\java\net\nicguzzo\wands\forge      
  popd

))