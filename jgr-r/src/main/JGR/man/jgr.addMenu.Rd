\name{jgr.addMenu}
\alias{jgr.addMenu}
\alias{jgr.insertMenu}
\title{
  New JGR Console Menu
}
\description{
  adds a new Menu to MenuBar in JGR Console
}
\usage{
jgr.addMenu(name)
jgr.insertMenu(name, index)
}
\arguments{
  \item{name}{Menu name}
  \item{index}{index at which to insert}
}
\value{
  Menu
}
\seealso{
  \code{\link{jgr.addMenuItem}}
  \code{\link{jgr.addMenuSeparator}}  
}
\examples{
jgr.addMenu("Workspace")
jgr.addMenuItem("Workspace","Browse","ls()")
jgr.addMenuSeparator("Workspace")
jgr.addMenuItem("Workspace","Browse (pos=2)","ls(pos=2)")


menus <- jgr.getMenuNames()
index <- which(menus=="Packages & Data")
if(length(index)==0) index <- 1

jgr.insertMenu("User menu", index)
jgr.addMenuItem("User menu", "Good place for user generated menus is",
                "print('before Packages & Data')")
}
\keyword{programming}
