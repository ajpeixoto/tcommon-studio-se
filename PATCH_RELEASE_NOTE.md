---
version: 8.0.1
module: https://talend.poolparty.biz/coretaxonomy/42
product: 
- https://talend.poolparty.biz/coretaxonomy/183
- https://talend.poolparty.biz/coretaxonomy/23
---

# TPS-5573

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20231204\_TPS-5573\_v1-8.0.1 |
| Release date     | 2023-12-04 |
| Target version   | 20211109\_1610-8.0.1 |
| Product affected | Talend Studio |

## Introduction

This is a self-contained patch.

**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.

## Fixed issues

This patch contains the following fix:

- TDI-50317 [8.0.1] tFileInputPositional don't parse properly when pattern units field is set to Symbols



## Prerequisites

Consider the following requirements for your system:

- Must install Talend Studio 8.0.1 with the monthly released patch, Patch\_20230421\_R2023-04_v1-8.0.1.zip.
- Or must update the Talend Studio 8.0.1 with the URL, https://update.talend.com/Studio/8/updates/R2023-04/.

## Installation

Installation On Studio:

1. Shut down Talend studio if it is opened.
2. Extract the zip.
3. Merge the folder "plugins"  and its content to "{studio}/plugins" and overwrite the existing files. 
4. remove the folder "{studio}/configuration/org.eclipse.osgi".
5. Start the Talend studio.
6. Rebuild your jobs.
 
Installation On CI:

1. put the patch in a location that can be reached in CI environment.
2. copy the patch to <product.path>/patches folder between executions of org.talend.ci:builder-maven-plugin:8.0.x:install and org.talend.ci:builder-maven-plugin:8.0.x:generateAllPoms command.
3. if it's an automation task for CI/CD, use scripts to do the copy action.
4. Run the CI/CD task.
