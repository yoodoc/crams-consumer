crams indexer
=============

packaging 
--------
>./install.sh


install
-------
>tar xvzf crams-indexer.tar.gz    ## target/crams-indexer.tar.gz

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
