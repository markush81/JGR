\name{jgr.addMenuSeparator}
\alias{jgr.addMenuSeparator}
\alias{jgr.insertMenuSeparator}
\title{
  New JGR Console MenuSeparator
}
\description{
  adds a Separator to a Menu in JGR Console
}
\usage{
jgr.addMenuSeparator(menu)
jgr.insertMenuSeparator(menu,index)
}
\arguments{
  \item{menu}{name of the menu to add this separator to}
  \item{index}{index at which to insert}
}
\value{
  Menu
}
\seealso{
  \code{\link{jgr.addMenu}}
  \code{\link{jgr.addMenuItem}}
}
\examples{
jgr.addMenu("Workspace")
jgr.addMenuItem("Workspace","Browse","ls()")
jgr.addMenuSeparator("Workspace")
jgr.addMenuItem("Workspace","Browse (pos=2)","ls(pos=2)")
}
\keyword{programming}
