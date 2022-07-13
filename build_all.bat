SET CURDIR=%cd%
SET VERS="1.16.5" "1.17.1" "1.18.1" "1.18.2" "1.19"

(for %%v in (%VERS%) do ( 
  pushd wands%%v
    gradlew build
  popd
))