\name{launchJGR}
\alias{launchJGR}
\alias{launchJGR}
\title{
  Creates a new process launching the JGR Console
}
\description{
  Creates a new process launching the JGR Console
}
\usage{
launchJGR(javaArgs=NULL,jgrArgs="",popMsgs=TRUE)
}
\arguments{
  \item{javaArgs}{Arguments for Java.}
  \item{jgrArgs}{Arguments passed to the main method of JGR.}
  \item{popMsgs}{If true, system pop-up messages are displayed if any errors are encountered.}
}
\value{
  The system call for launching JGR
}
 