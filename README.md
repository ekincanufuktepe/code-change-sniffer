# Code Change Sniffer (CCS)
This is a study on predicting code changes with Markov Chains for Java projects based on their commits. To have a better understanding how Markov Chains is used in CCS, please check the [Running Example Supplementary Material](/docs/Supplementary_Material__Running_Example_of_Code_Change_Sniffer_Method.pdf)

![](/figures/junit4_prediction_example.png)

## How to Run CCS
CCS runs by the class `MainCCS.java`, and requires two arguments:
* **First argument:** Directory to last commit
* **Second argument:** Directory last version of the project

### Example:
Assume you want to predict the code changes based on the last version of JJWT `jjwt-0.6.0`, with respect to the changes that made in the commit `jjwt-e392524919ecd155c00240533f54e66b6b1eaa82`. The arguments shall be given as below:
* **First argument:** `"../jjwt-e392524919ecd155c00240533f54e66b6b1eaa82"`
* **Second argument:** `"../jjwt-0.6.0"`

## Setting Inputs for CCS
There are several inputs that needs to be setup for code change prediction:
* Setting model
* Setting commit number
* Setting project commit directory for output

### Setting model
CCS has two models implemented *Call Graph (CG)* and *Effect Graph (EG)* based models. To set the model to be used, in the `MainCCS.java` set the `MarkovChains.model` to:

`SelectModel.EFFECT_GRAPH;` or `SelectModel.CALL_GRAPH;`

### Setting commit number
To set the commit number that will be analyzed, in the `MainCCS.java` class, initialize the `MarkovChains.commitNum` variable to the commit number that will be analyzed. For example, if the 16th commit is going to be analyzed, set the variable as below:

`MarkovChains.commitNum=16;`

### Setting project commit directory for output
To generate the prediction output into the specified directory, in the `MainCCS.java` class, initialize the `predictionOutputFile` variable. The prediction output is normally  implemtented to generate the output into CCS under the `../data` folder. See dataset organization

## Dataset
Dataset is organized as the following:

```
data
└── project
    ├── version + commit_number
    |   └── EIS_Markov_FS_CG.txt (Estimated Impact Set of Call Graph)
    |   └── EIS_Markov_FS_EG.txt (Estimated Impact Set of Effect Graph)
    ├── ...
    ├── README.md (table of precision, recall, f-measure results, and change types for each commit)
    ├── charts
    └── AIS.txt (Actual Impact Set)
```

## How to Generate Ground Truth
To generate ground truth, you need to run the `ccs.markov.change.TestNumberOfChanges.java` file. The `TestNumberOfChanges.java` class requires two arguments. The first argument must be the last version of the project, and the second argument must be the previous version of the project. For example, for the versions *0.6.0* and *0.7.0* of project *JJWT*, the `TestNumberOfChanges.java` class should be run as below:

`"../jjwt-0.7.0" "../jjwt-0.6.0"`

## Citing
If you have used our work please cite us
```tex
@inproceedings{ufuktepe2021ccs,
  title={Code Change Sniffer: Predicting Future Code Changes with Markov Chain},
  author={Ufuktepe, Ekincan and Tuglular, Tugkan},
  booktitle={IEEE Computer Software and Applications Conference},
  year={2021},
}
```
