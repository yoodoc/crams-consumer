crams indexer
=============

wiki
--------
http://wiki.ktcloudware.com/display/CDP/WATCH+Agent

source hierarchy
--------
- conf/
설정 파일
- bin/
바이너리 실행 스크립트
- src/
java 소스 및 라이브러리
- README.txt
- pom.xml
maven 설정 파일
- assembly.xml
바이너리 압축 설정 파일
- install.sh
바이너리 압축 파일을 생성하기 위한 실행 스크립트

packaging 
--------
kafka consumer java client 라이브러리를 local maven 저장소에 등록한 후, 프로젝트를빌드한다.
>./install.sh


install
-------
1. 먼저, jsvc를 설치한다. 
>sudo apt-get install jsvc

2. 압축을 해제한다. 
>tar xvzf crams-indexer.tar.gz    ## target/crams-indexer.tar.gz

3. 실행 스크립트의 환경변수 값을 설정한다. 
압축제된 폴더의 ../bin/creams-indexer.sh 의 java, jsvc 경로 내용을 머신 설정에 맞게 수정한다. 

configurations
-------------
conf/* contains configuration files.  
  - kafkaConsumer.properties contains kafka client configuration  
  - esIndexer.properties contains es client configuration  
  - cramsConsumerPlugins.properties contains plugin setting for specific kafka topic  
  - indexSetting.json contaions index setting info to generate new es index  
  - mappingInfo.json contaion mapping info to generate new es index mapping  

run 
------
>cd crams-indexer
>bin/crams-indexer.sh start

stop
------
>bin/crams-indexer.sh stop

thankyou,thankyou
