# workflow
Orchestration or workflow library for use by other packages

This package supports a dynamic workflow orchestration. The workflow can be defined via a 
number of means including the use of enum types that store the workflow steps. The workflow
itself is made up of a number of steps that are executed in sequence according to the 
response from the previous step. The workflow can be defined in a number of ways including
in code, in enums, in the database or configuration files depending on requirements.

You can down load and build locally which will then build the following archive and load to your maven repository

<groupId>au.com.kahaara.wf</groupId>
<artifactId>orchestration</artifactId>

Then you can add the following dependency if using maven to you pom.xml

<dependency>
	<groupId>au.com.kahaara.wf</groupId>
	<artifactId>orchestration</artifactId>
	<version>1.0.0</version>
</dependency>

