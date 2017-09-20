\name{jgr.getMenuNames}
\alias{jgr.getMenuItemNames}
\alias{jgr.getMenuNames}
\title{
  JGR Console menus
}
\description{
  returns the menu / menu item names
}
\usage{
jgr.getMenuNames()
jgr.getMenuItemNames(menu)
}
\arguments{
  \item{menu}{name of the menu}
}
\value{
  a character vector
}
\seealso{
  \code{\link{jgr.addMenu}}
  \code{\link{jgr.addMenuItem}}
}
\examples{
menus <- jgr.getMenuNames()
index <- which(menus=="Packages & Data")
if(length(index)==0) index <- 1

jgr.insertMenu("User menu",index)
jgr.addMenuItem("User menu", "Good place for user generated menus is",
                "print('before Packages & Data')")
}
\keyword{programming}
