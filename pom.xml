<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>dev.pistonfilter</groupId>
    <artifactId>PistonFilter</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>PistonFilter</name>
    <description>Filters blocks that pistons can move — cancels piston movement if a forbidden block is found in the 16-block chain.</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <!-- Purpur repo -->
        <repository>
            <id>purpur</id>
            <url>https://repo.purpurmc.org/snapshots</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- Purpur API (includes Paper/Spigot/Bukkit API) -->
        <dependency>
            <groupId>org.purpurmc.purpur</groupId>
            <artifactId>purpur-api</artifactId>
            <version>1.21.1-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Shade plugin — fat jar if needed -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals><goal>shade</goal></goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
        <finalName>${project.artifactId}-${project.version}</finalName>
    </build>
</project>
