[![codecov](https://codecov.io/github/GiacomoRomagnoli/Social-Network/branch/dev/graph/badge.svg?token=FAYRLMA91C)](https://codecov.io/github/GiacomoRomagnoli/Social-Network)
# Social Network
[Explore the report »](./resurces/report/docs/docs.md)\
[Documentation »](./resurces/dokka/index.html)

## Table of Contents
- [About the Project](#about-the-project)
  - [Abstract](#abstract)
  - [Built With](#built-with)
- [Getting Started](#getting-started)

# About the Project
The project is developed and maintained by:
- Marco Fontana - marco.fontana17@studio.unibo.it

## Abstract
The current market includes several social networks, 
each differentiated primarily by the type of post that users can publish. 
This project aims to create the backbone of a generic social network,
from which it would be possible to specialize into a fully-fledged application by identifying a particular type of post 
or adding distinctive features.

## Built With
### Infrastructure
- Docker
- Kafka
### Kotlin Microservices
- Kotlin
- Vertx
- Jackson
- Mockito
- Kover
- Ktlint
- MySQL Connector
- JUnit
### Typescript Microservices
- NodeJS
- Typescript
- Express
- MySQL2
- Jest
### Versioning
- Conventional Commits
- Semantic Versioning
### CI-CD
- Gradle
- Docker Hub
- GitHub Actions

## Getting Started
### Prerequisites
- Docker
### Installation
1. Clone the repository
```bash
$ git clone https://github.com/GiacomoRomagnoli/Social-Network.git
```
2. Navigate to folder
```bash
$ cd Social-Network
```
3. Create two files in which store db passwords
```bash
$ echo -n <password> > db-password.txt
$ echo -n <root-password> > db-root-password.txt
```
4. Run the deployment command
```bash
$ docker compose up
```
Note: Some microservices include docker-compose files to demonstrate how to deploy a single microservice. 
The steps to execute are similar to those described above, except for the directory in which you need to navigate.