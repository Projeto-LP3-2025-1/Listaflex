# Listaflex

Projeto da disciplina Linguagem de Programação 3.

## Tecnologias

- [Java 11+](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
- [Java Swing](https://docs.oracle.com/javase/8/docs/api/index.html?javax/swing/package-summary.html)
- [MySQL](https://dev.mysql.com/doc/)
- [JDBC](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/)
- [Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/)

## Rodando

Necessários JDK e MySQL Server instalados.

Rodar com os comandos:

````
javac -cp "lib/mysql-connector-j-9.3.0.jar:." src/main/java/project/Main.java src/main/java/project/dao/*.java src/main/java/project/model/*.java src/main/java/project/util/*.java src/main/java/project/view/*.java  
java -cp "out:lib/mysql-connector-j-9.3.0.jar" src/main/java/project/Main.java
````

## Banco MySQL

Usuário: root <br>
Senha: admin 

## Gerar o arquivo .jar

Para criar o executável com duplo clique, é necessário o [Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) instalado, e depois rodar os comandos:

```
mvn clean package
cp target/listaflex-1.0-SNAPSHOT-jar-with-dependencies.jar target/listaflex.jar
```

Após isso, só clicar no ícone **listaflex.jar** na pasta *target* para abrir o programa.

## Relatório

[Relatório do projeto em documento aqui](https://docs.google.com/document/d/1guL6Bfg23nWVw2u7haPVDjO94ofyeKM0buse4v86_WQ/edit?usp=sharing) para mais informações.