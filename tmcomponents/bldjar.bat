xcopy resources\*.* dist/s/y
xcopy classes\*.* dist/s/y
cd dist
jar -cvf TM-Components-v1.0.jar com images
cd..