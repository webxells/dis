DIS
===
# prototype-bundled
webXells GmbH's prototype project of **D**ata **I**ntegration **S**ervice  (DIS)

**Important**: Source code is published under **CC BY-NC-ND 4.0** for now — this is a transitional step while we finalize our open-source licensing. We plan to transition to a more permissive license in the near future. Stay tuned for updates.
### LICENSE ###
This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.\
To view a copy of this license, see [LICENSE](LICENSE) file or visit [creativecommons.org](https://creativecommons.org/licenses/by-nc-nd/4.0/).
#### Third party licenses ####
To view a list of used third party software with its license, see [3RD_PARTY_LICENSES](3RD_PARTY_LICENSES) file.


# Requirements
* Java ≥ 21
# How to start
**TBD**

# Encyclopedia
## Dis
**D**ata **I**ntegration  **S**ervice
## Json
**J**ava**S**cript **O**bject **N**otation,\
@see https://en.wikipedia.org/wiki/JavaScript_Object_Notation
## Dison
**Dis** **O**bject **N**otation is a superset of RFC-8259 json. Any valid json is valid dison. The main motivation to make it a superset was to promote familiarity. Content-type is `application/dison`.
Additional feature commands are enclosed by `'`. Between `'` no further `'` are necessary There are two types of features.
### Block commands
Via block commands you can provide an object to a dison command.\
E.g.
```
'--dis-var': {
    "variableName": "var-content"
}
```
### Inline commands
Inline commands are made to place a dison command inside complex structures. Mostly has a functional character.\
E.g.
```
...
    "some-object-name": '--dis-var("text $variableName text")'
...
```
### Feature list
* `--dis-local-file`: includes content of local file as string
* `--dis-environment`: includes a predefined environment variable of the running system
* `--dis-include`: includes an extern dison file into current place. Root element of inclusion may be trimmed to fit into current dison context. You may provide variables only effective for insertion
* `--dis-include-as-children`: includes an extern dison file into current place. Root element may be an array - so array children are inserted in current dison context
* `--dis-template`: as block command you are able to define templates (similar to `--dis-var`) and with the inline call you may insert the template by name and optional var mapping (only visible inside template)
* `--dis-foreach`: pasting in current position for all array values a **json** snippet (replacing $current by current value)
* `--dis-foreach-template`: pasting for all array values a dison template (defined by `--dis-template`) (current array value defined in `--dis-var` $current)
* `--dis-var`: defining/inserting variables
* `--dis-var-default`: defining variables only for undefined ones
* `--dis-var-delete`: deleting a single variable or multiple (by array parameter)
* `--dis-if`: based mostly on variable conditions to decide if something should be inserted
    *  `--and`: if parameters resolve to true (and)
    *  `--or`: if any parameter resolve to true (or)
    *  `--not`: if parameters resolve to false (and)
    *  `--isset`: if variables (by parameters) as name are set (Variable-Plugin required)
    *  `--equals`: if parameters are equal
    *  `--empty`: if parameters are empty
* Math: some basic mathematical functions
    * `--dis-add`: Addition of two operands
    * `--dis-sub`: Substitution of two operands
    * `--dis-multiply`: Multiply of two operands
    * `--dis-round`: Round an operand
* `--dis-random`: returns a random long/big int number between 0 and 10000000. Range may be limited by max parameter e.g. `--dis-random(100)`
* `--dis-error`: halts dison parsing, throws exception with optional provided error message
* `//` or `/**/`: json without comments is confusing
## Type
Modularity inside configurations is accomplished by providing a type that is resolved to the proper class.\
In general class name shows responsibility. \
E.g.
```
{
    "type": "com.webxells.dis.base.validator.IsSet"
}
```
## Configuration
The whole definition of the `Dis` workload.
## Jobs
Description of one work definition based on `Trigger`, `Input`, `Output` and `Mapping`
## System
How Dis is handling provided `Job`s. You may call it workflow. In most cases running as a service is recommended.
```
  "system": {
    "type": "com.webxells.dis.workflow.service.ServiceSupplier"
  }
```
## Trigger
The condition when the `Job` is triggered.
## Input
Description what data is consumed. Only one input may be defined.
To load multiple data `com.webxells.dis.base.Join` is able to link data by `Linker`.
### Linker
Description how an `Input` is loaded into existing data.
## Output
Description what data is produced.
## Resource
Description where data is read/written.
## MappingConfiguration
Descriptions of MappingParts
### MappingPart
Description of mapping connecting `Input` with `Output`.
Most important definition are "input" and "output" `MappingPoint`s and multiple `MappingOperation`.
All definitions are optional, so you might have a `MappingPart` only with some `MappingOperation`s or only with an `Input` definition.
#### Refinements
Extra reusable feature flags for certain modules.
E.g a basic format `Refinement` may be used by a time module or a database module to specify format of time or datatype.  
To be specific each `Refinement` may be limited by the argument `forTypes:=class[]`.

e.g.
```
{
  "input":...
  "output":...
  "refinements": [
    {
      "type": "some.refinement.Class",
      "extraArg": "xyz",
      //set accessibillity only for class com.webxells.dis.time.Handler 
      "forTypes": [
        "com.webxells.dis.time.Handler"
      ]
    }
  ]
}
```
#### Dataset
Values assigned to `MappingPart`. Consists of multiple `DatasetPiece`.
There are cases where only one value of `Dataset` is read. To decide how to handle multiple values in such cases `MappingPart` has an attribute called "multiToSingleSelectStrategy":
* `FIRST`: first value is used - default
* `LAST`: last value is used
* `ERROR`: raises an error on this occurrences
##### DatasetPiece
Single value of `Dataset`.
#### MappingPoint
Definition of data location by `reference` and `path`. `reference` is mostly name of `Input`/`Output`
#### MappingPortrayal
Description of MappingPart by `reference`, `path` and `source` (might be`INPUT`or`OUTPUT`)
#### SubData
Description of sub `MappingConfiguration` of `MappingPart`. \
E.g. Parsing of a xml file containing a node containing some larger child-nodes that you want to validate as `MappingPart`s.
#### MappingOperation
Description of operation for a mapping part. There are processed in order of description.
`MappingOperation` are called for each `DatasetPiece`.
##### Validator
Validators are mapping operations that in general don't alter data.
If a validation fails, `MappingPart`s "validatorErrorStrategy" will be triggered:
* `RESET_CONFIGURATION`: removes current MappingConfiguration - useful to skip subData
* `SKIP_FOLLOWING_OPERATIONS`: stops operating further `MappingOperation`s
* `SKIP_DATASET_PIECE`: removes current processed `DatasetPiece`
* `SKIP_DATASET`: removes whole `Dataset`
* `CONTINUE_NEXT_READ`: skips current data reading, continues with reading next
* `ERROR`: raises error - breaks current job
##### SingleCallForAllValuesValidator
Same as `Validator` but called only once with first `DatasetPiece`.
##### Manipulator
Manipulators are mapping operations that in general alter data.
If a `Manipulator` encounters an error, this will be treated as a failing `Validator`.
##### SingleCallForAllValuesManipulator
Same as `Manipulator` but called only once with first `DatasetPiece`.

# Structure
Basic configuration (with some examples to demonstrate functionality)
```
{
  // configuration
  "system": {
    // system (com.webxells.dis.api.workflow.SystemSupplier)
  },
  "configurations": [
    // jobs (com.webxells.dis.api.config.JobConfig)
    {
      "name": "",
      "trigger": [ ], //com.webxells.dis.api.config.TriggerConfig
      "input": { }, //com.webxells.dis.api.config.InputConfig
      "output": [ ], //com.webxells.dis.api.config.OutputConfig
      "mapping": {
        "parts": [ //com.webxells.dis.api.config.MappingConfiguration
          {
            // mappingParts (com.webxells.dis.api.config.MappingPart)
            "input": {
              // mappingPoint (com.webxells.dis.api.config.MappingPoint)
              "reference": "",
              "path": ""
            },
            "output": {
              // mappingPoint (com.webxells.dis.api.config.MappingPoint)
              "reference": "",
              "path": ""
            }
            "multiToSingleSelectStrategy": "",
            "validatorErrorStrategy": "",
            "operations": [
              // mappingOperations (com.webxells.dis.api.MappingOperation)
              //   validators (com.webxells.dis.api.validator.Validator)
              //   manipulators (com.webxells.dis.api.manipulator.Manipulator)
            ],
            "subData": {
                // subData (com.webxells.dis.api.config.MappingConfiguration)
                "parts": [ ]
            }
          }
        ]
      }
    }
  ]
}
```
## Example
Configuration with a basic job, reading a csv containing usernames and saving a csv containing case variations, except for username "admin".
```
{
  "system": {
    //Dis works as a Service: Running endlessly in background; all jobs concurrently
    "type": "com.webxells.dis.workflow.service.ServiceSupplier"
  },
  "configurations": [
    {
      "name": "simple-job",
      "trigger": [
        {
          "type": "com.webxells.dis.base.trigger.JustStart"
        }
      ],
      "input": {
        "name": "file",
        "type": "com.webxells.dis.csv.CsvInput",
        "cellAccessType": "VALUE_OF_FIRST_LINE",
        "receiver": {
          "type": "com.webxells.dis.localfile.resource.LocalFile",
          "path": "/tmp/usernames.csv"
        }
      },
      "output": [
        {
          "name": "result",
          "type": "com.webxells.dis.handler.text.CsvOutput",
          "sender": {
            "type": "com.webxells.dis.localfile.resource.LocalFile",
            "path": "/tmp/usernames-case.csv"
          }
        }
      ],
      "mapping": {
        "parts": [
          {
            "input": {
              "reference": "file",
              "path": "word"
            },
            "output": {
              "reference": "result",
              "path": "original"
            }
            "operations": [
              {
                "type": "com.webxells.dis.base.validator.Equals",
                "value": "admin",
                "not": true
              }
            ]
          },
          {
            "input": {
              "reference": "file",
              "path": "word"
            },
            "output": {
              "reference": "result",
              "path": "lowercase"
            },
            "operations": [
              {
                //manipulator
                "type": "com.webxells.dis.base.manipulator.ChangeCase"
                //toType defaults to "LOWER"
              }
            ]
          },
          {
            "input": {
              "reference": "file",
              "path": "word"
            },
            "output": {
              "reference": "result",
              "path": "uppercase"
            },
            "operations": [
              {
                "type": "com.webxells.dis.base.manipulator.ChangeCase",
                "toType": "UPPER"
              }
            ]
          },
          {
            "output": {
              "reference": "result",
              "path": "all-together"
            },
            "operations": [
              {
                "type": "com.webxells.dis.base.manipulator.Concatenation",
                "delimiter": "_",
                "pieces": [
                  {
                      "reference": "file",
                      "path": "word"
                      //"source" defaults to "INPUT"
                      //in case of mutliple matching parts, first found is taken
                  },
                  {
                      "reference": "result",
                      "path": "lowercase",
                      "source": "OUTPUT"
                  },
                  {
                      "reference": "result",
                      "path": "uppercase",
                      "source": "OUTPUT"
                  }
                ]
              }
            ]
          }
        ]
      }
    }
  ]
}
```