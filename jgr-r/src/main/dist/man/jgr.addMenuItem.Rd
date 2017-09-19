\name{jgr.addMenuItem}
\alias{jgr.addMenuItem}
\alias{jgr.insertMenuItem}
\title{
  New JGR Console MenuItem
}
\description{
  adds a new MenuItem to specified JGR Console Menu
}
\usage{
jgr.addMenuItem(menu,name,command,silent=TRUE)
jgr.insertMenuItem(menu,name,command,index,silent=TRUE)
}
\arguments{
  \item{menu}{Name of the menu that this item will be added to}
  \item{name}{Name of the menu item to add}
  \item{command}{R expression(s) as a string to be parsed and evaluated or a function that will be called (without arguments) when the menu item is selected}
  \item{index}{index at which to insert}
  \item{silent}{If FALSE, executes as if entered into the cons}
}
\value{
  MenuItem
}
\seealso{
  \code{\link{jgr.addMenu}}
  \code{\link{jgr.addMenuSeparator}}
}
\examples{
jgr.addMenu("Workspace")
jgr.addMenuItem("Workspace","Browse","ls()",FALSE)
jgr.addMenuSeparator("Workspace")
jgr.addMenuItem("Workspace","List Functions",
  function() unlist(lapply(ls(envir=.GlobalEnv),
                           function(x) if (is.function(get(x))) x else NULL )))
}
\keyword{programming}
