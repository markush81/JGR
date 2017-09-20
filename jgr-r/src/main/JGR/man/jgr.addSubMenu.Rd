\name{jgr.addSubMenu}
\alias{jgr.addSubMenu}
\alias{jgr.insertSubMenu}
\title{
  New JGR Console sub-menu
}
\description{
  adds a sub-menu to a Menu in JGR Console
}
\usage{
jgr.addSubMenu(menu, subMenuName, labels, commands)
jgr.insertSubMenu(menu, subMenuName, labels, commands,index)
}
\arguments{
  \item{menu}{name of the menu to add this sub-menu to}
  \item{subMenuName}{name of the sub-menu}
  \item{labels}{a character vector of menu items}
  \item{commands}{a character vector of commands to be exectued}
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
jgr.addMenu("added menu")
jgr.addMenuItem("added menu", "print 1","print(1)")
jgr.insertMenuItem("added menu","print 1 as if entered in console","print(1)",1,FALSE)
jgr.addMenuSeparator("added menu")
jgr.addSubMenu("added menu","sub menu",c("a","b","c"),c("print('a')","print('b')","print('c')"))

}
\keyword{programming}
