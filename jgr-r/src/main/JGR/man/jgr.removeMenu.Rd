\name{jgr.removeMenu}
\alias{jgr.removeMenu}
\alias{jgr.removeMenuItem}
\title{
  remove JGR Console menus
}
\description{
  removes a menu / menu item
}
\usage{
jgr.removeMenu(index)
jgr.removeMenuItem(menu,index)
}
\arguments{
  \item{menu}{name of the menu}
  \item{index}{index of the item to remove}
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

jgr.removeMenuItem("added menu",1)
jgr.removeMenu(length(jgr.getMenuNames()))
}
\keyword{programming}
