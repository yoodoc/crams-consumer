crams indexer
=============

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
config/* contains configuration files.  
  - kafkaConsumer.properties contains kafka client configuration  
  - lasticsearch.properties contains es client configuration  
  - cramsIndexerPlugins.properties contains plugin setting for specific kafka topic  
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
