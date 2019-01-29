# Semantic Anomaly Detection

This repository refers to the article *Detecting Anomalies in Image Classification by means of Semantic Relationships*.<br/>
Authors: Andrea Pasini, Elena Baralis.


**Data**

The source data for the experiments comes from the MIT Scene Parsing Benchmark, http://sceneparsing.csail.mit.edu/
(ADE20K dataset). The downloaded data must be placed in the "./Data/ADEChallengeData2016/" folder.
In order to run the experiments in this repository you should execute the neural network PSPNet (https://github.com/hszhao/PSPNet) on the 2000 images of the validation set. This operation allows having the labeled pixels necessary to test the anomaly detection phase.
<br/>
Although the knowledge base can be retrieved from scratch by analyzing the ground truth labels of the 20,000 training images, a ready-to-use copy is provided in this repository (in the "./Data" folder).
Specifically, the files "cf", "summaryArea", "summaryPosition" present a textual version of the knowledge base, useful to inspect the retrieved relationships.
Instead, "contextStatistics" contains the serialized Java object with the knowledge base ready to be used with the code in this repository.

**Source**

The code in this repository is related to the parts of our method architecture which:
- extract connected components from the image segmented by the PSPNet neural network
- compute the relative properties between objects (e.g. relative position with the string representation of images).
- compute the histograms of the knowledge base
- detect anomalies with both the Anomaly-Only and the Delta Methods.

In the Main class, there are the six predefined experiments shown in our white paper.
