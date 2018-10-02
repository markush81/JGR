:warning: JGR was mainly developed during my study time until 2006. Back then my programming skills where just coming up and evolving, that's why the code looks like it looks.

:warning: This branch is for JDK >= 1.9

# JGR - Java GUI for R

**JGR** (speak 'Jaguar') is a universal and unified Graphical User Interface for **R** (it actually abbreviates **J**ava **G**ui for **R**). **JGR** was introduced at the [useR!](http://www.ci.tuwien.ac.at/Conferences/useR-2004) meeting in 2004 and there is an introductory article in the [Statistical Computing and Graphics Newsletter Vol 16 nr 2 p9-12](http://stat-computing.org/newsletter/issues/scgn-16-2.pdf)


# HowTo

1. Install R from [r-project.org](https://www.r-project.org)
2. Install dependent packages from within R `install.packages(c('rJava','JavaGD'))`
2. Install packages from within R `install.packages('https://github.com/markush81/JGR/releases/download/1.9-1/JGR_1.9.1.tar.gz',repos=NULL)`
   
   ```
    R version 3.4.1 (2017-06-30) -- "Single Candle"
    Copyright (C) 2017 The R Foundation for Statistical Computing
    Platform: x86_64-apple-darwin15.6.0 (64-bit)
    
    R ist freie Software und kommt OHNE JEGLICHE GARANTIE.
    Sie sind eingeladen, es unter bestimmten Bedingungen weiter zu verbreiten.
    Tippen Sie 'license()' or 'licence()' für Details dazu.
    
      Natural language support but running in an English locale
    
    R is a collaborative project with many contributors.
    Type 'contributors()' for more information and
    'citation()' on how to cite R or R packages in publications.
    
    Tippen Sie 'demo()' für einige Demos, 'help()' für on-line Hilfe, oder
    'help.start()' für eine HTML Browserschnittstelle zur Hilfe.
    Tippen Sie 'q()', um R zu verlassen.
    
    [Vorher gesicherter Workspace wiederhergestellt]
    
    > install.packages('https://github.com/markush81/JGR/releases/download/1.9-1/JGR_1.9.1.tar.gz',repos=NULL)
    installiere auch Abhängigkeiten ‘rJava’, ‘JavaGD’
    
    
    ...
    
    
    * installing *source* package ‘JGR’ ...
    ** R
    ** inst
    ** preparing package for lazy loading
    ** help
    *** installing help indices
    ** building package indices
    ** testing if installed package can be loaded
    * DONE (JGR)
    
    > 

   ```
3. Run `JGR::JGR()`

# Notes

More information can be found at [rforge.net/JGR](http://www.rforge.net/JGR/index.html).

:exclamation: it also contains code from [RoSuDa Java package](https://github.com/s-u/rosuda) which is based in module `ibase` to be totally self-contained and have no dependency to iPlots anymore. As well as [JavaGD](http://www.rforge.net/JavaGD/) to be able to compile.