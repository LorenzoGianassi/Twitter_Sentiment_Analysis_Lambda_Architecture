# Twitter Sentiment Analysis with Lambda Architecture
![](Images/logo_large.png) <br/>
## Table of Contents  
- [About the Project](#1)  
  - [Built with](#2)
- [Datasets](#3)  
- [Usage](#4)
# About the Project <a name="1"/>
The sentiment analysis or opinion mining is the process of determining if a particular block of text is expressing a positive or negative reaction upon something.
The goal of this project is to present a functioning Lambda Architecture built to compute a sentiment analysis upon tweets, according to specific keywords. <br/>
The Implementation of the structure of the Lambda Architecture was made with *Apache Hadoop* for the *Batch Layer*, *Apache Storm* for the *Speed Layer* and *Apache HBase* for the *Serving Layer*. <br/>
To replicate the stream of tweets it was used the Twitter API, through the Twitter4J library. <br/>
A GUI, made with JavaFX, is provided to make easier the user experience.
*LingPipe* was used of process the tweets.
# Built with <a name="2"/>
- [*Apache Hadoop(3.2.1)*](https://hadoop.apache.org/release/3.2.1.html) : Apache Hadoop software library is a framework that allows for the distributed processing of large data sets across clusters of computers using simple programming models.
- [*Apache Storm(2.1.0)*](https://storm.apache.org/2019/10/31/storm210-released.html) : Apache Storm is a free and open source distributed realtime computation system. 
- [*Apache HBase(2.3.4)*](https://archive.apache.org/dist/hbase/2.3.4/) : Apache HBase is an open-source, distributed, versioned, non-relational database. 
- [*Twitter4J*](https://twitter4j.org/en/index.html) : Twitter4J is an unofficial Java library for the Twitter API. With Twitter4J, you can easily integrate your Java application with the Twitter service.
- [*LingPipe(4.1.0)*](http://www.alias-i.com/lingpipe/) : it is a tool kit for processing text using computational linguistics.
- [*JavaFX*](https://openjfx.io/) : JavaFX is an open source, next generation client application platform for desktop, mobile and embedded systems built on Java.
# Datasets <a name="3"/>
- [*Sentiment140*](https://www.kaggle.com/datasets/kazanova/sentiment140) : This is the sentiment140 dataset. It contains 1,600,000 tweets extracted using the twitter API.
- [*FullCorpus*](https://github.com/guyz/twitter-sentiment-dataset)
# Usage <a name="4"/>
To replicate the code is necessary to get your own ```Twitter Developer Credentials```  and replace them in the placeholder text file in the repo.
Next you need to start the server by running respectively Apache Hadoop, Storm and HBase. <br/>
Then run the  ```ClassifierLambdaArchitecture``` to train and store the model that will be required by the Lambda Architecture. So you have to set the datasets paths and the the file to store the classifier model. <br/>
Finally execute the class in the following order:
- ```Topology``` setting as args the keywords for the query
- ```BatchDriver```
- ```GUILauncher```
# Authors
- **Lorenzo Gianassi**
# Acknowledgments
Parallel Computing Project © Course held by Professor [Marco Bertini](https://www.unifi.it/p-doc2-2019-0-A-2b333d2d3529-1.html) - Computer Engineering Master Degree @[University of Florence](https://www.unifi.it/changelang-eng.html)

